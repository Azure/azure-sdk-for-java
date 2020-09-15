// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.http.policy.UserAgentPolicy;

import java.util.Collections;

/**
 * This class represents various options to be set on the client.
 * <p>
 * The {@link Header} could be set using {@link ClientOptions#setHeaders(Iterable) setHeaders}. The {@link Header} will
 * be applied on the request being sent to Azure Service.
 * <p>
 * The {@code applicationId} could be set using {@link ClientOptions#setApplicationId(String) setApplicationId} which
 * is used for setting {@code applicationId} in the {@link UserAgentPolicy}.
 */
@Fluent
public final class ClientOptions {
    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private final ClientLogger logger = new
        ClientLogger(ClientOptions.class);
    private Iterable<Header> headers;

    private String applicationId;

    /**
     * Gets the applicationId.
     * @return The applicationId.
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the applicationId provided. It is used for setting {@code applicationId} in the {@link UserAgentPolicy}.
     * @param applicationId to be set.
     *
     * @return updated {@link ClientOptions}.
     * @throws IllegalArgumentException If {@code applicationId} contains space or larger than 24 in length.
     */
    public ClientOptions setApplicationId(String applicationId) {

        if (CoreUtils.isNullOrEmpty(applicationId)) {
            this.applicationId = applicationId;
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
     * Sets the headers, overwriting all previously set headers in the process.
     * <p>
     * It will be applied on the request being sent to Azure Service.
     * @param headers headers to be set.
     *
     * @return updated {@link ClientOptions}.
     */
    public ClientOptions setHeaders(Iterable<Header> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets a {@link Iterable} representation of the {@link Header}.
     * @return the headers.
     */
    public Iterable<Header> getHeaders() {
        if (headers == null) {
            return Collections.emptyList();
        }
        return headers;
    }
}
