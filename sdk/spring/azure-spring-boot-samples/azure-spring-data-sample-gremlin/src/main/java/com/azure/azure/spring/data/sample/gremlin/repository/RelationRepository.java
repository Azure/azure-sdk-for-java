// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.azure.spring.data.sample.gremlin.repository;

import com.azure.azure.spring.data.sample.gremlin.domain.Relation;
import com.microsoft.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationRepository extends GremlinRepository<Relation, String> {
}

