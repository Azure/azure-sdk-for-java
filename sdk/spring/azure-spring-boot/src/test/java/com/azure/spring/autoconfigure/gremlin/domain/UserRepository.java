// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.gremlin.domain;

import com.microsoft.spring.data.gremlin.repository.GremlinRepository;

import java.util.List;

public interface UserRepository extends GremlinRepository<User, String> {

    List<User> findByNameAndEnabled(String name, Boolean enabled);
}
