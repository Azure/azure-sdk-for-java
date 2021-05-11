// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common.repository;

import com.azure.spring.data.gremlin.common.domain.Relationship;
import com.azure.spring.data.gremlin.repository.GremlinRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelationshipRepository extends GremlinRepository<Relationship, String> {

    List<Relationship> findByLocation(String location);

    List<Relationship> findByNameAndLocation(String name, String location);

    List<Relationship> findByNameOrId(String name, String id);
}
