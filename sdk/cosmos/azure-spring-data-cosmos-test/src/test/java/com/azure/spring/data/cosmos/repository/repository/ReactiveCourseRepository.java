// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Course;
import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.repository.query.Param;
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
     * Find Course list by (name and department) or (name2 and department2)
     * @param name name
     * @param department department
     * @param name2 name2
     * @param department2 department2
     * @return Course list
     */
    Flux<Course> findByNameAndDepartmentOrNameAndDepartment(String name,
        String department, String name2, String department2);

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

    @Query(value = "select * from c where c.name = @name and c.department = @department")
    Flux<Course> getCoursesWithNameDepartment(@Param("name") String name, @Param("department") String department);

    @Query(value = "select count(c.id) as num_ids, c.department from c group by c.department")
    Flux<ObjectNode> getCoursesGroupByDepartment();

    Mono<Long> countByName(String name);

    @Query(value = "select value count(1) from c where c.name = @name")
    Mono<Long> countByQuery(@Param("name") String name);

}
