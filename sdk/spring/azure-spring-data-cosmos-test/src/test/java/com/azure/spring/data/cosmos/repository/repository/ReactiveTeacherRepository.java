// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.ReactiveTeacher;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import reactor.core.publisher.Mono;


public interface ReactiveTeacherRepository extends ReactiveCosmosRepository<ReactiveTeacher, String> {

    Mono<Boolean> existsByFirstNameIsNotNull();

    Mono<Boolean> existsByLastNameIsNull();
}
