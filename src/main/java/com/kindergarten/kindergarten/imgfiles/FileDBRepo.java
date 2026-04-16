package com.kindergarten.kindergarten.imgfiles;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//Remove @RepositoryRestResource below to disable auto REST api:
@RepositoryRestResource
public interface FileDBRepo extends CrudRepository<FileDB, String> {
}
