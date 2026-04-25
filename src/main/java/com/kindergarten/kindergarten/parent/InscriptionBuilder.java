package com.kindergarten.kindergarten.parent;

/**
 * GoF - Builder Pattern
 * Permet de construire une Inscription étape par étape.
 * Respecte le principe de faible couplage : le builder ne connaît pas Payment
 * ni KinderGarten directement.
 */
public interface InscriptionBuilder {

    InscriptionBuilder withEnfant(Enfant enfant);

    InscriptionBuilder withKindergarten(com.kindergarten.kindergarten.kindergarten.KinderGarten kindergarten);

    InscriptionBuilder withParent(Parent parent);

    InscriptionBuilder withAnneeScolaire(String anneeScolaire);

    InscriptionBuilder withClassLevel(String classLevel);

    InscriptionBuilder withDate(String date);

    /**
     * Construit et retourne l'Inscription finale.
     * Lance une IllegalStateException si des champs obligatoires sont manquants.
     */
    Inscription build();
}