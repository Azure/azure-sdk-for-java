// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.AuditableEntity;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditableRepository extends CosmosRepository<AuditableEntity, String> {

    @Query("select * from r where r.id = @id")
    List<AuditableEntity> annotatedFindById(@Param("id") String id);

}
