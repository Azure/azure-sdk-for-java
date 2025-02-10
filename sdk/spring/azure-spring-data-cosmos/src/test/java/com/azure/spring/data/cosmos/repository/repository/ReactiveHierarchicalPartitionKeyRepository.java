// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.HierarchicalPartitionKeyEntity;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactiveHierarchicalPartitionKeyRepository extends ReactiveCosmosRepository<HierarchicalPartitionKeyEntity, String> {

}
