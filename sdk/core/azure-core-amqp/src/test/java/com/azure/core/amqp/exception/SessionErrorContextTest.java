// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.Assert;
import org.junit.Test;

public class SessionErrorContextTest {
    /**
     * Verifies properties set correctly.
     */
    @Test
    public void constructor() {
        // Arrange
        String namespace = "an-namespace-test";
        String entity = "an-entity-name";

        // Act
        SessionErrorContext context = new SessionErrorContext(namespace, entity);

        // Assert
        Assert.assertEquals(namespace, context.getNamespace());
        Assert.assertEquals(entity, context.getEntityPath());
    }
}
