package com.kindergarten.kindergarten.parent;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String date;
    private String anneescolaire;
    private String class_level;
    private boolean valid = false;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private KinderGarten kindergarten;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Parent parent;

    @OneToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Enfant enfant;

    public Inscription() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return String return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return String return the anneescolaire
     */
    public String getAnneescolaire() {
        return anneescolaire;
    }

    /**
     * @param anneescolaire the anneescolaire to set
     */
    public void setAnneescolaire(String anneescolaire) {
        this.anneescolaire = anneescolaire;
    }

    /**
     * @return KinderGarten return the kindergarten
     */
    public KinderGarten getKindergarten() {
        return kindergarten;
    }

    /**
     * @param kindergarten the kindergarten to set
     */
    public void setKindergarten(KinderGarten kindergarten) {
        this.kindergarten = kindergarten;
    }

    /**
     * @return Enfant return the enfant
     */
    public Enfant getEnfant() {
        return enfant;
    }

    /**
     * @param enfant the enfant to set
     */
    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
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

    /**
     * @return boolean return the valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @param valid the valid to set
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return String return the class_level
     */
    public String getClass_level() {
        return class_level;
    }

    /**
     * @param class_level the class_level to set
     */
    public void setClass_level(String class_level) {
        this.class_level = class_level;
    }

}