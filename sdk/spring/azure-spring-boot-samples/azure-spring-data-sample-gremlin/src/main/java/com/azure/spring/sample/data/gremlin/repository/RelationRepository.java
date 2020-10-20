// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.data.gremlin.repository;

import com.azure.spring.sample.data.gremlin.domain.Relation;
import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationRepository extends GremlinRepository<Relation, String> {
}

