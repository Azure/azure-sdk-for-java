// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.data.cosmos.domain;

import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

public interface ReactivePersonRepository extends ReactiveCosmosRepository<Person, String> {
}
