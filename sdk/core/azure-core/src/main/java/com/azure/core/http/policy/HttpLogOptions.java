// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import java.util.Set;

/**
 * The log configurations for HTTP messages.
 */
public class HttpLogOptions {
    public HttpLogOptions(final HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * The level of detail to log on HTTP messages.
     *
     * @return The {@link HttpLogDetailLevel}.
     */
    public HttpLogDetailLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Sets the level of detail to log on Http messages.
     *
     * @param logLevel The {@link HttpLogDetailLevel}.
     */
    public void setLogLevel(final HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Gets the whitelisted headers that should be logged.
     *
     * @return The list of whitelisted headers.
     */
    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames;
    }

    /**
     * Sets the given whitelisted headers that should be logged.
     *
     * @param allowedHeaderNames The list of whitelisted header names from the user.
     */
    public void setAllowedHeaderNames(final Set<String> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames;
    }

    /**
     * Gets the whitelisted query parameters.
     *
     * @return The list of whitelisted query parameters.
     */
    public Set<String> getAllowedQueryParamNames() {
        return allowedQueryParamNames;
    }

    /**
     * Sets the given whitelisted query params to be displayed in the logging info.
     *
     * @param allowedQueryParamNames The list of whitelisted query params from the user.
     */
    public void setAllowedQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames = allowedQueryParamNames;
    }

    private HttpLogDetailLevel logLevel;
    private Set<String> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;
}
