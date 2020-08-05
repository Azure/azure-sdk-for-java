// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.multidatasource.primarydatasource.first;

import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import reactor.core.publisher.Flux;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 */
public interface UserRepository extends ReactiveCosmosRepository<User, String> {

    Flux<User> findByName(String firstName);


    Flux<User> findByEmailOrName(String email, String name);

}
