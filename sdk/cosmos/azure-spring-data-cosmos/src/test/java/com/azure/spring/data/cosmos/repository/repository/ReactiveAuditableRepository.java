// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

public interface ReactiveAuditableRepository extends ReactiveCosmosRepository<AuditableEntity, String> {
}
