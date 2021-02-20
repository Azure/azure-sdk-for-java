package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.AuditableIdGeneratedEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;

public interface AuditableIdGeneratedRepository extends CosmosRepository<AuditableIdGeneratedEntity, String> {
}
