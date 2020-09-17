// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.gremlin;

import com.microsoft.spring.data.gremlin.repository.GremlinRepository;

import java.util.List;

public interface PersonRepository extends GremlinRepository<Person, String> {

    List<Person> findByNameAndLevel(String name, int level);
}
