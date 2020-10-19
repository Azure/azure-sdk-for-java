// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.cosmos.domain;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends CosmosRepository<Person, String> {
}
