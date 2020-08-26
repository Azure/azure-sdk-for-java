// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Client Options for setting common properties for example applicationId. Most of these properties are applied on
 * request being send to Azure Service but some could be used for other purpose also for example applicationId could be
 * used for telemetry.
 */
@Fluent
public final class ClientOptions {
    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private final ClientLogger logger = new ClientLogger(ClientOptions.class);
    private Iterable<Header> headers = new ArrayList<>();

    private String applicationId;

    /**
     * Gets the applicationId.
     * @return The applicationId.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the applicationId provided.
     * @param applicationId to be set.
     *
     * @return updated {@link ClientOptions}.
     */
    public ClientOptions setApplicationId(String applicationId) {

        if (CoreUtils.isNullOrEmpty(applicationId)) {
            return this;
        }

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

        return this;
    }

    /**
     * Sets the provided headers.
     * @param headers headers to be set.
     *
     * @return updated {@link ClientOptions}.
     * @throws NullPointerException if {@code headers} is null.
     */
    public ClientOptions headers(Iterable<Header> headers) {
        this.headers = Objects.requireNonNull(headers, "'headers' cannot be null.");
        return this;
    }

    /**
     * Gets a {@link Iterable} representation of the {@link Header}.
     * @return the headers.
     */
    public Iterable<Header> getHeaders() {
        return headers;
    }

}
