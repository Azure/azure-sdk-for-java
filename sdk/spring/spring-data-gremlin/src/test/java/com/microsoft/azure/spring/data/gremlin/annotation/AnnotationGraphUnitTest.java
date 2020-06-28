// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.annotation;

import com.microsoft.azure.spring.data.gremlin.common.GremlinUtils;
import com.microsoft.azure.spring.data.gremlin.common.domain.Network;
import com.microsoft.azure.spring.data.gremlin.common.domain.Roadmap;
import com.microsoft.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.azure.spring.data.gremlin.conversion.source.GremlinSourceGraph;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationGraphUnitTest {

    @Test
    public void testAnnotationGraphDefaultCollection() {
        final GremlinSource<Network> source = GremlinUtils.toGremlinSource(Network.class);

        Assert.assertTrue(source instanceof GremlinSourceGraph);
        Assert.assertTrue(source.getLabel().isEmpty());
    }

    @Test
    public void testAnnotationGraphSpecifiedCollection() {
        final GremlinSource<Roadmap> source = GremlinUtils.toGremlinSource(Roadmap.class);

        Assert.assertTrue(source instanceof GremlinSourceGraph);
        Assert.assertTrue(source.getLabel().isEmpty());
    }
}
