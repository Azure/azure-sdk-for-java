// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.Assert;
import org.junit.Test;

public class OperationCancelledExceptionTest {
    @Test
    public void correctMessage() {
        final String message = "A test message.";
        final OperationCancelledException exception = new OperationCancelledException(message, null);

        Assert.assertEquals(message, exception.getMessage());
    }

    @Test
    public void correctMessageAndThrowable() {
        // Arrange
        final String message = "A test message.";
        final Throwable innerException = new IllegalArgumentException("An argument");

        // Act
        final OperationCancelledException exception = new OperationCancelledException(message, innerException, null);

        // Arrange
        Assert.assertEquals(message, exception.getMessage());
        Assert.assertEquals(innerException, exception.getCause());
    }
}
