// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.sample.gremlin.repository;

import com.azure.spring.data.sample.gremlin.domain.Person;
import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends GremlinRepository<Person, String> {
}

