// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents the value of an HTTP Authorization header.
 */
@Immutable
public final class HttpAuthorization {
    private final String scheme;
    private final String parameter;

    /**
     * Constructs a new HttpAuthorization instance.
     *
     * @param scheme Scheme component of an authorization header value.
     * @param parameter The credentials used for the authorization header value.
     * @throws NullPointerException If either {@code scheme} or {@code parameter} is null.
     * @throws IllegalArgumentException If either {@code scheme} or {@code parameter} are an empty string.
     */
    public HttpAuthorization(String scheme, String parameter) {
        Objects.requireNonNull(scheme, "'scheme' cannot be null.");
        Objects.requireNonNull(parameter, "'parameter' cannot be null.");

        ClientLogger logger = new ClientLogger(HttpAuthorization.class);
        if (scheme.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'scheme' cannot be empty."));
        }
        if (parameter.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'parameter' cannot be empty."));
        }
        this.scheme = scheme;
        this.parameter = parameter;
    }

    /**
     * @return Scheme of the authorization header.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @return Credential of the authorization header.
     */
    public String getParameter() {
        return parameter;
    }

    @Override
    public String toString() {
        return scheme + " " + parameter;
    }
}
