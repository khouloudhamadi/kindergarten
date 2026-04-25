package com.kindergarten.kindergarten.compte;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * CompteRepo - SOLID SRP
 *
 * Repository pour l'entité Compte Interface minimaliste : CrudRepository
 * fournit toutes les opérations CRUD
 *
 * Recherches personnalisées (findByEmail, etc.) peuvent être ajoutées ici
 */
@RepositoryRestResource
public interface CompteRepo extends CrudRepository<Compte, String> {
}
