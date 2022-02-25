// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.spring.core.aware.HttpLoggingOptionsAware;

import java.util.HashSet;
import java.util.Set;

/**
 * Options related to http logging. For example, if you want to log the http request or response, you could set the
 * level to {@link HttpLogDetailLevel#BASIC} or some other levels.
 */
public final class HttpLoggingProperties implements HttpLoggingOptionsAware.HttpLogging {

    /**
     * Gets the level of detail to log on HTTP messages.
     */
    private HttpLogDetailLevel level;
    /**
     * The whitelisted headers that should be logged.
     */
    private final Set<String> allowedHeaderNames = new HashSet<>();
    /**
     * The whitelisted query parameters.
     */
    private final Set<String> allowedQueryParamNames = new HashSet<>();
    /**
     * Whether to pretty print the message bodies.
     */
    private Boolean prettyPrintBody;

    /**
     * Get the logging detail level.
     * @return The logging detail level.
     */
    public HttpLogDetailLevel getLevel() {
        return level;
    }

    /**
     * Set the logging detail level.
     * @param level The logging detail level.
     */
    public void setLevel(HttpLogDetailLevel level) {
        this.level = level;
    }

    /**
     * Get allowed http header names.
     * @return The allowed http header names.
     */
    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames;
    }

    /**
     * Get allowed http query parameter names.
     * @return The allowed http query parameter names.
     */
    public Set<String> getAllowedQueryParamNames() {
        return allowedQueryParamNames;
    }

    /**
     * Get whether to pretty print body.
     * @return Whether to pretty print body.
     */
    public Boolean getPrettyPrintBody() {
        return prettyPrintBody;
    }

    /**
     * Set whether to pretty print body.
     * @param prettyPrintBody Whether to pretty print body.
     */
    public void setPrettyPrintBody(Boolean prettyPrintBody) {
        this.prettyPrintBody = prettyPrintBody;
    }

}
