// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class OperationCancelledExceptionTest {
    @Test
    public void correctMessage() {
        final String message = "A test message.";
        final OperationCancelledException exception = new OperationCancelledException(message, null);

        assertEquals(message, exception.getMessage());
    }

    @Test
    public void correctMessageAndThrowable() {
        // Arrange
        final String message = "A test message.";
        final Throwable innerException = new IllegalArgumentException("An argument");

        // Act
        final OperationCancelledException exception = new OperationCancelledException(message, innerException, null);

        // Arrange
        assertEquals(message, exception.getMessage());
        assertEquals(innerException, exception.getCause());
    }
}
