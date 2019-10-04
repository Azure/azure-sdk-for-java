// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The log configurations for HTTP messages.
 */
public class HttpLogOptions {
    private HttpLogDetailLevel logLevel;
    private Set<String> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;

    public HttpLogOptions() {
        allowedHeaderNames = new HashSet<>();
        allowedQueryParamNames = new HashSet<>();
    }

    /**
     * Gets the level of detail to log on HTTP messages.
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
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code logLevel} is {@code null}.
     */
    public HttpLogOptions setLogLevel(final HttpLogDetailLevel logLevel) {
        this.logLevel = Objects.requireNonNull(logLevel);
        return this;
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
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedHeaderNames} is {@code null}.
     */
    public HttpLogOptions setAllowedHeaderNames(final Set<String> allowedHeaderNames) {
        this.allowedHeaderNames = Objects.requireNonNull(allowedHeaderNames);
        return this;
    }

    /**
     * Sets the given whitelisted header that should be logged.
     *
     * @param allowedHeaderName The whitelisted header name from the user.
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     */
    public HttpLogOptions addAllowedHeaderName(final String allowedHeaderName) {
        Objects.requireNonNull(allowedHeaderName);
        this.allowedHeaderNames.add(allowedHeaderName);
        return this;
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
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedQueryParamNames} is {@code null}.
     */
    public HttpLogOptions setAllowedQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames = Objects.requireNonNull(allowedQueryParamNames);
        return this;
    }

    /**
     * Sets the given whitelisted query param that should be logged.
     *
     * @param allowedQueryParamName The whitelisted query param name from the user.
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedQueryParamName} is {@code null}.
     */
    public HttpLogOptions addAllowedQueryParamName(final String allowedQueryParamName) {
        Objects.requireNonNull(allowedQueryParamName);
        this.allowedQueryParamNames.add(allowedQueryParamName);
        return this;
    }
}
