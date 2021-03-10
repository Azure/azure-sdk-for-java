// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.cosmos.multi.database.multiple.account.repository.cosmos;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface UserRepository extends ReactiveCosmosRepository<CosmosUser, String> {

    Flux<CosmosUser> findByName(String firstName);

    Flux<CosmosUser> findByEmailOrName(String email, String name);
}
