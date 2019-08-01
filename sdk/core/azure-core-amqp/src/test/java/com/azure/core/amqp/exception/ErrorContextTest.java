// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.Assert;
import org.junit.Test;

public class ErrorContextTest {
    /**
     * Verifies properties set correctly.
     */
    @Test
    public void constructor() {
        // Arrange
        String namespace = "an-namespace-test";

        // Act
        ErrorContext context = new ErrorContext(namespace);

        // Assert
        Assert.assertEquals(namespace, context.getNamespace());
    }

    /**
     * Verifies an exception is thrown if namespace is an empty string.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorEmptyString() {
        // Act
        ErrorContext context = new ErrorContext("");
    }

    /**
     * Verifies an exception is thrown if namespace is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorNull() {
        // Act
        ErrorContext context = new ErrorContext(null);
    }
}
