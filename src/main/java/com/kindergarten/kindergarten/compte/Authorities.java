package com.kindergarten.kindergarten.compte;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Authorities Entity - SOLID SRP
 *
 * Responsabilité unique : gérer la relation Compte ↔ Rôle
 *
 * Changements majeurs : ✓ id : Long @GeneratedValue → permet plusieurs rôles
 * par utilisateur ✓ compte : @ManyToOne → lien JPA explicite (plus de String
 * username) ✓ authority : RoleType enum → valeurs contraintes (pas de String
 * libre)
 *
 * Une seule raison de changer : si la politique de gestion des rôles évolue
 */
@Entity
@Table(name = "authorities")
public class Authorities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    private Compte compte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType authority;

    public Authorities() {
    }

    /**
     * Constructeur avec paramètres
     */
    public Authorities(Compte compte, RoleType authority) {
        this.compte = compte;
        this.authority = authority;
    }

    /**
     * @return Long return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return Compte return the compte
     */
    public Compte getCompte() {
        return compte;
    }

    /**
     * @param compte the compte to set
     */
    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    /**
     * @return RoleType return the authority
     */
    public RoleType getAuthority() {
        return authority;
    }

    /**
     * @param authority the authority to set
     */
    public void setAuthority(RoleType authority) {
        this.authority = authority;
    }

    /**
     * Méthode utilitaire pour Spring Security JdbcUserDetailsManager Retourne
     * l'email du compte associé
     *
     * @return String l'email (username dans Spring Security)
     */
    public String getUsername() {
        return compte != null ? compte.getEmail() : null;
    }

}
