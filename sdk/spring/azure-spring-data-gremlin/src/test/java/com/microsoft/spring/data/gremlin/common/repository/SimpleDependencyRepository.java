// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.repository;

import com.microsoft.spring.data.gremlin.common.domain.SimpleDependency;
import com.microsoft.spring.data.gremlin.repository.GremlinRepository;

public interface SimpleDependencyRepository extends GremlinRepository<SimpleDependency, String> {
}
