// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.UUIDIdDomain;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UUIDIdDomainRepository extends CosmosRepository<UUIDIdDomain, UUID> {

}
