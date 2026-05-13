package com.kindergarten.kindergarten.compte;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AccountService - SOLID SRP
 *
 * Responsabilité unique : gérer le cycle de vie d'un compte (identité, mot de
 * passe, activation) ET les rôles associés
 *
 * Une seule raison de changer : si la logique de gestion des comptes évolue
 */
@Service
public class AccountService {

    @Autowired
    private CompteRepo compteRepo;

    @Autowired
    private AuthoritiesRepo authoritiesRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    // ========== CRÉER UN COMPTE ==========
    /**
     * Crée un nouveau compte avec mot de passe chiffré et rôle associé
     *
     * @param email l'adresse email de l'utilisateur
     * @param password le mot de passe en clair (sera chiffré)
     * @param type le type de compte (Admin, Director, Parent) - facultatif
     * @return le compte créé (enabled=false par défaut)
     */
    public Compte creerCompte(String email, String password, String type) {
        Compte compte = new Compte();
        compte.setEmail(email);
        compte.setPassword(passwordEncoder.encode(password));
        compte.setEnabled(false);  // Attente d'activation
        Compte savedCompte = compteRepo.save(compte);

        // Attribuer le rôle correspondant si spécifié
        if (type != null && !type.isBlank()) {
            roleService.attribuerRole(email, RoleType.fromLegacy(type));
        }

        return savedCompte;
    }

    /**
     * Crée un nouveau compte sans rôle (surcharge pour compatibilité)
     *
     * @param email l'adresse email de l'utilisateur
     * @param password le mot de passe en clair (sera chiffré)
     * @return le compte créé (enabled=false par défaut)
     */
    public Compte creerCompte(String email, String password) {
        return creerCompte(email, password, null);
    }

    // ========== ACTIVER / DÉSACTIVER ==========
    /**
     * Active un compte (enabled=true)
     *
     * @param email l'adresse email du compte
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void activerCompte(String email) {
        Compte compte = compteRepo.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable : " + email));
        compte.setEnabled(true);
        compteRepo.save(compte);
    }

    /**
     * Désactive un compte (enabled=false)
     *
     * @param email l'adresse email du compte
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void desactiverCompte(String email) {
        Compte compte = compteRepo.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable : " + email));
        compte.setEnabled(false);
        compteRepo.save(compte);
    }

    // Alias pour compatibilité rétroactive
    public void activer(String email) {
        activerCompte(email);
    }

    public void desactiver(String email) {
        desactiverCompte(email);
    }

    // ========== CHANGER LE MOT DE PASSE ==========
    /**
     * Change le mot de passe d'un compte
     *
     * @param email l'adresse email du compte
     * @param nouveauMotDePasse le nouveau mot de passe (sera chiffré)
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void changerMotDePasse(String email, String nouveauMotDePasse) {
        Compte compte = compteRepo.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable : " + email));
        compte.setPassword(passwordEncoder.encode(nouveauMotDePasse));
        compteRepo.save(compte);
    }

    /**
     * Vérifie si un mot de passe est correct (comparaison avec le hash)
     *
     * @param email l'adresse email du compte
     * @param motDePasse le mot de passe en clair
     * @return true si le mot de passe est correct
     */
    public boolean verifierMotDePasse(String email, String motDePasse) {
        Compte compte = compteRepo.findById(email).orElse(null);
        if (compte == null) {
            return false;
        }
        return passwordEncoder.matches(motDePasse, compte.getPassword());
    }

    // ========== RÉCUPÉRER / SAUVEGARDER ==========
    /**
     * Récupère le compte de l'utilisateur actuellement connecté
     *
     * @param principal le Principal Spring Security
     * @return le compte, ou null si non connecté
     */
    public Compte getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return compteRepo.findById(principal.getName()).orElse(null);
    }

    /**
     * Récupère un compte par email
     *
     * @param email l'adresse email
     * @return le compte, ou Optional.empty() si introuvable
     */
    public Optional<Compte> obtenirCompte(String email) {
        return compteRepo.findById(email);
    }

    /**
     * Récupère un compte par email (alias pour les tests)
     *
     * @param email l'adresse email
     * @return le compte, ou Optional.empty() si introuvable
     */
    public Optional<Compte> getCompteByEmail(String email) {
        return obtenirCompte(email);
    }

    /**
     * Sauvegarde un compte
     *
     * @param compte le compte à sauvegarder
     * @return le compte sauvegardé
     */
    public Compte sauvegarder(Compte compte) {
        return compteRepo.save(compte);
    }

    /**
     * Vérifie si un compte existe
     *
     * @param email l'adresse email
     * @return true si le compte existe
     */
    public boolean exists(String email) {
        return compteRepo.existsById(email);
    }

    /**
     * Supprime un compte
     *
     * @param email l'adresse email du compte à supprimer
     */
    public void supprimer(String email) {
        compteRepo.deleteById(email);
    }

    // ========== VÉRIFIER LES RÔLES ==========
    /**
     * Vérifie si un compte a un rôle spécifique
     *
     * @param compte le compte à vérifier
     * @param role le rôle attendu (e.g., "Admin", "Kindergarten Director",
     * "Parent")
     * @return true si l'utilisateur a le rôle, false sinon
     */
    public boolean hasRole(Compte compte, String role) {
        if (compte == null || role == null || role.isBlank()) {
            return false;
        }
        RoleType roleType = RoleType.fromLegacy(role);
        return roleService.aLe(compte.getEmail(), roleType);
    }

    /**
     * Vérifie si un compte est un Admin
     *
     * @param compte le compte à vérifier
     * @return true si c'est un admin, false sinon
     */
    public boolean isAdmin(Compte compte) {
        return hasRole(compte, "Admin");
    }

    /**
     * Vérifie si un compte est un Directeur (Kindergarten Director)
     *
     * @param compte le compte à vérifier
     * @return true si c'est un directeur, false sinon
     */
    public boolean isDirector(Compte compte) {
        return hasRole(compte, "Kindergarten Director");
    }

    /**
     * Vérifie si un compte est un Parent
     *
     * @param compte le compte à vérifier
     * @return true si c'est un parent, false sinon
     */
    public boolean isParent(Compte compte) {
        return hasRole(compte, "Parent");
    }
}
