package com.kindergarten.kindergarten.compte;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification - Pattern GRASP Controller (Façade)
 *
 * Centralise TOUTE la logique métier d'authentification en déléguant à : -
 * AccountService : gestion des comptes (création, activation, mot de passe) -
 * RoleService : gestion des rôles
 *
 * Les contrôleurs appelleront UNIQUEMENT les méthodes de ce service et ne
 * feront aucune logique métier eux-mêmes.
 */
@Service
public class AuthService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleService roleService;

    /**
     * Crée un nouveau compte désactivé (utilisé pour registration
     * director/parent) Le compte reste inactif jusqu'à son activation par un
     * admin
     *
     * @param email Email du compte
     * @param password Mot de passe en clair (sera chiffré)
     * @param type Type de compte (Admin, Kindergarten Director, Parent)
     * @return Le compte créé
     */
    public Compte creerCompte(String email, String password, String type) {
        return accountService.creerCompte(email, password, type);
    }

    /**
     * Active un compte et crée automatiquement son autorité Spring Security
     * Applique le principe Low Coupling : le contrôleur ne doit pas s'occuper
     * de créer les autorités.
     *
     * @param email Email du compte à activer
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void activerCompte(String email) {
        accountService.activerCompte(email);
    }

    /**
     * Désactive un compte (l'utilisateur ne peut plus se connecter)
     *
     * @param email Email du compte à désactiver
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void desactiverCompte(String email) {
        accountService.desactiverCompte(email);
    }

    /**
     * Change le mot de passe d'un compte Le nouveau mot de passe est
     * automatiquement chiffré
     *
     * @param email Email du compte
     * @param nouveauMdp Nouveau mot de passe en clair
     * @throws IllegalArgumentException si le compte n'existe pas
     */
    public void changerMotDePasse(String email, String nouveauMdp) {
        accountService.changerMotDePasse(email, nouveauMdp);
    }

    /**
     * Récupère l'utilisateur actuellement connecté Remplace le code dupliqué
     * dans chaque contrôleur
     *
     * @param principal Le Principal Spring Security
     * @return Le compte correspondant, ou null si non connecté
     */
    public Compte getCurrentUser(Principal principal) {
        return accountService.getCurrentUser(principal);
    }

    /**
     * Récupère un compte par son email
     *
     * @param email Email du compte
     * @return Optional contenant le compte s'il existe
     */
    public Optional<Compte> getCompteByEmail(String email) {
        return accountService.getCompteByEmail(email);
    }

    /**
     * Vérifie si un utilisateur a un rôle spécifique Utilisé par le contrôleur
     * pour les vérifications de droits d'accès
     *
     * @param compte Le compte à vérifier
     * @param role Le rôle attendu (e.g., "Admin", "Kindergarten Director",
     * "Parent")
     * @return true si l'utilisateur a le rôle, false sinon
     */
    public boolean hasRole(Compte compte, String role) {
        return accountService.hasRole(compte, role);
    }

    /**
     * Vérifie si un utilisateur est un Admin
     *
     * @param compte Le compte à vérifier
     * @return true si c'est un admin, false sinon
     */
    public boolean isAdmin(Compte compte) {
        return accountService.isAdmin(compte);
    }

    /**
     * Vérifie si un utilisateur est un Directeur (Kindergarten Director)
     *
     * @param compte Le compte à vérifier
     * @return true si c'est un directeur, false sinon
     */
    public boolean isDirector(Compte compte) {
        return accountService.isDirector(compte);
    }

    /**
     * Vérifie si un utilisateur est un Parent
     *
     * @param compte Le compte à vérifier
     * @return true si c'est un parent, false sinon
     */
    public boolean isParent(Compte compte) {
        return accountService.isParent(compte);
    }
}
