// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.data.cosmos.domain;

import com.azure.spring.data.cosmos.repository.CosmosRepository;

public interface PersonRepository extends CosmosRepository<Person, String> {
}
