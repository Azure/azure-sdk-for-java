// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.repository;

import com.azure.spring.data.gremlin.common.domain.Network;
import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NetworkRepository extends GremlinRepository<Network, String> {

    List<Network> findByEdgeList(List<Object> edgeList);
}
