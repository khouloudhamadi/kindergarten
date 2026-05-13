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
 * passe, activation)
 *
 * NE gère PAS les rôles → RoleService s'en charge
 *
 * Une seule raison de changer : si la logique de gestion des comptes évolue
 */
@Service
public class AccountService {

    @Autowired
    private CompteRepo compteRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ========== CRÉER UN COMPTE ==========
    /**
     * Crée un nouveau compte avec mot de passe chiffré
     *
     * @param email l'adresse email de l'utilisateur
     * @param password le mot de passe en clair (sera chiffré)
     * @return le compte créé (enabled=false par défaut)
     */
    public Compte creerCompte(String email, String password) {
        Compte compte = new Compte();
        compte.setEmail(email);
        compte.setPassword(passwordEncoder.encode(password));
        compte.setEnabled(false);  // Attente d'activation
        return compteRepo.save(compte);
    }

    // ========== ACTIVER / DÉSACTIVER ==========
    /**
     * Active un compte (enabled=true)
     *
     * @param email l'adresse email du compte
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void activer(String email) {
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
    public void desactiver(String email) {
        Compte compte = compteRepo.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable : " + email));
        compte.setEnabled(false);
        compteRepo.save(compte);
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

}
