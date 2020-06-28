// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.annotation;

import com.microsoft.azure.spring.data.gremlin.common.GremlinUtils;
import com.microsoft.azure.spring.data.gremlin.common.domain.Dependency;
import com.microsoft.azure.spring.data.gremlin.common.domain.Relationship;
import com.microsoft.azure.spring.data.gremlin.common.TestConstants;
import com.microsoft.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.azure.spring.data.gremlin.conversion.source.GremlinSourceEdge;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationEdgeUnitTest {

    @Test
    public void testAnnotationEdgeDefaultLabel() {
        final GremlinSource<Dependency> source = GremlinUtils.toGremlinSource(Dependency.class);

        Assert.assertTrue(source instanceof GremlinSourceEdge);
        Assert.assertNotNull(source.getLabel());
        Assert.assertEquals(source.getLabel(), Dependency.class.getSimpleName());
    }

    @Test
    public void testAnnotationEdgeSpecifiedLabel() {
        final GremlinSource<Relationship> source = GremlinUtils.toGremlinSource(Relationship.class);

        Assert.assertTrue(source instanceof GremlinSourceEdge);
        Assert.assertNotNull(source.getLabel());
        Assert.assertEquals(source.getLabel(), TestConstants.EDGE_RELATIONSHIP_LABEL);
    }
}
