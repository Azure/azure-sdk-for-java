// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.repository.repository;

import com.azure.spring.data.cosmos.domain.Teacher;
import com.azure.spring.data.cosmos.repository.CosmosRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TeacherRepository extends CosmosRepository<Teacher, String> {

    boolean existsByFirstNameIsNotNull();

    boolean existsByLastNameIsNull();
}
