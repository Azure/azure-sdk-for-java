// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.performance.repository;

import com.microsoft.azure.spring.data.cosmosdb.performance.domain.PerfPerson;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfPersonRepository extends CosmosRepository<PerfPerson, String> {
    List<PerfPerson> findAll(Sort sort);

    List<PerfPerson> findByName(String name);
}
