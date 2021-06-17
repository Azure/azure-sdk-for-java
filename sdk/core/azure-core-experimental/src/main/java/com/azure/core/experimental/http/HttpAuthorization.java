// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.http;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * Represents the value of an HTTP Authorization header.
 */
public class HttpAuthorization {
    private final ClientLogger logger = new ClientLogger(DynamicRequest.class);
    private final String scheme;
    private final String parameter;

    /**
     * Constructs a new HttpAuthorization instance.
     *
     * @param scheme Scheme component of an authorization header value.
     * @param parameter The credentials used for the authorization header value.
     */
    public HttpAuthorization(String scheme, String parameter) {
        if (CoreUtils.isNullOrEmpty(scheme)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("scheme must be a nonempty string."));
        }
        if (CoreUtils.isNullOrEmpty(parameter)) {
            throw logger.logExceptionAsError(new NullPointerException("parameter must be a nonempty string."));
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
