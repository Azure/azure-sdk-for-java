// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorContext;
import org.junit.Assert;
import org.junit.Test;

public class RetryPolicyTest {
    private final ErrorContext errorContext = new ErrorContext("test-namespace");

    @Test
    public void isRetriableException() {
        final Exception exception = new AmqpException(true, "error message", errorContext);
        Assert.assertTrue(RetryPolicy.isRetriableException(exception));
    }

    @Test
    public void notRetriableException() {
        final Exception invalidException = new RuntimeException("invalid exception");
        Assert.assertFalse(RetryPolicy.isRetriableException(invalidException));
    }

    @Test
    public void notRetriableExceptionNotTransient() {
        final Exception invalidException = new AmqpException(false, "Some test exception", errorContext);
        Assert.assertFalse(RetryPolicy.isRetriableException(invalidException));
    }
}
