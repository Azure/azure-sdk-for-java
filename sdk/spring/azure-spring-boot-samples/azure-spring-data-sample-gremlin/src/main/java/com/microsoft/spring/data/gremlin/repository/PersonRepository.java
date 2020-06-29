// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.repository;

import com.microsoft.spring.data.gremlin.domain.Person;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.stereotype.Repository;

@Repository
@RepositoryRestController
public interface PersonRepository extends GremlinRepository<Person, String> {
}

