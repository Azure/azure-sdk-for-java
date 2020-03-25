/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package sample.gremlin;

import com.microsoft.spring.data.gremlin.repository.GremlinRepository;

import java.util.List;

public interface PersonRepository extends GremlinRepository<Person, String> {

    List<Person> findByNameAndLevel(String name, int level);
}
