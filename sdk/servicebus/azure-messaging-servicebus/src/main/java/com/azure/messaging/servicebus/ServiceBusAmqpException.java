// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpException;

/**
 * Defines {@link ServiceBusAmqpException} which has additional information about the operation that caused the error.
 *
 * @see ServiceBusErrorSource
 */
public final class ServiceBusAmqpException extends AmqpException {
    private final transient ServiceBusErrorSource errorSource;

    /**
     * @param amqpException for the error hapened.
     * @param errorSource indicating which api caused the error.
     */
    ServiceBusAmqpException(AmqpException amqpException, ServiceBusErrorSource errorSource) {
        super(amqpException.isTransient(), amqpException.getErrorCondition(), amqpException.getMessage(),
            amqpException.getCause(), amqpException.getContext());
        this.errorSource = errorSource;
    }

    /**
     *  Gets the {@link ServiceBusErrorSource} in case of any errors.
     *
     * @return the {@link ServiceBusErrorSource}
     */
    public ServiceBusErrorSource getErrorSource() {
        return errorSource;
    }
}
