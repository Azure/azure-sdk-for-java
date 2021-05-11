// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common;

import com.azure.spring.data.gremlin.common.domain.Service;
import com.azure.spring.data.gremlin.conversion.source.AbstractGremlinSource;
import org.junit.Assert;
import org.junit.Test;

public class GremlinUtilsUnitTest {

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIntegerInstance() {
        GremlinUtils.createInstance(Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTestConstantsInstance() {
        GremlinUtils.createInstance(TestConstants.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAbstractInstance() {
        GremlinUtils.createInstance(AbstractGremlinSource.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testTimeToMilliSecondsException() {
        GremlinUtils.timeToMilliSeconds(new Service());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToPrimitiveLongException() {
        GremlinUtils.toPrimitiveLong((short) 2);
    }

    @Test
    public void testToPrimitiveLong() {
        Assert.assertEquals(3, GremlinUtils.toPrimitiveLong(3L));
    }
}
