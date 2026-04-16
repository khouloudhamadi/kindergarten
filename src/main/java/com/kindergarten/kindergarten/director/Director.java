package com.kindergarten.kindergarten.director;

import java.util.ArrayList;
import java.util.List;

import com.kindergarten.kindergarten.compte.Compte;
import com.kindergarten.kindergarten.kindergarten.KinderGarten;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Director {

    @Id
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false)
    private String tel;

    @OneToOne(cascade = CascadeType.ALL)
    private Compte compte;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<KinderGarten> kindergartens = new ArrayList<>();

    public Director() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return String return the prenom
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * @param prenom the prenom to set
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /**
     * @return String return the nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * @param nom the nom to set
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * @return String return the adresse
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * @param adresse the adresse to set
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * @return String return the tel
     */
    public String getTel() {
        return tel;
    }

    /**
     * @param tel the tel to set
     */
    public void setTel(String tel) {
        this.tel = tel;
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
     * @return List<KinderGarten> return the kindergartens
     */
    public List<KinderGarten> getKindergartens() {
        return kindergartens;
    }

    /**
     * @param kindergartens the kindergartens to set
     */
    public void setKindergartens(List<KinderGarten> kindergartens) {
        this.kindergartens = kindergartens;
    }

}