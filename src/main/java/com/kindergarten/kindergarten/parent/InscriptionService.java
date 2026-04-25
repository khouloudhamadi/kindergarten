package com.kindergarten.kindergarten.parent;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * GRASP - Faible couplage
 *
 * Ce service encapsule la logique métier d'inscription.
 * Il est découplé de Payment et KinderGarten : il ne crée pas de paiements,
 * ne manipule pas directement KinderGartenRepo.
 *
 * Utilise :
 * - Builder (GoF) pour construire les inscriptions
 * - InscriptionValidator pour l'invariant OCL
 * - InscriptionBase (LSP) pour exposer les inscriptions
 */
@Service
public class InscriptionService {

    @Autowired
    private InscriptionRepo inscriptionRepo;

    @Autowired
    private InscriptionValidator validator;

    @Autowired
    private InscriptionBuilder inscriptionBuilder; // ← CORRECTION : injection via interface

    /**
     * Crée une nouvelle inscription en utilisant le Builder.
     * Vérifie l'invariant OCL avant la sauvegarde.
     *
     * Faible couplage : ce service ne touche pas Payment.
     * La création des paiements est déléguée à un autre service (PaymentService)
     * via un événement ou appel explicite du controller.
     *
     * @return l'Inscription sauvegardée
     * @throws InscriptionValidator.InscriptionViolationException si OCL violé
     */
    public Inscription creerInscription(
            Enfant enfant,
            KinderGarten kindergarten,
            Parent parent,
            String anneeScolaire,
            String classLevel,
            String date) {

        // Builder Pattern : construction étape par étape
        // ← CORRECTION : director reçoit le builder injecté par Spring
        // (plus de new InscriptionBuilderImpl() en dur)
        InscriptionDirector director = new InscriptionDirector(inscriptionBuilder);
        Inscription inscription = director.buildInscriptionStandard(
                enfant, kindergarten, parent, anneeScolaire, classLevel, date);

        // OCL : vérification de l'invariant avant sauvegarde
        validator.validerContrainteOCL(inscription);

        // Sauvegarde
        return inscriptionRepo.save(inscription);
    }

    /**
     * Retourne une vue LSP-compatible d'une inscription.
     * Toute sous-classe d'InscriptionBase peut être substituée ici.
     */
    public InscriptionBase getInscriptionBase(Inscription inscription) {
        InscriptionStandard std = new InscriptionStandard(inscription);
        std.verifierContratLSP();
        return std;
    }
}