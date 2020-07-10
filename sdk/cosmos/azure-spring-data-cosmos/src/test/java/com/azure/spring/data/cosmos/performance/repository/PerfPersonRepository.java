// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.performance.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.performance.domain.PerfPerson;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfPersonRepository extends CosmosRepository<PerfPerson, String> {
    List<PerfPerson> findAll(Sort sort);

    List<PerfPerson> findByName(String name);
}
