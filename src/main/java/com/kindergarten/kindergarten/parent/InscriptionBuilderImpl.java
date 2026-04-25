package com.kindergarten.kindergarten.parent;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import org.springframework.stereotype.Component;

/**
 * GoF - Builder Pattern (implémentation concrète)
 * Construction fluide d'une Inscription étape par étape.
 */
@Component // ← AJOUT : permet à Spring de l'injecter via l'interface
public class InscriptionBuilderImpl implements InscriptionBuilder {

    private Enfant enfant;
    private KinderGarten kindergarten;
    private Parent parent;
    private String anneeScolaire;
    private String classLevel;
    private String date;

    @Override
    public InscriptionBuilder withEnfant(Enfant enfant) {
        this.enfant = enfant;
        return this;
    }

    @Override
    public InscriptionBuilder withKindergarten(KinderGarten kindergarten) {
        this.kindergarten = kindergarten;
        return this;
    }

    @Override
    public InscriptionBuilder withParent(Parent parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public InscriptionBuilder withAnneeScolaire(String anneeScolaire) {
        this.anneeScolaire = anneeScolaire;
        return this;
    }

    @Override
    public InscriptionBuilder withClassLevel(String classLevel) {
        this.classLevel = classLevel;
        return this;
    }

    @Override
    public InscriptionBuilder withDate(String date) {
        this.date = date;
        return this;
    }

    /**
     * Build final avec validation des champs obligatoires.
     * OCL: un enfant ne peut avoir qu'une inscription active par an (vérifiée dans
     * InscriptionValidator).
     */
    @Override
    public Inscription build() {
        if (enfant == null)
            throw new IllegalStateException("L'enfant est obligatoire.");
        if (kindergarten == null)
            throw new IllegalStateException("Le jardin d'enfants est obligatoire.");
        if (parent == null)
            throw new IllegalStateException("Le parent est obligatoire.");
        if (anneeScolaire == null)
            throw new IllegalStateException("L'année scolaire est obligatoire.");
        if (classLevel == null)
            throw new IllegalStateException("Le niveau de classe est obligatoire.");
        if (date == null)
            throw new IllegalStateException("La date est obligatoire.");

        Inscription inscription = new Inscription();
        inscription.setEnfant(enfant);
        inscription.setKindergarten(kindergarten);
        inscription.setParent(parent);
        inscription.setAnneescolaire(anneeScolaire);
        inscription.setClass_level(classLevel);
        inscription.setDate(date);
        inscription.setValid(false);

        return inscription;
    }
}