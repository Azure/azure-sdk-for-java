// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.repository;

import com.microsoft.azure.spring.data.gremlin.domain.Relation;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationRepository extends GremlinRepository<Relation, String> {
}

