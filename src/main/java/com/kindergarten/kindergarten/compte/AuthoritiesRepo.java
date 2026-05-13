package com.kindergarten.kindergarten.compte;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * AuthoritiesRepo - SOLID SRP
 *
 * Repository pour l'entité Authorities Contient les méthodes de requête
 * spécifiques à la gestion des rôles
 */
@RepositoryRestResource
public interface AuthoritiesRepo extends CrudRepository<Authorities, Long> {

    // ========== TROUVER PAR EMAIL ==========
    /**
     * Trouve tous les rôles d'un utilisateur par email
     *
     * @param email l'adresse email
     * @return liste des Authorities
     */
    List<Authorities> findByCompteEmail(String email);

    // ========== VÉRIFIER L'EXISTENCE ==========
    /**
     * Vérifie si une association Compte-Rôle existe
     *
     * @param compte le compte
     * @param authority le rôle
     * @return true si le rôle est attribué au compte
     */
    boolean existsByCompteAndAuthority(Compte compte, RoleType authority);

    /**
     * Vérifie si un utilisateur a un rôle spécifique
     *
     * @param email l'adresse email
     * @param authority le rôle
     * @return true si l'utilisateur a ce rôle
     */
    boolean existsByCompteEmailAndAuthority(String email, RoleType authority);

    /**
     * Compte le nombre de rôles d'un utilisateur
     *
     * @param email l'adresse email
     * @return le nombre de rôles
     */
    long countByCompteEmail(String email);

    // ========== SUPPRIMER ==========
    /**
     * Retire un rôle spécifique d'un utilisateur
     *
     * @param email l'adresse email
     * @param authority le rôle à retirer
     */
    void deleteByCompteEmailAndAuthority(String email, RoleType authority);

    /**
     * Retire tous les rôles d'un utilisateur
     *
     * @param email l'adresse email
     */
    void deleteByCompteEmail(String email);

}
