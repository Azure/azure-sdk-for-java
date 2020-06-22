// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.common.repository;

import com.microsoft.spring.data.gremlin.common.domain.Neighbor;
import com.microsoft.spring.data.gremlin.repository.GremlinRepository;

public interface NeighborRepository extends GremlinRepository<Neighbor, Long> {
}
