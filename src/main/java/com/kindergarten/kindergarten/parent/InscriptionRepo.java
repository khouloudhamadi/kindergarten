package com.kindergarten.kindergarten.parent;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;

import jakarta.transaction.Transactional;

//Remove @RepositoryRestResource below to disable auto REST api:
@RepositoryRestResource
public interface InscriptionRepo extends CrudRepository<Inscription, Integer> {
    public List<Inscription> findByParent(Parent parent);

    public List<Inscription> findByEnfantAndValidOrderByDateDesc(Enfant enfant, boolean valid);

    public List<Inscription> findByKindergarten(KinderGarten kindergarten);

    public boolean existsByKindergarten(KinderGarten kindergarten);

    @Transactional
    public Long deleteByKindergarten(KinderGarten kindergarten);

    public boolean existsByKindergartenAndValid(KinderGarten kindergarten, boolean valid);

    public boolean existsByEnfantAndValid(Enfant enfant, boolean valid);
}
