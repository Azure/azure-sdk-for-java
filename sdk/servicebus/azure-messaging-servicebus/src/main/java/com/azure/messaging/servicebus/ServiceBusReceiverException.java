// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;

/**
 * Defines {@link ServiceBusReceiverException} which has additional information about the operation that caused the
 * error.
 *
 * @see ServiceBusErrorSource
 */
public final class ServiceBusReceiverException extends Exception {
    private final transient ServiceBusErrorSource errorSource;
    private final AmqpException amqpException;

    /**
     * @param amqpException for the error happened.
     * @param errorSource indicating which api caused the error.
     */
    ServiceBusReceiverException(AmqpException amqpException, ServiceBusErrorSource errorSource) {
        super(amqpException.getMessage(), amqpException.getCause());
        this.errorSource = errorSource;
        this.amqpException = amqpException;
    }

    /**
     *  Gets the {@link ServiceBusErrorSource} in case of any errors.
     *
     * @return the {@link ServiceBusErrorSource}
     */
    public ServiceBusErrorSource getErrorSource() {
        return errorSource;
    }

    @Override
    public String getMessage() {
        return amqpException.getMessage();
    }

    /**
     * A boolean indicating if the exception is a transient error or not.
     *
     * @return returns true when user can retry the operation that generated the exception without additional
     * intervention.
     */
    public boolean isTransient() {
        return amqpException.isTransient();
    }

    /**
     * Gets the {@link AmqpErrorCondition} for this exception.
     *
     * @return The {@link AmqpErrorCondition} for this exception, or {@code null} if nothing was set.
     */
    public AmqpErrorCondition getErrorCondition() {
        return amqpException.getErrorCondition();
    }

    /**
     * Gets the context for this exception.
     *
     * @return The context for this exception.
     */
    public AmqpErrorContext getContext() {
        return amqpException.getContext();
    }
}
