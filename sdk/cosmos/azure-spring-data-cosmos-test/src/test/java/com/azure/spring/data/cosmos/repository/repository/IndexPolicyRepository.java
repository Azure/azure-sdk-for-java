package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.IndexPolicyEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;

public interface IndexPolicyRepository extends CosmosRepository<IndexPolicyEntity, String> {
}
