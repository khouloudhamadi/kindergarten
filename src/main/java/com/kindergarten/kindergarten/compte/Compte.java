package com.kindergarten.kindergarten.compte;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Compte Entity - SOLID SRP
 *
 * Responsabilité unique : gérer l'identité et l'état d'activation d'un
 * utilisateur
 *
 * ✓ Contient : email, password, enabled ✗ Ne contient PAS : type (géré par
 * Authorities), confirm_password (géré par DTO)
 *
 * Une seule raison de changer : si la logique d'identité évolue
 */
@Entity
@Table(name = "users")
public class Compte {

    @Id
    @Column(name = "username", nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean enabled = false;

    public Compte() {
    }

    /**
     * @return String return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return String return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return boolean return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
