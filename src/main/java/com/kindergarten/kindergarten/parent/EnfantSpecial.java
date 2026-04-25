package com.kindergarten.kindergarten.parent;

import jakarta.persistence.Entity;

/**
 * SOLID — OCP (Open/Closed Principle)
 * Enfant est fermé à la modification mais ouvert à l'extension.
 * EnfantSpecial étend Enfant sans toucher à la classe de base.
 */
@Entity
public class EnfantSpecial extends Enfant {

    private String typeHandicap;
    private String suiviMedical;

    public EnfantSpecial() {
        super();
    }

    public String getTypeHandicap() {
        return typeHandicap;
    }

    public void setTypeHandicap(String typeHandicap) {
        this.typeHandicap = typeHandicap;
    }

    public String getSuiviMedical() {
        return suiviMedical;
    }

    public void setSuiviMedical(String suiviMedical) {
        this.suiviMedical = suiviMedical;
    }
}