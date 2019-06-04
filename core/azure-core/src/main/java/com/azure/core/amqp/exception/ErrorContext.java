// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.exception;

import com.azure.core.implementation.util.ImplUtils;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Provides context for an {@link AmqpException}.
 *
 * @see AmqpException
 */
public class ErrorContext implements Serializable {
    private static final long serialVersionUID = -2819764407122954922L;

    private final String namespaceName;
    private final Throwable exception;

    /**
     * Creates a new instance with the provided {@code namespaceName}.
     *
     * @param exception Exception that caused this error.
     * @param namespaceName Event Hub namespace of the error context.
     * @throws NullPointerException when {@code exception} is {@code null}.
     * @throws IllegalArgumentException when {@code namespaceName} is {@code null} or empty.
     */
    public ErrorContext(final Throwable exception, final String namespaceName) {
        Objects.requireNonNull(exception);

        if (ImplUtils.isNullOrEmpty(namespaceName)) {
            throw new IllegalArgumentException("'namespaceName' cannot be null or empty");
        }

        this.namespaceName = namespaceName;
        this.exception = exception;
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
     * Gets the exception wrapped in this context.
     *
     * @return The exception that caused the error.
     */
    public Throwable exception() {
        return exception;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(Locale.US, "NS: %s. Exception: %s", namespaceName, exception);
    }
}
