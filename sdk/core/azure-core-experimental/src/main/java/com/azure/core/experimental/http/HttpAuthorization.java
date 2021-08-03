// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents the value of an HTTP Authorization header.
 */
@Immutable
public final class HttpAuthorization {
    private final ClientLogger logger = new ClientLogger(HttpAuthorization.class);
    private final String scheme;
    private final String parameter;

    /**
     * Constructs a new HttpAuthorization instance.
     *
     * @param scheme Scheme component of an authorization header value.
     * @param parameter The credentials used for the authorization header value.
     * @throws NullPointerException if any argument is null.
     * @throws IllegalArgumentException if any argument is an empty string.
     */
    public HttpAuthorization(String scheme, String parameter) {
        Objects.requireNonNull(scheme);
        Objects.requireNonNull(parameter);
        if (scheme.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("scheme must be a nonempty string."));
        }
        if (parameter.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("parameter must be a nonempty string."));
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
        return String.format("%s %s", scheme, parameter);
    }
}
