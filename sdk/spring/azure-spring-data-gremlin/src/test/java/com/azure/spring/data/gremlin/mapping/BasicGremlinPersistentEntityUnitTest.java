// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.mapping;

import com.azure.spring.data.gremlin.annotation.Edge;
import com.azure.spring.data.gremlin.annotation.Graph;
import com.azure.spring.data.gremlin.annotation.Vertex;
import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.domain.Network;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.ClassTypeInformation;

public class BasicGremlinPersistentEntityUnitTest {

    @Test
    public void testVertexPersistentEntity() {
        final BasicGremlinPersistentEntity<Person> entity =
                new BasicGremlinPersistentEntity<>(ClassTypeInformation.from(Person.class));
        final Vertex annotation = entity.findAnnotation(Vertex.class);

        Assertions.assertEquals(entity.getType(), Person.class);
        Assertions.assertEquals(annotation.annotationType(), Vertex.class);
        Assertions.assertEquals(annotation.label(), TestConstants.VERTEX_PERSON_LABEL);
    }

    @Test
    public void testEdgePersistentEntity() {
        final BasicGremlinPersistentEntity<Relationship> entity =
                new BasicGremlinPersistentEntity<>(ClassTypeInformation.from(Relationship.class));
        final Edge annotation = entity.findAnnotation(Edge.class);

        Assertions.assertEquals(entity.getType(), Relationship.class);
        Assertions.assertEquals(annotation.annotationType(), Edge.class);
        Assertions.assertEquals(annotation.label(), TestConstants.EDGE_RELATIONSHIP_LABEL);
    }

    @Test
    public void testGraphPersistentEntity() {
        final BasicGremlinPersistentEntity<Network> entity =
                new BasicGremlinPersistentEntity<>(ClassTypeInformation.from(Network.class));
        final Graph annotation = entity.findAnnotation(Graph.class);

        Assertions.assertEquals(entity.getType(), Network.class);
        Assertions.assertEquals(annotation.annotationType(), Graph.class);
    }
}
