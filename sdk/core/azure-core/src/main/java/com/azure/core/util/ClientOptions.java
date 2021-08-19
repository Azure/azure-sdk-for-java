// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;

/**
 * General configuration options for clients.
 */
@Fluent
public class ClientOptions {
    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private static final String INVALID_APPLICATION_ID_LENGTH = "'applicationId' length cannot be greater than "
        + MAX_APPLICATION_ID_LENGTH;
    private static final String INVALID_APPLICATION_ID_SPACE = "'applicationId' cannot contain spaces.";

    private final ClientLogger logger = new ClientLogger(ClientOptions.class);
    private Iterable<Header> headers;

    private String applicationId;

    /**
     * Gets the application ID.
     *
     * @return The application ID.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the application ID.
     * <p>
     * The {@code applicationId} is used to configure {@link UserAgentPolicy} for telemetry/monitoring purposes.
     * <p>
     * See <a href="https://azure.github.io/azure-sdk/general_azurecore.html#telemetry-policy">Azure Core: Telemetry
     * policy</a> for additional information.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create ClientOptions with application ID 'myApplicationId'</p>
     *
     * {@codesnippet com.azure.core.util.ClientOptions.setApplicationId#String}
     *
     * @param applicationId The application ID.
     *
     * @return The updated ClientOptions object.
     *
     * @throws IllegalArgumentException If {@code applicationId} contains spaces or is larger than 24 characters in
     * length.
     */
    public ClientOptions setApplicationId(String applicationId) {
        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.length() > MAX_APPLICATION_ID_LENGTH) {
                throw logger.logExceptionAsError(new IllegalArgumentException(INVALID_APPLICATION_ID_LENGTH));
            } else if (applicationId.contains(" ")) {
                throw logger.logExceptionAsError(new IllegalArgumentException(INVALID_APPLICATION_ID_SPACE));
            }
        }

        this.applicationId = applicationId;

        return this;
    }

    /**
     * Sets the {@link Header Headers}.
     * <p>
     * The passed headers are applied to each request sent with the client.
     * <p>
     * This overwrites all previously set headers.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Create ClientOptions with Header 'myCustomHeader':'myStaticValue'</p>
     *
     * {@codesnippet com.azure.core.util.ClientOptions.setHeaders#Iterable}
     *
     * @param headers The headers.
     * @return The updated ClientOptions object.
     */
    public ClientOptions setHeaders(Iterable<Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the {@link Header Headers}.
     *
     * @return The {@link Header Headers}, if headers weren't set previously an empty list is returned.
     */
    public Iterable<Header> getHeaders() {
        if (headers == null) {
            return Collections.emptyList();
        }
        return headers;
    }
}
