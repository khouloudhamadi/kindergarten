package com.kindergarten.kindergarten.compte;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * EXEMPLE - Utilisation de AccountService et RoleService Version corrigée
 */
@RestController
@RequestMapping("/api/auth")
public class SRPExampleController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private RoleService roleService;

    // ================== INSCRIPTION ==================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CompteDTO dto) {

        try {

            if (!dto.isPasswordValid()) {
                return ResponseEntity.badRequest()
                        .body("Les mots de passe ne correspondent pas");
            }

            if (!dto.isTypeValid()) {
                return ResponseEntity.badRequest()
                        .body("Type de compte invalide");
            }

            if (accountService.exists(dto.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Cet email existe déjà");
            }

            // création compte
            accountService.creerCompte(
                    dto.getEmail(),
                    dto.getPassword()
            );

            // attribution rôle
            RoleType roleType = dto.getTypeAsRoleType();
            roleService.attribuerRole(dto.getEmail(), roleType);

            return ResponseEntity.ok("Inscription réussie");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    // ================== ACTIVER ==================
    @PostMapping("/activate/{email}")
    public ResponseEntity<?> activateAccount(@PathVariable String email) {

        try {
            accountService.activer(email);
            return ResponseEntity.ok("Compte activé");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== DESACTIVER ==================
    @PostMapping("/deactivate/{email}")
    public ResponseEntity<?> deactivateAccount(@PathVariable String email) {

        try {
            accountService.desactiver(email);
            return ResponseEntity.ok("Compte désactivé");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== CHANGER PASSWORD ==================
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String email,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword) {

        try {

            if (!accountService.verifierMotDePasse(email, currentPassword)) {
                return ResponseEntity.badRequest()
                        .body("Mot de passe actuel incorrect");
            }

            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest()
                        .body("Les nouveaux mots de passe ne correspondent pas");
            }

            accountService.changerMotDePasse(email, newPassword);

            return ResponseEntity.ok("Mot de passe modifié");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== AJOUT ROLE ==================
    @PostMapping("/roles/{email}/{role}")
    public ResponseEntity<?> assignRole(
            @PathVariable String email,
            @PathVariable String role) {

        try {
            RoleType roleType = RoleType.valueOf(role);
            roleService.attribuerRole(email, roleType);

            return ResponseEntity.ok("Rôle attribué");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Rôle invalide");
        }
    }

    // ================== RETIRER ROLE ==================
    @DeleteMapping("/roles/{email}/{role}")
    public ResponseEntity<?> removeRole(
            @PathVariable String email,
            @PathVariable String role) {

        try {
            RoleType roleType = RoleType.valueOf(role);
            roleService.retirerRole(email, roleType);

            return ResponseEntity.ok("Rôle retiré");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Rôle invalide");
        }
    }

    // ================== CHANGER ROLE PRINCIPAL ==================
    @PutMapping("/roles/{email}/{newRole}")
    public ResponseEntity<?> changeMainRole(
            @PathVariable String email,
            @PathVariable String newRole) {

        try {
            RoleType roleType = RoleType.valueOf(newRole);
            roleService.changerRolePrincipal(email, roleType);

            return ResponseEntity.ok("Rôle modifié");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Rôle invalide");
        }
    }

    // ================== LISTER ROLES ==================
    @GetMapping("/roles/{email}")
    public ResponseEntity<?> getRoles(@PathVariable String email) {

        try {
            return ResponseEntity.ok(
                    roleService.obtenirRoles(email)
            );

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ================== VERIFIER ROLE ==================
    @GetMapping("/roles/{email}/has/{role}")
    public ResponseEntity<?> hasRole(
            @PathVariable String email,
            @PathVariable String role) {

        try {
            RoleType roleType = RoleType.valueOf(role);

            boolean hasRole = roleService.aLe(email, roleType);

            return ResponseEntity.ok(hasRole);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================== SUPPRIMER COMPTE ==================
    @DeleteMapping("/{email}")
    public ResponseEntity<?> deleteAccount(@PathVariable String email) {

        try {

            roleService.retirerTousLesRoles(email);
            accountService.supprimer(email);

            return ResponseEntity.ok("Compte supprimé");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    // ================== UTILISATEUR CONNECTE ==================
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Principal principal) {

        Compte compte = accountService.getCurrentUser(principal);

        if (compte == null) {
            return ResponseEntity.notFound().build();
        }

        List<RoleType> userRoles
                = roleService.obtenirRoles(compte.getEmail());

        CurrentUserDTO dto = new CurrentUserDTO();
        dto.email = compte.getEmail();
        dto.enabled = compte.isEnabled();
        dto.roles = userRoles;

        return ResponseEntity.ok(dto);
    }

    // ================== DTO ==================
    public static class CurrentUserDTO {

        public String email;
        public boolean enabled;
        public List<RoleType> roles;
    }
}
