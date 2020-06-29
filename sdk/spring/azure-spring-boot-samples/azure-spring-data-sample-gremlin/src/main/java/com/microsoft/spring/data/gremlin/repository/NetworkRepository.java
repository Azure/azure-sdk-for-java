// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.spring.data.gremlin.repository;

import com.microsoft.spring.data.gremlin.domain.Network;
import org.springframework.stereotype.Repository;

@Repository
public interface NetworkRepository extends GremlinRepository<Network, String> {
}

