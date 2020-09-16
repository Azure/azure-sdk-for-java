// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Role;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import reactor.core.publisher.Flux;

public interface RoleRepository extends ReactiveCosmosRepository<Role, String> {

    Flux<Role> findByDeveloperAndId(boolean isDeveloper, String id);
}
