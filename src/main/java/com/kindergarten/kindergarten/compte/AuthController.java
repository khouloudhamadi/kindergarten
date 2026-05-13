package com.kindergarten.kindergarten.compte;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kindergarten.kindergarten.director.DirectorInfo;
import com.kindergarten.kindergarten.parent.ParentInfo;

/**
 * GRASP Controller pour l'authentification
 *
 * Responsabilités UNIQUEMENT : 1. Recevoir la requête HTTP 2. Vérifier
 * l'identité (Principal) et les droits d'accès 3. Déléguer au service métier
 * (AuthService) 4. Retourner la vue ou redirection
 *
 * Le controller ne contient AUCUNE logique métier. Toute la logique est dans
 * AuthService.
 *
 * Bénéfices : - Low Coupling : le contrôleur ne dépend que du service - High
 * Cohesion : le contrôleur ne fait qu'une chose - Testabilité : facile de
 * mocker le service pour les tests - Maintenabilité : logique métier en un seul
 * endroit
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Crée un nouveau compte directeur Cette opération ne nécessite pas
     * d'authentification (phase d'inscription)
     *
     * @param dinfo Informations du directeur
     * @return Redirection vers la page d'accueil
     */
    @PostMapping("/director/register")
    public String registerDirector(DirectorInfo dinfo) {
        // Délégation complète au service
        authService.creerCompte(
                dinfo.getEmail(),
                dinfo.getPassword(),
                "Kindergarten Director"
        );
        return "redirect:/";
    }

    /**
     * Crée un nouveau compte parent Cette opération ne nécessite pas
     * d'authentification (phase d'inscription)
     *
     * @param pinf Informations du parent
     * @return Redirection vers la page d'accueil
     */
    @PostMapping("/parent/register")
    public String registerParent(ParentInfo pinf) {
        // Délégation complète au service
        authService.creerCompte(
                pinf.getEmail(),
                pinf.getPassword(),
                "Parent"
        );
        return "redirect:/";
    }

    /**
     * Active un compte (Admin seulement) Vérifie que l'utilisateur courant est
     * Admin avant d'activer
     *
     * @param email Email du compte à activer
     * @param principal L'utilisateur actuellement connecté
     * @return Redirection vers la page des comptes, ou page d'erreur si accès
     * refusé
     */
    @GetMapping("/activate/{email}")
    public String activateAccount(
            @PathVariable String email,
            Principal principal) {

        // 1. Récupérer l'utilisateur courant
        Compte currentUser = authService.getCurrentUser(principal);

        // 2. Vérifier les droits d'accès
        if (!authService.isAdmin(currentUser)) {
            return "/error/accessDenied";
        }

        // 3. Déléguer au service métier
        try {
            authService.activerCompte(email);
        } catch (IllegalArgumentException ex) {
            // Compte non trouvé
            return "redirect:/compte?error=" + ex.getMessage();
        }

        // 4. Retourner la vue
        return "redirect:/compte";
    }

    /**
     * Désactive un compte (Admin seulement) Vérifie que l'utilisateur courant
     * est Admin avant de désactiver
     *
     * @param email Email du compte à désactiver
     * @param principal L'utilisateur actuellement connecté
     * @return Redirection vers la page des comptes, ou page d'erreur si accès
     * refusé
     */
    @GetMapping("/deactivate/{email}")
    public String deactivateAccount(
            @PathVariable String email,
            Principal principal) {

        // 1. Récupérer l'utilisateur courant
        Compte currentUser = authService.getCurrentUser(principal);

        // 2. Vérifier les droits d'accès
        if (!authService.isAdmin(currentUser)) {
            return "/error/accessDenied";
        }

        // 3. Déléguer au service métier
        try {
            authService.desactiverCompte(email);
        } catch (IllegalArgumentException ex) {
            // Compte non trouvé
            return "redirect:/compte?error=" + ex.getMessage();
        }

        // 4. Retourner la vue
        return "redirect:/compte";
    }

    /**
     * Change le mot de passe de l'utilisateur actuellement connecté
     * L'utilisateur peut changer son propre mot de passe
     *
     * @param principal L'utilisateur actuellement connecté
     * @param newPassword Le nouveau mot de passe
     * @return Redirection vers la page de profil
     */
    @PostMapping("/change-password")
    public String changePassword(
            Principal principal,
            @RequestParam("newPassword") String newPassword) {

        // 1. Récupérer l'utilisateur courant
        Compte currentUser = authService.getCurrentUser(principal);

        // 2. Vérifier l'authentification
        if (currentUser == null) {
            return "/error/accessDenied";
        }

        // 3. Vérifier que le mot de passe n'est pas vide
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "redirect:/compte?error=Password cannot be empty";
        }

        // 4. Déléguer au service métier
        try {
            authService.changerMotDePasse(currentUser.getEmail(), newPassword);
        } catch (IllegalArgumentException ex) {
            return "redirect:/compte?error=" + ex.getMessage();
        }

        // 5. Retourner la vue
        return "redirect:/compte?success=Password changed successfully";
    }

    /**
     * Endpoint pour tester l'accès restreint (Admin seulement) Retourne les
     * informations du compte courant si l'utilisateur est Admin
     *
     * @param principal L'utilisateur actuellement connecté
     * @return Les informations du compte ou page d'erreur
     */
    @GetMapping("/admin-only")
    public String adminOnly(Principal principal) {
        Compte currentUser = authService.getCurrentUser(principal);

        if (!authService.isAdmin(currentUser)) {
            return "/error/accessDenied";
        }

        return "admin/dashboard";
    }
}
