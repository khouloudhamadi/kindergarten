package com.kindergarten.kindergarten.compte;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RoleService - SOLID SRP
 *
 * Responsabilité unique : gérer les rôles et autorités (attribution, retrait,
 * vérification)
 *
 * NE gère PAS le mot de passe ni l'activation → AccountService s'en charge
 *
 * Une seule raison de changer : si la politique de gestion des rôles évolue
 */
@Service
public class RoleService {

    @Autowired
    private AuthoritiesRepo authoritiesRepo;

    @Autowired
    private CompteRepo compteRepo;

    // ========== ATTRIBUER UN RÔLE ==========
    /**
     * Attribue un rôle à un utilisateur Si l'utilisateur a déjà ce rôle, ne
     * fait rien (pas de doublons)
     *
     * @param email l'adresse email
     * @param role le rôle à attribuer
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void attribuerRole(String email, RoleType role) {
        Compte compte = compteRepo.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable : " + email));

        // Vérifier que l'utilisateur n'a pas déjà ce rôle
        boolean dejaPresent = authoritiesRepo.existsByCompteAndAuthority(compte, role);

        if (!dejaPresent) {
            Authorities autorite = new Authorities(compte, role);
            authoritiesRepo.save(autorite);
        }
    }

    // ========== RETIRER UN RÔLE ==========
    /**
     * Retire un rôle à un utilisateur
     *
     * @param email l'adresse email
     * @param role le rôle à retirer
     */
    public void retirerRole(String email, RoleType role) {
        authoritiesRepo.deleteByCompteEmailAndAuthority(email, role);
    }

    /**
     * Retire tous les rôles d'un utilisateur
     *
     * @param email l'adresse email
     */
    public void retirerTousLesRoles(String email) {
        authoritiesRepo.deleteByCompteEmail(email);
    }

    // ========== LISTER LES RÔLES ==========
    /**
     * Liste tous les rôles d'un utilisateur
     *
     * @param email l'adresse email
     * @return liste des RoleType attribués
     */
    public List<RoleType> obtenirRoles(String email) {
        return authoritiesRepo.findByCompteEmail(email)
                .stream()
                .map(Authorities::getAuthority)
                .toList();
    }

    /**
     * Compte combien de rôles un utilisateur a
     *
     * @param email l'adresse email
     * @return le nombre de rôles
     */
    public int compterRoles(String email) {
        return (int) authoritiesRepo.countByCompteEmail(email);
    }

    // ========== VÉRIFIER UN RÔLE ==========
    /**
     * Vérifie si un utilisateur a un rôle spécifique
     *
     * @param email l'adresse email
     * @param role le rôle à vérifier
     * @return true si l'utilisateur a ce rôle
     */
    public boolean aLe(String email, RoleType role) {
        return authoritiesRepo.existsByCompteEmailAndAuthority(email, role);
    }

    /**
     * Vérifie si un utilisateur a TOUS les rôles spécifiés
     *
     * @param email l'adresse email
     * @param roles les rôles à vérifier
     * @return true si l'utilisateur a tous les rôles
     */
    public boolean aLes(String email, RoleType... roles) {
        for (RoleType role : roles) {
            if (!aLe(email, role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Vérifie si un utilisateur a AU MOINS UN des rôles spécifiés
     *
     * @param email l'adresse email
     * @param roles les rôles à vérifier
     * @return true si l'utilisateur a au moins un des rôles
     */
    public boolean aAuMoinsUn(String email, RoleType... roles) {
        for (RoleType role : roles) {
            if (aLe(email, role)) {
                return true;
            }
        }
        return false;
    }

    // ========== UTILITAIRES ==========
    /**
     * Change le rôle principal d'un utilisateur (retire tous les autres) Utile
     * pour changer le type de compte
     *
     * @param email l'adresse email
     * @param nouveauRole le nouveau rôle
     */
    public void changerRolePrincipal(String email, RoleType nouveauRole) {
        retirerTousLesRoles(email);
        attribuerRole(email, nouveauRole);
    }

    /**
     * Obtient le premier rôle d'un utilisateur (utile si l'utilisateur n'a
     * qu'un seul rôle)
     *
     * @param email l'adresse email
     * @return le premier RoleType, ou null si pas de rôle
     */
    public RoleType obtenirRolePrincipal(String email) {
        return obtenirRoles(email).stream().findFirst().orElse(null);
    }

}
