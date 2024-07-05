// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.ReactiveTeacher;
import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface ReactiveTeacherRepository extends ReactiveCosmosRepository<ReactiveTeacher, String> {

    Mono<Boolean> existsByFirstNameIsNotNull();

    Mono<Boolean> existsByLastNameIsNull();

    @Query(value = "SELECT * FROM a WHERE ARRAY_CONTAINS(@firstNames, a.firstName) order by a.id ")
    Flux<ReactiveTeacher> annotatedFindByFirstNames(@Param("firstNames") List<String> firstNames);

    @Query(value = "SELECT * FROM a WHERE ARRAY_CONTAINS(@firstNames, a.firstName) ")
    Flux<ReactiveTeacher> annotatedFindByFirstNamesWithSort(@Param("firstNames") List<String> firstNames, Sort sort);
}
