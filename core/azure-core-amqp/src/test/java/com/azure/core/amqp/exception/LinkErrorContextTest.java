// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.Assert;
import org.junit.Test;

public class LinkErrorContextTest {
    /**
     * Verifies properties set correctly.
     */
    @Test
    public void constructor() {
        // Arrange
        String namespace = "an-namespace-test";
        String entity = "an-entity-name";
        String trackingId = "a-tracking-id";
        Integer credits = -10;

        // Act
        LinkErrorContext context = new LinkErrorContext(namespace, entity, trackingId, credits);

        // Assert
        Assert.assertEquals(namespace, context.getNamespace());
        Assert.assertEquals(entity, context.getEntityPath());
        Assert.assertEquals(trackingId, context.getTrackingId());
        Assert.assertEquals(credits, context.getLinkCredit());
    }
}
