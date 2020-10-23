// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(namespace, context.getNamespace());
        Assertions.assertEquals(entity, context.getEntityPath());
        Assertions.assertEquals(trackingId, context.getTrackingId());
        Assertions.assertEquals(credits, context.getLinkCredit());
    }
}
