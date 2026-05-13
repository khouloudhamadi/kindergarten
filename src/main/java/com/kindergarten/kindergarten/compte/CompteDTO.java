package com.kindergarten.kindergarten.compte;

/**
 * CompteDTO - Data Transfer Object - SOLID SRP
 *
 * Responsabilité unique : transporter et valider les données du formulaire
 * d'inscription Jamais persistée en base de données.
 *
 * ✓ Contient : email, password, confirmPassword, type ✗ Pas de id (technique) ✗
 * Pas de enabled (défaut à false)
 *
 * Flux : 1. Utilisateur remplit le formulaire HTML 2. Spring mappe vers
 * CompteDTO 3. Valider confirmPassword == password 4. Créer Compte (sans
 * confirmPassword ni type) 5. Créer Authorities avec le type converti en
 * RoleType enum
 *
 * Une seule raison de changer : si le formulaire d'inscription évolue
 */
public class CompteDTO {

    private String email;
    private String password;
    private String confirmPassword;  // ← validation UI uniquement
    private String type;              // ← converti en RoleType après validation

    public CompteDTO() {
    }

    public CompteDTO(String email, String password, String confirmPassword, String type) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.type = type;
    }

    /**
     * Valide que les mots de passe correspondent
     *
     * @return true si password == confirmPassword
     */
    public boolean isPasswordValid() {
        if (this.password == null || this.confirmPassword == null) {
            return false;
        }
        return this.password.equals(this.confirmPassword);
    }

    /**
     * Valide que le type de compte est valide
     *
     * @return true si le type peut être converti en RoleType
     */
    public boolean isTypeValid() {
        try {
            RoleType.fromLegacy(this.type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Convertit le type en RoleType
     *
     * @return RoleType correspondant
     * @throws IllegalArgumentException si le type n'existe pas
     */
    public RoleType getTypeAsRoleType() {
        return RoleType.fromLegacy(this.type);
    }

    // ========== GETTERS / SETTERS ==========
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
