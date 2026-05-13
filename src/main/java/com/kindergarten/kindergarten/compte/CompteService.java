package com.kindergarten.kindergarten.compte;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.kindergarten.kindergarten.director.Director;
import com.kindergarten.kindergarten.director.DirectorRepo;
import com.kindergarten.kindergarten.parent.Parent;
import com.kindergarten.kindergarten.parent.ParentRepo;

/**
 * Singleton Service - Centralise toute la logique métier de gestion des comptes
 * Spring garantit UNE SEULE INSTANCE pour toute l'application (@Service)
 *
 * Remplace la duplication de code dans : - CompteController -
 * DirectorController - ParentController
 */
@Service
public class CompteService {

    @Autowired
    private CompteRepo compteRepo;

    @Autowired
    private AuthoritiesRepo authoritiesRepo;

    @Autowired
    private ParentRepo parentRepo;

    @Autowired
    private DirectorRepo directorRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Injecté depuis WebSecurityConfig

    // ============= CRÉER UN COMPTE =============
    /**
     * Crée un nouveau compte avec mot de passe chiffré
     */
    public Compte creerCompte(String email, String password, String type) {
        Compte compte = new Compte();
        compte.setEmail(email);
        compte.setPassword(passwordEncoder.encode(password));
        compte.setEnabled(false);
        Compte savedCompte = compteRepo.save(compte);

        // Attribuer le rôle correspondant
        if (type != null && !type.isBlank()) {
            Authorities autorite = new Authorities(savedCompte, RoleType.fromLegacy(type));
            authoritiesRepo.save(autorite);
        }

        return savedCompte;
    }

    // ============= RÉCUPÉRER L'UTILISATEUR COURANT =============
    /**
     * Récupère le compte de l'utilisateur actuellement connecté Remplace le
     * bloc dupliqué dans chaque contrôleur :
     *
     * Ancien code (dupliqué partout): Compte currentuser = null; if (principal
     * != null) { String email = principal.getName(); currentuser =
     * cptrepo.findById(email).get(); }
     */
    public Compte getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return compteRepo.findById(principal.getName()).orElse(null);
    }

    // ============= SAUVEGARDER UN COMPTE =============
    /**
     * Sauvegarde un compte avec chiffrement du mot de passe si nécessaire
     */
    public Compte saveCompte(Compte compte) {
        if (compte.getPassword() != null && !compte.getPassword().isEmpty()) {
            // Ne rechiffrer que si le mot de passe n'est pas déjà chiffré
            if (!compte.getPassword().startsWith("$2a$")
                    && !compte.getPassword().startsWith("$2b$")
                    && !compte.getPassword().startsWith("$2y$")) {
                compte.setPassword(passwordEncoder.encode(compte.getPassword()));
            }
        }
        return compteRepo.save(compte);
    }

    /**
     * Sauvegarde un compte en préservant l'ancien mot de passe si aucun n'est
     * fourni
     */
    public Compte saveCompteWithOldPassword(Compte newCompte) {
        Optional<Compte> oldCompte = compteRepo.findById(newCompte.getEmail());

        if (newCompte.getPassword() == null || newCompte.getPassword().isEmpty()) {
            // Préserver le mot de passe existant
            if (oldCompte.isPresent()) {
                newCompte.setPassword(oldCompte.get().getPassword());
            }
        } else {
            // Chiffrer le nouveau mot de passe
            newCompte.setPassword(passwordEncoder.encode(newCompte.getPassword()));
        }

        return compteRepo.save(newCompte);
    }

    // ============= ACTIVER / DÉSACTIVER =============
    /**
     * Active un compte et crée automatiquement son autorité si elle n'existe
     * pas
     */
    public void activerCompte(String email) {
        Optional<Compte> compteOpt = compteRepo.findById(email);
        if (!compteOpt.isPresent()) {
            throw new IllegalArgumentException("Compte introuvable : " + email);
        }

        Compte compte = compteOpt.get();
        compte.setEnabled(true);
        compteRepo.save(compte);
    }

    /**
     * Désactive un compte
     */
    public void desactiverCompte(String email) {
        Optional<Compte> compteOpt = compteRepo.findById(email);
        if (!compteOpt.isPresent()) {
            throw new IllegalArgumentException("Compte introuvable : " + email);
        }

        Compte compte = compteOpt.get();
        compte.setEnabled(false);
        compteRepo.save(compte);
    }

    // ============= SUPPRIMER UN COMPTE =============
    /**
     * Supprime complètement un compte et toutes ses données associées
     */
    public void supprimerCompte(String email) {
        Optional<Compte> compteOpt = compteRepo.findById(email);
        if (!compteOpt.isPresent()) {
            throw new IllegalArgumentException("Compte introuvable : " + email);
        }

        // Supprimer les données associées selon les rôles
        List<RoleType> roles = new java.util.ArrayList<>();
        authoritiesRepo.findByCompteEmail(email).forEach(auth -> roles.add(auth.getAuthority()));

        if (roles.contains(RoleType.ROLE_DIRECTOR)) {
            directorRepo.deleteById(email);
        } else if (roles.contains(RoleType.ROLE_PARENT)) {
            parentRepo.deleteById(email);
        }

        // Supprimer les autorités et le compte
        authoritiesRepo.deleteByCompteEmail(email);
        compteRepo.deleteById(email);
    }

    // ============= CHANGER LE MOT DE PASSE =============
    /**
     * Change le mot de passe d'un compte
     */
    public void changerMotDePasse(String email, String nouveauPassword) {
        Optional<Compte> compteOpt = compteRepo.findById(email);
        if (!compteOpt.isPresent()) {
            throw new IllegalArgumentException("Compte introuvable : " + email);
        }

        Compte compte = compteOpt.get();
        compte.setPassword(passwordEncoder.encode(nouveauPassword));
        compteRepo.save(compte);
    }

    /**
     * Change le mot de passe après vérification de l'ancien mot de passe
     */
    public boolean changerMotDePasseSecurise(String email, String ancienPassword, String nouveauPassword) {
        Optional<Compte> compteOpt = compteRepo.findById(email);
        if (!compteOpt.isPresent()) {
            return false;
        }

        Compte compte = compteOpt.get();

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(ancienPassword, compte.getPassword())) {
            return false;
        }

        // Changer le mot de passe
        compte.setPassword(passwordEncoder.encode(nouveauPassword));
        compteRepo.save(compte);
        return true;
    }

    // ============= RÉCUPÉRER DES COMPTES =============
    /**
     * Récupère tous les comptes
     */
    public List<Compte> getAllComptes() {
        return (List<Compte>) compteRepo.findAll();
    }

    /**
     * Récupère un compte par email
     */
    public Optional<Compte> getCompteByEmail(String email) {
        return compteRepo.findById(email);
    }

    /**
     * Vérifie si un compte existe
     */
    public boolean compteExists(String email) {
        return compteRepo.existsById(email);
    }

    // ============= GÉRER LES AUTORITÉS =============
    /**
     * Crée ou met à jour l'autorité d'un compte
     */
    public void setCompteAuthority(String email, String authority) {
        Optional<Compte> compteOpt = compteRepo.findById(email);
        if (!compteOpt.isPresent()) {
            throw new IllegalArgumentException("Compte introuvable : " + email);
        }

        Compte compte = compteOpt.get();
        RoleType roleType = RoleType.fromLegacy(authority);

        // Supprimer les anciens rôles
        authoritiesRepo.deleteByCompteEmail(email);

        // Créer la nouvelle autorité
        Authorities auth = new Authorities(compte, roleType);
        authoritiesRepo.save(auth);
    }

    /**
     * Récupère l'autorité d'un compte (le premier rôle si plusieurs)
     */
    public Authorities getCompteAuthority(String email) {
        List<Authorities> authorities = authoritiesRepo.findByCompteEmail(email);
        return authorities.isEmpty() ? null : authorities.get(0);
    }

    // ============= CONSTRUIRE LES DONNÉES D'AFFICHAGE =============
    /**
     * Crée un objet CompteOwner à partir d'un compte pour l'affichage
     * Centralise la logique qui était dupliquée dans CompteController
     */
    public CompteOwner buildCompteOwner(Compte compte) {
        CompteOwner owner = new CompteOwner();

        // Récupérer le rôle principal de l'utilisateur
        List<Authorities> authorities = authoritiesRepo.findByCompteEmail(compte.getEmail());
        String roleStr = "";
        if (!authorities.isEmpty()) {
            roleStr = authorities.get(0).getAuthority().getLegacyName();
        }
        owner.setType(roleStr);

        if ("Admin".equals(roleStr)) {
            owner.setNom("");
            owner.setPrenom("");
        } else if ("Parent".equals(roleStr)) {
            Optional<Parent> parent = parentRepo.findById(compte.getEmail());
            if (parent.isPresent()) {
                owner.setNom(parent.get().getNom());
                owner.setPrenom(parent.get().getPrenom());
            }
        } else if ("Kindergarten Director".equals(roleStr)) {
            Optional<Director> director = directorRepo.findById(compte.getEmail());
            if (director.isPresent()) {
                owner.setNom(director.get().getNom());
                owner.setPrenom(director.get().getPrenom());
            }
        }

        return owner;
    }

    // ============= UTILITAIRES DE VALIDATION =============
    /**
     * Valide les identifiants de connexion
     */
    public boolean validateCredentials(String email, String rawPassword) {
        Optional<Compte> compteOpt = compteRepo.findById(email);
        if (!compteOpt.isPresent()) {
            return false;
        }

        return passwordEncoder.matches(rawPassword, compteOpt.get().getPassword());
    }
}
