package com.kindergarten.kindergarten.parent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OCL — Contrainte complexe (traduite en Java)
 *
 * context Inscription inv:
 * Inscription.allInstances()
 * ->select(i | i.enfant = self.enfant and i.anneescolaire = self.anneescolaire)
 * ->size() = 1;
 *
 * Traduction : Un enfant ne peut avoir qu'UNE SEULE inscription active
 * (valid=true)
 * pour une même année scolaire.
 *
 * GRASP - Faible couplage : ce validateur ne dépend pas de Payment ni de
 * KinderGarten.
 */
@Component
public class InscriptionValidator {

    @Autowired
    private InscriptionRepo inscriptionRepo;

    /**
     * Vérifie l'invariant OCL avant toute sauvegarde.
     *
     * @param inscription l'inscription à valider
     * @throws InscriptionViolationException si la contrainte OCL est violée
     */
    public void validerContrainteOCL(Inscription inscription) {
        boolean dejaInscrit = inscriptionRepo.existsByEnfantAndValid(
                inscription.getEnfant(), true);

        if (dejaInscrit) {
            // Vérifier si c'est pour la même année scolaire
            List<Inscription> inscriptionsActives = inscriptionRepo.findByEnfantAndValidOrderByDateDesc(
                    inscription.getEnfant(), true);

            boolean memeAnnee = inscriptionsActives.stream()
                    .anyMatch(i -> i.getAnneescolaire()
                            .equals(inscription.getAnneescolaire()));

            if (memeAnnee) {
                throw new InscriptionViolationException(
                        "OCL Violation: L'enfant "
                                + inscription.getEnfant()
                                + " possède déjà une inscription active "
                                + "pour l'année " + inscription.getAnneescolaire()
                                + ". Un enfant ne peut avoir qu'une seule inscription active par an.");
            }
        }
    }

    /**
     * Exception dédiée à la violation de contrainte OCL.
     */
    public static class InscriptionViolationException extends RuntimeException {
        public InscriptionViolationException(String message) {
            super(message);
        }
    }
}