// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import static org.junit.jupiter.api.Assertions.*;
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
        assertEquals(namespace, context.getNamespace());
        assertEquals(entity, context.getEntityPath());
        assertEquals(trackingId, context.getTrackingId());
        assertEquals(credits, context.getLinkCredit());
    }
}
