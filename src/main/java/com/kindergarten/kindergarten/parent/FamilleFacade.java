package com.kindergarten.kindergarten.parent;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * =====================================================
 * PATRON GoF — FACADE (Structure)
 * =====================================================
 * Rôle dans le patron :
 *   - FamilleFacade  → joue le rôle de la FACADE
 *   - ParentRepo     → sous-système 1 (gestion des parents)
 *   - EnfantRepo     → sous-système 2 (gestion des enfants)
 *
 * But : Fournir une interface unifiée et simplifiée pour
 * gérer un Parent et ses Enfants, sans exposer la complexité
 * des repositories sous-jacents.
 * =====================================================
 */
@Service
public class FamilleFacade {

    // --- Sous-systèmes cachés derrière la Facade ---
    @Autowired
    private ParentRepo parentRepo;

    @Autowired
    private EnfantRepo enfantRepo;

    // =====================================================
    // OPERATIONS SUR LE PARENT
    // =====================================================

    /**
     * Récupérer un parent par son email
     * (GRASP Expert : Parent connaît ses propres données)
     */
    public Parent getParent(String email) {
        return parentRepo.findById(email)
                .orElseThrow(() -> new RuntimeException("Parent non trouvé : " + email));
    }

    /**
     * Sauvegarder / mettre à jour un parent
     */
    public Parent sauvegarderParent(Parent parent) {
        return parentRepo.save(parent);
    }

    // =====================================================
    // OPERATIONS SUR LES ENFANTS
    // =====================================================

    /**
     * Récupérer tous les enfants d'un parent via son email
     * → Interface unifiée : l'appelant n'a pas besoin de
     *   connaître ParentRepo ni EnfantRepo séparément.
     */
    public List<Enfant> getEnfantsduParent(String email) {
        Parent parent = getParent(email);
        return enfantRepo.findByParent(parent);
    }

    /**
     * Ajouter un enfant à un parent
     * → L'appelant fournit juste l'email et l'objet Enfant,
     *   la Facade gère tout le reste.
     */
    public Enfant ajouterEnfant(String emailParent, Enfant enfant) {
        Parent parent = getParent(emailParent);
        enfant.setParent(parent);
        return enfantRepo.save(enfant);
    }

    /**
     * Modifier un enfant existant
     */
    public Enfant modifierEnfant(Enfant enfant) {
        return enfantRepo.save(enfant);
    }

    /**
     * Supprimer un enfant par son id
     */
    public void supprimerEnfant(Integer idEnfant) {
        enfantRepo.deleteById(idEnfant);
    }

    /**
     * Récupérer un enfant par son id
     */
    public Enfant getEnfant(Integer idEnfant) {
        return enfantRepo.findById(idEnfant)
                .orElseThrow(() -> new RuntimeException("Enfant non trouvé : " + idEnfant));
    }

    // =====================================================
    // OPERATION COMBINEE (Parent + Enfants ensemble)
    // =====================================================

    /**
     * Récupérer un parent avec tous ses enfants en une seule opération.
     * → C'est ici que la Facade apporte le plus de valeur :
     *   une seule méthode remplace plusieurs appels séparés.
     */
    public ParentAvecEnfants getParentAvecEnfants(String email) {
        Parent parent = getParent(email);
        List<Enfant> enfants = enfantRepo.findByParent(parent);
        return new ParentAvecEnfants(parent, enfants);
    }

    // =====================================================
    // Classe interne de résultat combiné
    // =====================================================
    public static class ParentAvecEnfants {
        private final Parent parent;
        private final List<Enfant> enfants;

        public ParentAvecEnfants(Parent parent, List<Enfant> enfants) {
            this.parent = parent;
            this.enfants = enfants;
        }

        public Parent getParent() {
            return parent;
        }

        public List<Enfant> getEnfants() {
            return enfants;
        }
    }
}