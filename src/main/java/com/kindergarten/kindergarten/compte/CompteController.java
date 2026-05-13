package com.kindergarten.kindergarten.compte;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * CompteController - GRASP Controller Pattern
 *
 * Responsabilités : 1. Afficher la liste des comptes (Admin seulement) 2.
 * Afficher le formulaire de création de compte 3. Afficher les permissions d'un
 * compte 4. Sauvegarder les modifications d'un compte 5. Supprimer un compte
 *
 * Délégation : - Activation/Désactivation → AuthController + AuthService -
 * Changement de mot de passe → AuthController + AuthService - Création de
 * compte → AuthController + AuthService
 */
@Controller
public class CompteController {

    @Autowired
    private CompteService compteService;

    @Autowired
    private AuthService authService;

    @GetMapping("/compte")
    public String listComptes(Principal principal, Model m) {
        Compte currentuser = compteService.getCurrentUser(principal);
        List<Compte> listcomptes = compteService.getAllComptes();
        m.addAttribute("currentuser", currentuser);
        m.addAttribute("listcomptes", listcomptes);
        return "/compte/index";
    }

    @GetMapping("/compte/new")
    public String showFormCompte(Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        Compte compte = new Compte();
        model.addAttribute("currentuser", currentuser);
        model.addAttribute("compte", compte);

        return "/compte/formCompte";
    }

    @GetMapping("/compte/setperms/{email}")
    public String setPermCompte(@PathVariable("email") String email, Principal principal, Model model) {
        Compte currentuser = compteService.getCurrentUser(principal);

        Compte compte = compteService.getCompteByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable"));
        CompteOwner cpt_owner = compteService.buildCompteOwner(compte);

        model.addAttribute("compte", compte);
        model.addAttribute("cpt_owner", cpt_owner);
        model.addAttribute("currentuser", currentuser);
        return "/compte/setperms";
    }

    @PostMapping("/compte/save")
    public String saveCompte(Compte cpt) {
        compteService.saveCompteWithOldPassword(cpt);
        return "redirect:/compte";
    }

    @GetMapping("/compte/delete/{email}")
    public String deleteCompte(@PathVariable("email") String email) {
        compteService.supprimerCompte(email);
        return "redirect:/compte";
    }

    /**
     * Active un compte (Admin seulement) - Pattern GRASP Controller
     *
     * 1. Récupère l'utilisateur courant 2. Vérifie les droits (Admin) 3.
     * Délègue à AuthService 4. Retourne la vue
     */
    @GetMapping("/compte/activer/{email}")
    public String activerCompte(
            @PathVariable("email") String email,
            Principal principal) {

        // Vérifier les droits d'accès (Admin seulement)
        Compte currentUser = authService.getCurrentUser(principal);
        if (!authService.isAdmin(currentUser)) {
            return "/error/accessDenied";
        }

        // Déléguer au service métier
        try {
            authService.activerCompte(email);
        } catch (IllegalArgumentException ex) {
            System.err.println("Erreur lors de l'activation du compte : " + ex.getMessage());
            return "redirect:/compte?error=" + ex.getMessage();
        }
        return "redirect:/compte";
    }

    /**
     * Désactive un compte (Admin seulement) - Pattern GRASP Controller
     *
     * 1. Récupère l'utilisateur courant 2. Vérifie les droits (Admin) 3.
     * Délègue à AuthService 4. Retourne la vue
     */
    @GetMapping("/compte/desactiver/{email}")
    public String desactiverCompte(
            @PathVariable("email") String email,
            Principal principal) {

        // Vérifier les droits d'accès (Admin seulement)
        Compte currentUser = authService.getCurrentUser(principal);
        if (!authService.isAdmin(currentUser)) {
            return "/error/accessDenied";
        }

        // Déléguer au service métier
        try {
            authService.desactiverCompte(email);
        } catch (IllegalArgumentException ex) {
            System.err.println("Erreur lors de la désactivation du compte : " + ex.getMessage());
            return "redirect:/compte?error=" + ex.getMessage();
        }
        return "redirect:/compte";
    }
}
