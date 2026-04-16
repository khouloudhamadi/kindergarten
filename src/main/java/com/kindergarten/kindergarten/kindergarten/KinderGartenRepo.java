package com.kindergarten.kindergarten.kindergarten;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.kindergarten.kindergarten.director.Director;

//Remove @RepositoryRestResource below to disable auto REST api:
@RepositoryRestResource
public interface KinderGartenRepo extends CrudRepository<KinderGarten, Integer> {
    public List<KinderGarten> findByDirector(Director directeur);
}
