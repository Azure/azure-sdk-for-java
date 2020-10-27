// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpException;

/**
 * Defines {@link ServiceBusException} with addition properties for example {@link ServiceBusErrorSource}.
 */
public class ServiceBusException extends AmqpException {
    private final ServiceBusErrorSource errorSource;

    /**
     * @param amqpException for the error hapened.
     * @param errorSource indicating which api caused the error.
     */
    ServiceBusException(AmqpException amqpException, ServiceBusErrorSource errorSource) {
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
