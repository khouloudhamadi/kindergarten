package com.kindergarten.kindergarten.parent;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;

/**
 * GoF - Builder Director
 * Orchestre la construction d'une Inscription via le builder.
 * Le controller n'a plus besoin de connaître les détails de construction.
 */
public class InscriptionDirector {

    private final InscriptionBuilder builder;

    public InscriptionDirector(InscriptionBuilder builder) {
        this.builder = builder;
    }

    /**
     * Construit une inscription standard (non validée par défaut).
     */
    public Inscription buildInscriptionStandard(
            Enfant enfant,
            KinderGarten kindergarten,
            Parent parent,
            String anneeScolaire,
            String classLevel,
            String date) {

        return builder
                .withEnfant(enfant)
                .withKindergarten(kindergarten)
                .withParent(parent)
                .withAnneeScolaire(anneeScolaire)
                .withClassLevel(classLevel)
                .withDate(date)
                .build();
    }
}