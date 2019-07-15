// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertTrue(exception.isTransient());
        Assert.assertNotNull(exception.getMessage());
        Assert.assertTrue(exception.getMessage().contains(message));

        Assert.assertTrue(exception.getContext() instanceof SessionErrorContext);

        SessionErrorContext actualContext = (SessionErrorContext) exception.getContext();
        Assert.assertEquals(context.getNamespace(), actualContext.getNamespace());
        Assert.assertEquals(context.getEntityPath(), actualContext.getEntityPath());

        Assert.assertNull(exception.getErrorCondition());
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
        Assert.assertEquals(condition, exception.getErrorCondition());

        Assert.assertTrue(exception.getContext() instanceof SessionErrorContext);

        SessionErrorContext actualContext = (SessionErrorContext) exception.getContext();
        Assert.assertEquals(context.getNamespace(), actualContext.getNamespace());
        Assert.assertEquals(context.getEntityPath(), actualContext.getEntityPath());

        Assert.assertSame(innerException, exception.getCause());
    }
}
