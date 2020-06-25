// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.data.cosmosdb.repository.repository;

import com.microsoft.azure.spring.data.cosmosdb.domain.Course;
import com.microsoft.azure.spring.data.cosmosdb.repository.ReactiveCosmosRepository;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface ReactiveCourseRepository extends ReactiveCosmosRepository<Course, String> {

    Flux<Course> findByDepartmentIn(Collection<String> departments);
}
