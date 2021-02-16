// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.exception.AzureException;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusProcessorClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder;

import java.util.function.Consumer;

/**
 * Exception containing additional information about the operation that caused the error.
 *
 * @see ServiceBusErrorSource
 * @see ServiceBusProcessorClientBuilder#processError(Consumer)
 * @see ServiceBusSessionProcessorClientBuilder#processError(Consumer)
 */
public final class ServiceBusException extends AzureException {
    private final transient ServiceBusErrorSource errorSource;
    private final transient ServiceBusFailureReason reason;
    private final boolean isTransient;

    /**
     * Creates an instance containing the error and the operation that created the error.
     *
     * @param throwable The exception that occurred.
     * @param errorSource The Service Bus operation which caused the error.
     */
    public ServiceBusException(Throwable throwable, ServiceBusErrorSource errorSource) {
        super(throwable.getMessage(), throwable);
        this.errorSource = errorSource;

        if (throwable instanceof AmqpException) {
            AmqpException amqpException = (AmqpException) throwable;
            reason = getServiceBusFailureReasonFromException(amqpException);
            isTransient = amqpException.isTransient();
        } else {
            reason = ServiceBusFailureReason.GENERAL_ERROR;
            isTransient = false;
        }
    }

    /**
     * Gets the {@link ServiceBusFailureReason} in case of any errors.
     * @return the {@link ServiceBusFailureReason}
     */
    public ServiceBusFailureReason getReason() {
        return reason;
    }

    /**
     * Gets whether or not the exception is a transient error or not.
     *
     * @return {@code true} when user can retry the operation that generated the exception without additional
     *      intervention; false otherwise.
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Gets the {@link ServiceBusErrorSource} in case of any errors.
     *
     * @return The source of the error.
     */
    ServiceBusErrorSource getErrorSource() {
        return errorSource;
    }

    private ServiceBusFailureReason getServiceBusFailureReasonFromException(AmqpException throwable) {
        final AmqpErrorCondition errorCondition = throwable.getErrorCondition();

        if (errorCondition == null) {
            return ServiceBusFailureReason.GENERAL_ERROR;
        }

        switch (errorCondition) {
            case NOT_FOUND:
                return ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND;
            case MESSAGE_LOCK_LOST:
                return ServiceBusFailureReason.MESSAGE_LOCK_LOST;
            case MESSAGE_NOT_FOUND:
                return ServiceBusFailureReason.MESSAGE_NOT_FOUND;
            case LINK_PAYLOAD_SIZE_EXCEEDED:
                return ServiceBusFailureReason.MESSAGE_SIZE_EXCEEDED;
            case ENTITY_ALREADY_EXISTS:
                return ServiceBusFailureReason.MESSAGING_ENTITY_ALREADY_EXISTS;
            case ENTITY_DISABLED_ERROR:
                return ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED;
            case RESOURCE_LIMIT_EXCEEDED:
                return ServiceBusFailureReason.QUOTA_EXCEEDED;
            case SERVER_BUSY_ERROR:
                return ServiceBusFailureReason.SERVICE_BUSY;
            case TIMEOUT_ERROR:
                return ServiceBusFailureReason.SERVICE_TIMEOUT;
            case SESSION_CANNOT_BE_LOCKED:
                return ServiceBusFailureReason.SESSION_CANNOT_BE_LOCKED;
            case SESSION_LOCK_LOST:
                return ServiceBusFailureReason.SESSION_LOCK_LOST;
            case UNAUTHORIZED_ACCESS:
                return ServiceBusFailureReason.UNAUTHORIZED;
            case PROTON_IO:
                return ServiceBusFailureReason.SERVICE_COMMUNICATION_ERROR;
            default:
                return ServiceBusFailureReason.GENERAL_ERROR;
        }
    }
}
