// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.amqp.exception;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpLink;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.util.CoreUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Provides context for an {@link AmqpException} that occurs in an {@link AmqpConnection}, {@link AmqpSession}, or
 * {@link AmqpLink}.
 *
 * @see AmqpException
 * @see SessionErrorContext
 * @see LinkErrorContext
 */
public class AmqpErrorContext implements Serializable {
    static final String MESSAGE_PARAMETER_DELIMITER = ", ";

    private static final long serialVersionUID = -2819764407122954922L;

    private final String namespace;
    private final Map<String, Object> errorInfo;

    /**
     * Creates a new instance with the provided {@code namespace}.
     *
     * @param namespace The service namespace of the error.
     *
     * @throws IllegalArgumentException when {@code namespace} is {@code null} or empty.
     */
    public AmqpErrorContext(String namespace) {
        if (CoreUtils.isNullOrEmpty(namespace)) {
            throw new IllegalArgumentException("'namespace' cannot be null or empty");
        }

        this.namespace = namespace;
        this.errorInfo = null;
    }

    /**
     * Creates a new instance with the provided {@code namespace}.
     *
     * @param namespace The service namespace of the error.
     * @param errorInfo Additional information associated with the error.
     *
     * @throws IllegalArgumentException when {@code namespace} is {@code null} or empty.
     */
    public AmqpErrorContext(String namespace, Map<String, Object> errorInfo) {
        if (CoreUtils.isNullOrEmpty(namespace)) {
            throw new IllegalArgumentException("'namespace' cannot be null or empty");
        }

        this.namespace = namespace;
        this.errorInfo = Objects.requireNonNull(errorInfo, "'errorInfo' cannot be null.");
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
     * Gets the map carrying information about the error condition.
     *
     * @return Map carrying additional information about the error.
     */
    public Map<String, Object> getErrorInfo() {
        return errorInfo != null ? Collections.unmodifiableMap(errorInfo) : Collections.emptyMap();
    }

    /**
     * Creates a string representation of this ErrorContext.
     *
     * @return A string representation of this ErrorContext.
     */
    @Override
    public String toString() {
        final String formatString = "NAMESPACE: %s. ERROR CONTEXT: %s";

        if (errorInfo == null) {
            return String.format(Locale.ROOT, formatString, getNamespace(), "N/A");
        }

        final StringBuilder builder = new StringBuilder();
        errorInfo.forEach((key, value) -> builder.append(String.format("[%s: %s], ", key, value)));

        return String.format(Locale.ROOT, formatString, getNamespace(), builder.toString());
    }
}
