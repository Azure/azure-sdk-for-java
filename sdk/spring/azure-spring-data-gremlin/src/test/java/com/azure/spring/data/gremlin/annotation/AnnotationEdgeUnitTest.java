// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.annotation;

import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.domain.Dependency;
import com.azure.spring.data.gremlin.common.domain.Relationship;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationEdgeUnitTest {

    @Test
    public void testAnnotationEdgeDefaultLabel() {
        final GremlinSource<Dependency> source = GremlinUtils.toGremlinSource(Dependency.class);

        Assertions.assertTrue(source instanceof GremlinSourceEdge);
        Assertions.assertNotNull(source.getLabel());
        Assertions.assertEquals(source.getLabel(), Dependency.class.getSimpleName());
    }

    @Test
    public void testAnnotationEdgeSpecifiedLabel() {
        final GremlinSource<Relationship> source = GremlinUtils.toGremlinSource(Relationship.class);

        Assertions.assertTrue(source instanceof GremlinSourceEdge);
        Assertions.assertNotNull(source.getLabel());
        Assertions.assertEquals(source.getLabel(), TestConstants.EDGE_RELATIONSHIP_LABEL);
    }
}
