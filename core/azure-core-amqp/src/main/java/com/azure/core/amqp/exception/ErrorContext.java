// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.exception;

import com.azure.core.implementation.util.ImplUtils;

import java.io.Serializable;
import java.util.Locale;

/**
 * Provides context for an {@link AmqpException}.
 *
 * @see AmqpException
 */
public class ErrorContext implements Serializable {
    private static final long serialVersionUID = -2819764407122954922L;

    private final String namespaceName;

    /**
     * Creates a new instance with the provided {@code namespaceName}.
     *
     * @param namespaceName Event Hub namespace of the error context.
     * @throws NullPointerException when {@code exception} is {@code null}.
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
    public String namespaceName() {
        return namespaceName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "Namespace: %s.", namespaceName);
    }
}
