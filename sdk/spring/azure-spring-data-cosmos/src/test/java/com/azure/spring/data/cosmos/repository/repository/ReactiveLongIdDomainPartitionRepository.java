// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.LongIdDomainPartition;
import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveLongIdDomainPartitionRepository extends ReactiveCosmosRepository<LongIdDomainPartition, Long> {

    @Query("SELECT VALUE SUM(a.number) from a where a.name = @name")
    Mono<Long> annotatedSumLongIdValuesByName(@Param("name") String name);

}
