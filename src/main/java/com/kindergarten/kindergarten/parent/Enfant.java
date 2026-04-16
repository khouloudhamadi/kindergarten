package com.kindergarten.kindergarten.parent;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Enfant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nom;
    private String prenom;
    private String datenais;
    private String sexe;
    private String etatsante;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Parent parent;

    public Enfant() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
     * @return String return the datenais
     */
    public String getDatenais() {
        return datenais;
    }

    /**
     * @param datenais the datenais to set
     */
    public void setDatenais(String datenais) {
        this.datenais = datenais;
    }

    /**
     * @return String return the sexe
     */
    public String getSexe() {
        return sexe;
    }

    /**
     * @param sexe the sexe to set
     */
    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    /**
     * @return String return the etatsante
     */
    public String getEtatsante() {
        return etatsante;
    }

    /**
     * @param etatsante the etatsante to set
     */
    public void setEtatsante(String etatsante) {
        this.etatsante = etatsante;
    }

    /**
     * @return Parent return the parent
     */
    public Parent getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Parent parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return this.nom + " " + this.prenom;
    }

}
