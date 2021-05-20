// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.annotation;

import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.common.TestConstants;
import com.azure.spring.data.gremlin.common.domain.Library;
import com.azure.spring.data.gremlin.common.domain.Person;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.conversion.source.GremlinSourceVertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AnnotationVertexUnitTest {

    @Test
    public void testAnnotationVertexDefaultLabel() {
        final GremlinSource<Library> source = GremlinUtils.toGremlinSource(Library.class);

        Assertions.assertTrue(source instanceof GremlinSourceVertex);
        Assertions.assertNotNull(source.getLabel());
        Assertions.assertEquals(source.getLabel(), Library.class.getSimpleName());
    }

    @Test
    public void testAnnotationVertexSpecifiedLabel() {
        final GremlinSource<Person> source = GremlinUtils.toGremlinSource(Person.class);

        Assertions.assertTrue(source instanceof GremlinSourceVertex);
        Assertions.assertNotNull(source.getLabel());
        Assertions.assertEquals(source.getLabel(), TestConstants.VERTEX_PERSON_LABEL);
    }
}
