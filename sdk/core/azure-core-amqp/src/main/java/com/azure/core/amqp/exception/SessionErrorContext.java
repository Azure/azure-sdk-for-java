// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import com.azure.core.util.CoreUtils;

import java.util.Locale;

/**
 * Context for an error that occurs in an AMQP session when an {@link AmqpException} occurs.
 */
public class SessionErrorContext extends AmqpErrorContext {
    private static final long serialVersionUID = -6595933736672371232L;

    /**
     * Remote endpoint the AMQP connection was connected to when the error occurred.
     */
    private final String entityPath;

    /**
     * Creates a new instance with the {@code namespace} and {@code entityPath}.
     *
     * @param namespace The service namespace of the error.
     * @param entityPath The remote endpoint this AMQP session is connected to when the error occurred.
     * @throws IllegalArgumentException if {@code namespace} or {@code entityPath} is {@code null} or empty.
     */
    public SessionErrorContext(String namespace, String entityPath) {
        super(namespace);
        if (CoreUtils.isNullOrEmpty(entityPath)) {
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
