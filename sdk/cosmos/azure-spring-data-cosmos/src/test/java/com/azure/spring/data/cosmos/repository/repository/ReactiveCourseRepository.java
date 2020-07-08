// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.azure.spring.data.cosmos.domain.Course;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface ReactiveCourseRepository extends ReactiveCosmosRepository<Course, String> {

    Flux<Course> findByDepartmentIn(Collection<String> departments);
}
