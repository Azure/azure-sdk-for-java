// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.repository;

import com.azure.spring.data.gremlin.common.domain.Group;
import com.azure.spring.data.gremlin.repository.GremlinRepository;

public interface GroupRepository extends GremlinRepository<Group, Long> {
}
