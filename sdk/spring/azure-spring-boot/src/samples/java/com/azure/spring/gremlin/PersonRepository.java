// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.gremlin;

import com.microsoft.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends GremlinRepository<Person, String> {
    List<Person> findByName(String name);
}
