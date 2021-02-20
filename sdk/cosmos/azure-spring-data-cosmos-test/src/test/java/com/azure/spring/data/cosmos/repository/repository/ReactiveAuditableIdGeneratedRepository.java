package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.AuditableIdGeneratedEntity;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;

public interface ReactiveAuditableIdGeneratedRepository extends ReactiveCosmosRepository<AuditableIdGeneratedEntity, String> {
}
