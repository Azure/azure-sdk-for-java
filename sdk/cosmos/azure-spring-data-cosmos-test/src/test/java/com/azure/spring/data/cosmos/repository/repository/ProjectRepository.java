// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Project;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;

public interface ProjectRepository extends CosmosRepository<Project, String> {

    Iterable<Project> findByNameAndStarCount(String name, Long startCount);

    Iterable<Project> findByNameOrForkCount(String name, Long forkCount);

    Iterable<Project> findByNameAndCreator(String name, String creator);
    
    Iterable<Project> findByNameAndCreatorOrNameAndCreator(String name, String creator, String name2, String creator2);

    Iterable<Project> findByNameOrCreator(String name, String creator);

    Iterable<Project> findByNameAndCreatorOrForkCount(String name, String creator, Long forkCount);

    Iterable<Project> findByNameOrCreatorAndForkCount(String name, String creator, Long forkCount);

    Iterable<Project> findByNameOrCreatorOrForkCount(String name, String creator, Long forkCount);

    Iterable<Project> findByNameOrCreatorAndForkCountOrStarCount(String name, String creator,
                                                                 Long forkCount, Long starCount);

    Iterable<Project> findByForkCountGreaterThan(Long forkCount);

    Iterable<Project> findByCreatorAndForkCountGreaterThan(String creator, Long forkCount);

    Iterable<Project> findByCreatorOrForkCountGreaterThan(String creator, Long forkCount);

    Iterable<Project> findByNameOrCreator(String name, String creator, Sort sort);

    Iterable<Project> findByNameAndCreator(String name, String creator, Sort sort);

    Iterable<Project> findByForkCount(Long forkCount, Sort sort);

    Iterable<Project> findByStarCountLessThan(Long starCount);

    Iterable<Project> findByForkCountLessThanEqual(Long forkCount);

    Iterable<Project> findByStarCountLessThanAndForkCountGreaterThan(Long max, Long min);

    Iterable<Project> findByForkCountLessThanEqualAndStarCountGreaterThan(Long max, Long min);

    Iterable<Project> findByStarCountGreaterThanEqual(Long count);

    Iterable<Project> findByForkCountGreaterThanEqualAndCreator(Long count, String creator);

    Iterable<Project> findByHasReleasedTrue();

    Iterable<Project> findByHasReleasedFalse();

    Iterable<Project> findByHasReleasedTrueAndCreator(String creator);

    Iterable<Project> findByHasReleasedFalseAndCreator(String creator);

    Iterable<Project> findByHasReleasedTrueOrCreator(String creator);

    Iterable<Project> findByHasReleasedFalseOrCreator(String creator);

    Iterable<Project> findByCreatorIn(Collection<String> creators);

    Iterable<Project> findByCreatorInAndStarCountIn(Collection<String> creators, Collection<Long> starCounts);

    Iterable<Project> findByCreatorInOrStarCount(Collection<String> creators, Long starCount);

    Iterable<Project> findByCreatorNotIn(Collection<String> creators);

    Iterable<Project> findByCreatorInAndStarCountNotIn(Collection<String> creators, Collection<Long> starCounts);

    Iterable<Project> findByNameIsNull();

    Iterable<Project> findByNameIsNullAndForkCount(Long forkCount);

    Iterable<Project> findByNameIsNotNull();

    Iterable<Project> findByNameIsNotNullAndHasReleased(boolean hasReleased);

    Page<Project> findByForkCount(Long forkCount, Pageable pageable);

    Iterable<Project> findAllByNameIn(Collection<String> names);

    Iterable<Project> findAllByStarCountIn(Collection<Long> startCounts);

    Iterable<Project> findAllByHasReleasedIn(Collection<Boolean> releases);
}
