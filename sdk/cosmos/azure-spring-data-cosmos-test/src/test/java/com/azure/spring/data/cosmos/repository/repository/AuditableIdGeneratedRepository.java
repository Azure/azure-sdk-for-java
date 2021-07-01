// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.AuditableIdGeneratedEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;

public interface AuditableIdGeneratedRepository extends CosmosRepository<AuditableIdGeneratedEntity, String> {
}
