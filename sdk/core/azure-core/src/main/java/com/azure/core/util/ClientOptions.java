// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client Options for setting common properties for example applicationId. Most of these properties are applied on
 * request being send to Azure Service but some could be used for other purpose also for example applicationId could be
 * used for telemetry.
 */
@Fluent
public final class ClientOptions {
    private final ClientLogger logger = new ClientLogger(ClientOptions.class);
    private final Map<String, Header> headers = new ConcurrentHashMap<>();

    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private String applicationId;

    /**
     * Gets the applicationId.
     *
     * @return The applicationId.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the applicationId provided.
     *
     * @param applicationId to be set.
     * @return updated {@link ClientOptions}.
     *
     */
    public ClientOptions setApplicationId(String applicationId) {
        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.length() > MAX_APPLICATION_ID_LENGTH) {
                throw logger
                    .logExceptionAsError(new IllegalArgumentException("'applicationId' length cannot be greater than "
                        + MAX_APPLICATION_ID_LENGTH));
            } else if (applicationId.contains(" ")) {
                throw logger
                    .logExceptionAsError(new IllegalArgumentException("'applicationId' must not contain a space."));
            } else {
                this.applicationId = applicationId;
            }
        }
        return this;
    }

    /**
     * Sets the provided headers.
     *
     * @param headers headers to be set.
     * @return updated {@link ClientOptions}.
     *
     * @throws NullPointerException if {@code headers} is null.
     */
    public ClientOptions headers(Iterable<Header> headers) {
        Objects.requireNonNull(headers, "'headers' cannot be null.");

        for (final Header header : headers) {
            this.headers.put(formatKey(header.getName()), header);
        }
        return this;
    }

    /**
     * Sets a {@link Header header} with the given name and value or append the {@code value} separated by {@code comma}
     * if the {@link Header} exists for given {@code name}.
     *
     * @param name the name
     * @param value the value
     * @return The updated ClientOptions object
     *
     * @throws NullPointerException if {@code name} or {@code value} is null.
     */
    public ClientOptions addHeader(String name, String value) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");

        Header existing = get(name);
        if (existing == null) {
            this.headers.put(formatKey(name), new Header(name, value));
        } else {
            existing.addValue(value);
        }
        return this;
    }

    /**
     * Gets the {@link Header header} for the provided header name. {@code Null} is returned if the header isn't
     * found.
     *
     * @param name the name of the header to find.
     * @return the header if found, null otherwise.
     *
     * @throws NullPointerException if {@code name} is null.
     */
    public Header get(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");

        return headers.get(formatKey(name));
    }

    /**
     * Removes the {@link Header header} with the provided header name. {@code Null} is returned if the header
     * isn't found.
     *
     * @param name the name of the header to remove.
     * @return the header if removed, null otherwise.
     *
     * @throws NullPointerException if {@code name} is null.
     */
    public Header remove(String name) {
        Objects.requireNonNull(name, "'name' cannot be null.");

        return headers.remove(formatKey(name));
    }

    /**
     * Get the value for the provided header name. {@code Null} is returned if the header name isn't found.
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the value of the header, or null if the header isn't found
     *
     * @throws NullPointerException if {@code name} is null.
     */
    public String getValue(String name) {
        final Header header = get(name);
        return header == null ? null : header.getValue();
    }

    /**
     * Get the values for the provided header name. {@code Null} is returned if the header name isn't found.
     *
     * <p>This returns {@link #getValue(String) getValue} split by {@code comma}.</p>
     *
     * @param name the name of the header whose value is being retrieved.
     * @return the values of the header, or null if the header isn't found
     */
    public String[] getValues(String name) {
        final Header header = get(name);
        return header == null ? null : header.getValues();
    }

    /**
     * Gets a {@link Map} representation of the {@link Header} collection. The keys are {@link Header#getName()} in the
     * given {@link Header}.
     *
     * @return the headers as map
     */
    public Map<String, Header> toMap() {
        return headers;
    }

    private String formatKey(final String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
