// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import java.util.Objects;

public class ServiceBusErrorContext {
    private final Throwable throwable;

    private final String fullyQualifiedNamespace;

    private final String entityPath;

    /**
     * Creates a new instance of {@link ServiceBusErrorContext}.
     *
     * @param throwable The {@link Throwable error} that occurred.
     * @param fullyQualifiedNamespace for the Service Bus.
     * @param entityPath of the Service Bus resource.
     *
     * @throws NullPointerException if {@code throwable},{@code fullyQualifiedNamespace} or {@code entityPath}
     * is {@code null}.
     */
    ServiceBusErrorContext(final Throwable throwable, final String fullyQualifiedNamespace,
        final String entityPath) {
        this.throwable = Objects.requireNonNull(throwable, "'throwable' cannot be null");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null");
        this.entityPath = Objects.requireNonNull(entityPath, "'throwentityPathable' cannot be null");
    }

    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    public String getEntityPath() {
        return entityPath;
    }
}
