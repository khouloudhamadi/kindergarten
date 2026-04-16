package com.kindergarten.kindergarten.director;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.kindergarten.kindergarten.kindergarten.KinderGarten;

//Remove @RepositoryRestResource below to disable auto REST api:
@RepositoryRestResource
public interface PaymentParamsRepo extends CrudRepository<PaymentParams, Integer> {
    public List<PaymentParams> findByKindergartenOrderByAnneescol(KinderGarten kindergarten);

    public PaymentParams findByKindergartenAndAnneescolAndClassLevel(KinderGarten kindergarten, String as,
            String classlevel);

    public List<PaymentParams> findByDirectorOrderByKindergarten(Director director);
}
