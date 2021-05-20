// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query;

import com.azure.spring.data.gremlin.common.domain.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SimpleGremlinEntityMetadataUnitTest {

    @Test
    public void testSimpleGremlinEntityMetadata() {
        final SimpleGremlinEntityMetadata<Person> metadata = new SimpleGremlinEntityMetadata<>(Person.class);

        Assertions.assertNotNull(metadata);
        Assertions.assertEquals(metadata.getJavaType(), Person.class);
    }
}
