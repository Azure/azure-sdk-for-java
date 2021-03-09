package com.azure.spring.sample.multi.database;

import org.springframework.data.repository.CrudRepository;

public interface UserRepositoryForMYSQL extends CrudRepository<UserForMYSQL, Integer> {

}
