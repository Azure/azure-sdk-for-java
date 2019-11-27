// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.jupiter.api.Assertions;
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
        Assertions.assertTrue(exception.isTransient());
        Assertions.assertNotNull(exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains(message));

        Assertions.assertTrue(exception.getContext() instanceof SessionErrorContext);

        SessionErrorContext actualContext = (SessionErrorContext) exception.getContext();
        Assertions.assertEquals(context.getNamespace(), actualContext.getNamespace());
        Assertions.assertEquals(context.getEntityPath(), actualContext.getEntityPath());

        Assertions.assertNull(exception.getErrorCondition());
    }

    /**
     * Verifies the exception's properties when creating with an ErrorCondition.
     */
    @Test
    public void constructorErrorCondition() {
        // Arrange
        IllegalArgumentException innerException = new IllegalArgumentException("Some parameter");
        AmqpErrorCondition condition = AmqpErrorCondition.ILLEGAL_STATE;

        // Act
        AmqpException exception = new AmqpException(false, condition, message, innerException, context);

        // Assert
        Assertions.assertEquals(condition, exception.getErrorCondition());

        Assertions.assertTrue(exception.getContext() instanceof SessionErrorContext);

        SessionErrorContext actualContext = (SessionErrorContext) exception.getContext();
        Assertions.assertEquals(context.getNamespace(), actualContext.getNamespace());
        Assertions.assertEquals(context.getEntityPath(), actualContext.getEntityPath());

        Assertions.assertSame(innerException, exception.getCause());
    }
}
