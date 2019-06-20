// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.exception;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.implementation.util.ImplUtils;

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
public class ErrorContext implements Serializable {
    static final String MESSAGE_PARAMETER_DELIMITER = ", ";

    private static final long serialVersionUID = -2819764407122954922L;

    private final String namespaceName;

    /**
     * Creates a new instance with the provided {@code namespaceName}.
     *
     * @param namespaceName The service namespace of the error.
     * @throws IllegalArgumentException when {@code namespaceName} is {@code null} or empty.
     */
    public ErrorContext(String namespaceName) {
        if (ImplUtils.isNullOrEmpty(namespaceName)) {
            throw new IllegalArgumentException("'namespaceName' cannot be null or empty");
        }

        this.namespaceName = namespaceName;
    }

    /**
     * Gets the namespace for this error.
     *
     * @return The namespace for this error.
     */
    public String getNamespace() {
        return namespaceName;
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
