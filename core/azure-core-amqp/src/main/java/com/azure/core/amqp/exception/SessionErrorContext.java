// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import com.azure.core.implementation.util.ImplUtils;

import java.util.Locale;

/**
 * Context for an error that occurs in an AMQP session when an {@link AmqpException} occurs.
 */
public class SessionErrorContext extends ErrorContext {
    private static final long serialVersionUID = -6595933736672371232L;
    private final String entityPath;

    /**
     * Creates a new instance with the {@code namespaceName} and {@code entityPath}.
     *
     * @param namespaceName The service namespace of the error.
     * @param entityPath The remote endpoint this AMQP session is connected to when the error occurred.
     * @throws IllegalArgumentException if {@code namespaceName} or {@code entityPath} is {@code null} or empty.
     */
    public SessionErrorContext(String namespaceName, String entityPath) {
        super(namespaceName);
        if (ImplUtils.isNullOrEmpty(entityPath)) {
            throw new IllegalArgumentException("'entityPath' cannot be null or empty");
        }

        this.entityPath = entityPath;
    }

    /**
     * Gets the remote path this AMQP entity was connected to when the error occurred.
     *
     * @return the remote path this AMQP entity was connected to when the error occurred.
     */
    public String getEntityPath() {
        return entityPath;
    }

    @Override
    public String toString() {
        return String.join(MESSAGE_PARAMETER_DELIMITER, super.toString(),
            String.format(Locale.US, "PATH: %s", getEntityPath()));
    }
}
