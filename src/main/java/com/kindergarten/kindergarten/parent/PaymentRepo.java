package com.kindergarten.kindergarten.parent;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//Remove @RepositoryRestResource below to disable auto REST api:
@RepositoryRestResource
public interface PaymentRepo extends CrudRepository<Payment, Integer> {
    public List<Payment> findByInscriptionOrderById(Inscription insc);

    public Long deleteByInscription(Inscription insc);

    List<Payment> findByInscription(Inscription insc);
}
