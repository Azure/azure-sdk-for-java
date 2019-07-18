// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import org.junit.Assert;
import org.junit.Test;

public class ExceptionUtilTest {
    private final ErrorContext context = new ErrorContext("test-namespace");
    private final String message = "an-error-message";

    /**
     * Verifies correct exception is created from an error condition.
     */
    @Test
    public void createsCorrectException() {
        // Arrange
        ErrorCondition condition = ErrorCondition.ARGUMENT_OUT_OF_RANGE_ERROR;

        // Act
        Exception exception = ExceptionUtil.toException(condition.getErrorCondition(), message, context);

        // Assert
        Assert.assertTrue(exception instanceof AmqpException);

        AmqpException amqpException = (AmqpException) exception;

        Assert.assertEquals(condition, amqpException.getErrorCondition());
        Assert.assertFalse(amqpException.isTransient());
        Assert.assertSame(context, amqpException.getContext());
        Assert.assertTrue(amqpException.getMessage().startsWith(message));
    }

    /**
     * Creates correct exception from not found exception that matches the expected service string.
     */
    @Test
    public void createsNotFoundException() {
        // Arrange
        AmqpResponseCode notFound = AmqpResponseCode.NOT_FOUND;
        ErrorCondition condition = ErrorCondition.NOT_FOUND;
        String entityName = "some-name";
        String message = "The messaging entity " + entityName + " could not be found";

        // Act
        Exception exception = ExceptionUtil.amqpResponseCodeToException(notFound.getValue(), message, context);

        // Assert
        Assert.assertTrue(exception instanceof AmqpException);

        AmqpException amqpException = (AmqpException) exception;

        Assert.assertEquals(condition, amqpException.getErrorCondition());
        Assert.assertFalse(amqpException.isTransient());
        Assert.assertSame(context, amqpException.getContext());
        Assert.assertTrue(amqpException.getMessage().contains(message));
    }


    /**
     * Creates correct exception from not found exception.
     */
    @Test
    public void createsNotFoundExceptionNotMatches() {
        // Arrange
        AmqpResponseCode notFound = AmqpResponseCode.NOT_FOUND;
        ErrorCondition condition = ErrorCondition.NOT_FOUND;
        String message = "An entity was not found.";

        // Act
        Exception exception = ExceptionUtil.amqpResponseCodeToException(notFound.getValue(), message, context);

        // Assert
        Assert.assertTrue(exception instanceof AmqpException);

        AmqpException amqpException = (AmqpException) exception;

        Assert.assertEquals(condition, amqpException.getErrorCondition());
        Assert.assertTrue(amqpException.isTransient());
        Assert.assertSame(context, amqpException.getContext());
        Assert.assertTrue(amqpException.getMessage().contains(message));
    }

    /**
     * Verifies AmqpException created from an integer.
     */
    @Test
    public void createsFromStatusCode() {
        // Arrange
        AmqpResponseCode responseCode = AmqpResponseCode.FORBIDDEN;
        ErrorCondition actualCondition = ErrorCondition.RESOURCE_LIMIT_EXCEEDED;

        // Act
        Exception exception = ExceptionUtil.amqpResponseCodeToException(responseCode.getValue(), message, context);

        // Assert
        Assert.assertTrue(exception instanceof AmqpException);

        AmqpException amqpException = (AmqpException) exception;
        Assert.assertEquals(actualCondition, amqpException.getErrorCondition());
        Assert.assertFalse(amqpException.isTransient());
        Assert.assertSame(context, amqpException.getContext());

        Assert.assertTrue(amqpException.getMessage().contains(message));
    }

    /**
     * Verifies AmqpException created from a non-existent status code.
     */
    @Test
    public void createsFromNonExistentStatusCode() {
        // Arrange
        int unknownCode = 202;

        // Act
        Exception exception = ExceptionUtil.amqpResponseCodeToException(unknownCode, message, context);

        // Assert
        Assert.assertTrue(exception instanceof AmqpException);

        AmqpException amqpException = (AmqpException) exception;
        Assert.assertNull(amqpException.getErrorCondition());
        Assert.assertTrue(amqpException.isTransient());
        Assert.assertSame(context, amqpException.getContext());

        Assert.assertTrue(amqpException.getMessage().contains(message));
    }
}
