// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.sample.gremlin.repository;

import com.azure.spring.sample.data.sample.gremlin.domain.Network;
import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NetworkRepository extends GremlinRepository<Network, String> {
}

