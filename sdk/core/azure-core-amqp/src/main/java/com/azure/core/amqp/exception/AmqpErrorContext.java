// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.exception;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.util.CoreUtils;

import java.io.Serializable;
import java.util.Locale;

/**
 * Provides context for an {@link AmqpException} that occurs in an {@link AmqpConnection}, {@link AmqpSession},
 * or {@link AmqpLink}.
 *
 * @see AmqpException
 * @see SessionErrorContext
 * @see LinkErrorContext
 */
public class AmqpErrorContext implements Serializable {
    static final String MESSAGE_PARAMETER_DELIMITER = ", ";

    private static final long serialVersionUID = -2819764407122954922L;

    private final String namespace;

    /**
     * Creates a new instance with the provided {@code namespace}.
     *
     * @param namespace The service namespace of the error.
     * @throws IllegalArgumentException when {@code namespace} is {@code null} or empty.
     */
    public AmqpErrorContext(String namespace) {
        if (CoreUtils.isNullOrEmpty(namespace)) {
            throw new IllegalArgumentException("'namespace' cannot be null or empty");
        }

        this.namespace = namespace;
    }

    /**
     * Gets the namespace for this error.
     *
     * @return The namespace for this error.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Creates a string representation of this ErrorContext.
     *
     * @return A string representation of this ErrorContext.
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "NAMESPACE: %s", getNamespace());
    }
}
