package com.kindergarten.kindergarten.parent;

/**
 * SOLID - LSP (Liskov Substitution Principle)
 *
 * Toute sous-classe d'InscriptionBase doit respecter ce contrat :
 * - getEnfant() ne retourne jamais null
 * - getAnneescolaire() ne retourne jamais null
 * - getStatutDescription() retourne une description non-nulle
 *
 * Toute sous-classe peut être utilisée à la place de InscriptionBase
 * sans altérer le comportement attendu.
 */
public abstract class InscriptionBase {

    /**
     * Retourne l'enfant concerné par l'inscription.
     * Contrat LSP : jamais null.
     */
    public abstract Enfant getEnfant();

    /**
     * Retourne l'année scolaire.
     * Contrat LSP : jamais null.
     */
    public abstract String getAnneescolaire();

    /**
     * Retourne si l'inscription est validée.
     */
    public abstract boolean isValid();

    /**
     * Retourne une description lisible du statut.
     * Contrat LSP : jamais null, jamais vide.
     *
     * Les sous-classes DOIVENT respecter ce contrat — elles ne peuvent pas
     * retourner null sans violer LSP.
     */
    public String getStatutDescription() {
        return isValid() ? "Inscription validée" : "En attente de validation";
    }

    /**
     * Vérifie les invariants du contrat.
     * Appelable dans les tests pour vérifier qu'une sous-classe respecte LSP.
     */
    public final void verifierContratLSP() {
        if (getEnfant() == null)
            throw new IllegalStateException("LSP violation: getEnfant() ne doit pas retourner null");
        if (getAnneescolaire() == null)
            throw new IllegalStateException("LSP violation: getAnneescolaire() ne doit pas retourner null");
        if (getStatutDescription() == null || getStatutDescription().isEmpty())
            throw new IllegalStateException("LSP violation: getStatutDescription() ne doit pas retourner null ou vide");
    }
}