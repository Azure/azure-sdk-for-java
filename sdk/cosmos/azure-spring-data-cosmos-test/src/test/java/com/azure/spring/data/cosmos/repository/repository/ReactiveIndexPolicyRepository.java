package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.IndexPolicyEntity;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

public interface ReactiveIndexPolicyRepository extends ReactiveCosmosRepository<IndexPolicyEntity, String> {
}
