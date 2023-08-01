// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

// BEGIN: readme-sample-UserRepository
@Repository
public interface UserRepository extends CosmosRepository<User, String> {
    Iterable<User> findByFirstName(String firstName);
    long countByFirstName(String firstName);
    User findOne(String id, String lastName);
}
// END: readme-sample-UserRepository
