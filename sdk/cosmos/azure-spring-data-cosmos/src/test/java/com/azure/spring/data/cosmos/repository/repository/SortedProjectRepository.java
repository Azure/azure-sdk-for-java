// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.domain.SortedProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface SortedProjectRepository extends CosmosRepository<SortedProject, String> {

    List<SortedProject> findByNameOrCreator(String name, String creator, Sort sort);

    List<SortedProject> findByNameAndCreator(String name, String creator, Sort sort);

    List<SortedProject> findByForkCount(Long forkCount, Sort sort);

    Page<SortedProject> findByForkCount(Long forkCount, Pageable pageable);
}
