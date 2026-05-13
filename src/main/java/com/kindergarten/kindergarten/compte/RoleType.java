package com.kindergarten.kindergarten.compte;

/**
 * RoleType Enum - SOLID SRP
 *
 * Responsabilité unique : énumérer tous les rôles possibles du système Remplace
 * les String libres ("Admin", "Parent", "Kindergarten Director") par des
 * valeurs contraintes et typées.
 *
 * Avantages : - Pas de typo (impossible d'écrire "admin" au lieu de "Admin") -
 * Cohérence garantie avec Spring Security - Facile d'ajouter un nouveau rôle
 * (ex: ROLE_TEACHER)
 */
public enum RoleType {
    ROLE_ADMIN("Admin"),
    ROLE_PARENT("Parent"),
    ROLE_DIRECTOR("Kindergarten Director");

    private final String legacyName;

    RoleType(String legacyName) {
        this.legacyName = legacyName;
    }

    public String getLegacyName() {
        return legacyName;
    }

    /**
     * Convertit l'ancien format String du projet vers le nouveau RoleType enum
     * Utile pour la migration des données existantes
     *
     * @param type l'ancienne valeur String ("Admin", "Parent", etc)
     * @return le RoleType correspondant
     * @throws IllegalArgumentException si la valeur n'existe pas
     */
    public static RoleType fromLegacy(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type de rôle ne peut pas être null ou vide");
        }

        return switch (type.trim()) {
            case "Admin", "ROLE_ADMIN" ->
                ROLE_ADMIN;
            case "Parent", "ROLE_PARENT" ->
                ROLE_PARENT;
            case "Kindergarten Director", "ROLE_DIRECTOR" ->
                ROLE_DIRECTOR;
            default ->
                throw new IllegalArgumentException(
                        "Rôle inconnu : " + type + ". Valeurs autorisées : "
                        + "Admin, Parent, Kindergarten Director");
        };
    }

    /**
     * Retourne la représentation Spring Security du rôle (format ROLE_XXX
     * requIS par Spring Security)
     */
    @Override
    public String toString() {
        return this.name();
    }
}
