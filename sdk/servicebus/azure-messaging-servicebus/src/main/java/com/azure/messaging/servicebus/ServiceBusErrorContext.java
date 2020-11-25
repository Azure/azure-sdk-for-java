// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

/**
 * Context for errors handled by the {@link ServiceBusProcessorClient Service Bus processor}.
 *
 * @see ServiceBusProcessorClient
 */
public final class ServiceBusErrorContext {
    private final Throwable exception;
    private final ServiceBusErrorSource errorSource;
    private final String fullyQualifiedNamespace;
    private final String entityPath;

    ServiceBusErrorContext(Throwable throwable, String fullyQualifiedNamespace, String entityPath) {
        this.exception = throwable;
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.entityPath = entityPath;

        if (throwable instanceof ServiceBusException) {
            final ServiceBusException serviceBusException = ((ServiceBusException) throwable);
            this.errorSource = serviceBusException.getErrorSource();
        } else {
            this.errorSource = ServiceBusErrorSource.RECEIVE;
        }
    }

    /**
     * Gets the exception that triggered the call to the error event handler.
     * @return The exception that triggered the call to the error event handler.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Gets the source associated with the error.
     * @return The source associated with the error.
     */
    public ServiceBusErrorSource getErrorSource() {
        return errorSource;
    }

    /**
     * Gets the namespace name associated with the error event.
     * @return The namespace name associated with the error event.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the entity path associated with the error event.
     * @return The entity path associated with the error event.
     */
    public String getEntityPath() {
        return entityPath;
    }
}

