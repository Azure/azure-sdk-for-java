// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.SortedProject;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SortedProjectRepository extends CosmosRepository<SortedProject, String> {

    Iterable<SortedProject> findByNameOrCreator(String name, String creator, Sort sort);

    Iterable<SortedProject> findByNameAndCreator(String name, String creator, Sort sort);

    Iterable<SortedProject> findByForkCount(Long forkCount, Sort sort);

    Page<SortedProject> findByForkCount(Long forkCount, Pageable pageable);
}
