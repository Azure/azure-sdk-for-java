// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Course;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface ReactiveCourseRepository extends ReactiveCosmosRepository<Course, String> {

    Flux<Course> findByDepartmentIn(Collection<String> departments);

    /**
     * Find Course list by name without case sensitive
     * @param name name
     * @return Course list
     */
    Flux<Course> findByNameIgnoreCase(String name);

    /**
     * Find Course list by name and department without case sensitive
     * @param name name
     * @param department department
     * @return Course list
     */
    Flux<Course> findByNameAndDepartmentAllIgnoreCase(String name, String department);

    /**
     * Find Course list by name or department without case sensitive
     * @param name name
     * @param department department
     * @return Course list
     */
    Flux<Course> findByNameOrDepartmentAllIgnoreCase(String name, String department);

    /**
     * Find a single Course list by name
     * @param name name
     * @return Course list
     */
    Mono<Course> findOneByName(String name);

}
