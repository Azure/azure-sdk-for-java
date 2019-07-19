// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AmqpExceptionTest {
    private final SessionErrorContext context = new SessionErrorContext("namespace-test", "entity-path-test");
    private final String message = "Some test message.";

    /**
     * Verifies that the exception's properties are correctly set.
     */
    @Test
    public void constructor() {
        // Act
        AmqpException exception = new AmqpException(true, message, context);

        // Assert
        assertTrue(exception.isTransient());
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains(message));

        assertTrue(exception.getContext() instanceof SessionErrorContext);

        SessionErrorContext actualContext = (SessionErrorContext) exception.getContext();
        assertEquals(context.getNamespace(), actualContext.getNamespace());
        assertEquals(context.getEntityPath(), actualContext.getEntityPath());

        assertNull(exception.getErrorCondition());
    }

    /**
     * Verifies the exception's properties when creating with an ErrorCondition.
     */
    @Test
    public void constructorErrorCondition() {
        // Arrange
        IllegalArgumentException innerException = new IllegalArgumentException("Some parameter");
        ErrorCondition condition = ErrorCondition.ILLEGAL_STATE;

        // Act
        AmqpException exception = new AmqpException(false, condition, message, innerException, context);

        // Assert
        assertEquals(condition, exception.getErrorCondition());

        assertTrue(exception.getContext() instanceof SessionErrorContext);

        SessionErrorContext actualContext = (SessionErrorContext) exception.getContext();
        assertEquals(context.getNamespace(), actualContext.getNamespace());
        assertEquals(context.getEntityPath(), actualContext.getEntityPath());

        assertSame(innerException, exception.getCause());
    }
}
