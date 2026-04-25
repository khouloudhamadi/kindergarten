package com.kindergarten.kindergarten.parent;

/**
 * SOLID - LSP: Implémentation concrète respectant le contrat de InscriptionBase.
 *
 * Cette classe enveloppe une Inscription JPA et expose les méthodes
 * du contrat LSP sans modifier leur sémantique.
 */
public class InscriptionStandard extends InscriptionBase {

    private final Inscription inscription;

    public InscriptionStandard(Inscription inscription) {
        if (inscription == null)
            throw new IllegalArgumentException("Inscription ne peut pas être null");
        this.inscription = inscription;
    }

    /**
     * Respecte LSP : jamais null (l'enfant est garanti par le builder).
     */
    @Override
    public Enfant getEnfant() {
        return inscription.getEnfant();
    }

    /**
     * Respecte LSP : jamais null.
     */
    @Override
    public String getAnneescolaire() {
        return inscription.getAnneescolaire();
    }

    @Override
    public boolean isValid() {
        return inscription.isValid();
    }

    /**
     * Surcharge AUTORISÉE par LSP : enrichit le message sans changer la sémantique.
     * Ne retourne jamais null — contrat respecté.
     */
    @Override
    public String getStatutDescription() {
        String base = super.getStatutDescription();
        return base + " — Niveau: " + inscription.getClass_level();
    }

    public Inscription getInscription() {
        return inscription;
    }
}