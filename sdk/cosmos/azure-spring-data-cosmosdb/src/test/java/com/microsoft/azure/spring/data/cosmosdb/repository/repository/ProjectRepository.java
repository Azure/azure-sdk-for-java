// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.repository;

import com.microsoft.azure.spring.data.cosmosdb.domain.Project;
import com.microsoft.azure.spring.data.cosmosdb.repository.CosmosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;

public interface ProjectRepository extends CosmosRepository<Project, String> {

    List<Project> findByNameAndStarCount(String name, Long startCount);

    List<Project> findByNameOrForkCount(String name, Long forkCount);

    List<Project> findByNameAndCreator(String name, String creator);

    List<Project> findByNameOrCreator(String name, String creator);

    List<Project> findByNameAndCreatorOrForkCount(String name, String creator, Long forkCount);

    List<Project> findByNameOrCreatorAndForkCount(String name, String creator, Long forkCount);

    List<Project> findByNameOrCreatorOrForkCount(String name, String creator, Long forkCount);

    List<Project> findByNameOrCreatorAndForkCountOrStarCount(String name, String creator,
                                                             Long forkCount, Long starCount);

    List<Project> findByForkCountGreaterThan(Long forkCount);

    List<Project> findByCreatorAndForkCountGreaterThan(String creator, Long forkCount);

    List<Project> findByCreatorOrForkCountGreaterThan(String creator, Long forkCount);

    List<Project> findByNameOrCreator(String name, String creator, Sort sort);

    List<Project> findByNameAndCreator(String name, String creator, Sort sort);

    List<Project> findByForkCount(Long forkCount, Sort sort);

    List<Project> findByStarCountLessThan(Long starCount);

    List<Project> findByForkCountLessThanEqual(Long forkCount);

    List<Project> findByStarCountLessThanAndForkCountGreaterThan(Long max, Long min);

    List<Project> findByForkCountLessThanEqualAndStarCountGreaterThan(Long max, Long min);

    List<Project> findByStarCountGreaterThanEqual(Long count);

    List<Project> findByForkCountGreaterThanEqualAndCreator(Long count, String creator);

    List<Project> findByHasReleasedTrue();

    List<Project> findByHasReleasedFalse();

    List<Project> findByHasReleasedTrueAndCreator(String creator);

    List<Project> findByHasReleasedFalseAndCreator(String creator);

    List<Project> findByHasReleasedTrueOrCreator(String creator);

    List<Project> findByHasReleasedFalseOrCreator(String creator);

    List<Project> findByCreatorIn(Collection<String> creators);

    List<Project> findByCreatorInAndStarCountIn(Collection<String> creators, Collection<Long> starCounts);

    List<Project> findByCreatorNotIn(Collection<String> creators);

    List<Project> findByCreatorInAndStarCountNotIn(Collection<String> creators, Collection<Long> starCounts);

    List<Project> findByNameIsNull();

    List<Project> findByNameIsNullAndForkCount(Long forkCount);

    List<Project> findByNameIsNotNull();

    List<Project> findByNameIsNotNullAndHasReleased(boolean hasReleased);

    Page<Project> findByForkCount(Long forkCount, Pageable pageable);
}
