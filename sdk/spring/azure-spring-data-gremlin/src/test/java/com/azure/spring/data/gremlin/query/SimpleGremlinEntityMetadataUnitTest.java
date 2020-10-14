// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.query;

import com.azure.spring.data.gremlin.common.domain.Person;
import org.junit.Assert;
import org.junit.Test;

public class SimpleGremlinEntityMetadataUnitTest {

    @Test
    public void testSimpleGremlinEntityMetadata() {
        final SimpleGremlinEntityMetadata<Person> metadata = new SimpleGremlinEntityMetadata<>(Person.class);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(metadata.getJavaType(), Person.class);
    }
}
