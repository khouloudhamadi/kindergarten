package com.kindergarten.kindergarten.parent;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//Remove @RepositoryRestResource below to disable auto REST api:
@RepositoryRestResource
public interface EnfantRepo extends CrudRepository<Enfant, Integer> {
    public List<Enfant> findByParent(Parent parent);
}
