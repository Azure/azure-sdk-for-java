// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpException;

/**
 * Defines {@link ServiceBusAmqpException} which has addition properties. You can {@link ServiceBusErrorSource} to
 * determine source of error.
 *
 * @see ServiceBusErrorSource
 */
public class ServiceBusAmqpException extends AmqpException {
    private final ServiceBusErrorSource errorSource;

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