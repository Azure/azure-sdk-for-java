// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals(namespace, context.getNamespace());
        Assertions.assertEquals(entity, context.getEntityPath());
    }
}
