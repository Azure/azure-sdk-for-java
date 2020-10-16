// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.spring.azure.sample.cloudfoundry.cosmos;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CosmosRepository<User, String> {
}
