// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.cosmos.springData;

import com.azure.spring.cloud.integration.tests.cosmos.User;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveCosmosRepository<User, String> {
}
