// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package example.springdata.cosmosdb;

import com.microsoft.azure.spring.data.cosmosdb.repository.ReactiveCosmosRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
@RepositoryRestResource(collectionResourceRel = "user", path = "user")
public interface UserRepository extends ReactiveCosmosRepository<User, String> {

    Flux<User> findByName(String firstName);

    Flux<User> findByEmailAndAddress(String email, Address address);

    Flux<User> findByEmailOrName(String email, String name);

    Flux<User> findByCount(Long count, Sort sort);

    Flux<User> findByNameIn(Collection<String> names);

    Flux<User> findByAddress(Address address);
}

