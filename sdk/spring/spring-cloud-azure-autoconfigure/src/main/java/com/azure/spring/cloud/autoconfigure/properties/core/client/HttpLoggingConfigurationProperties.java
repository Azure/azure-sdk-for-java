// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.client;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.spring.cloud.core.provider.HttpLoggingOptionsProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Options related to http logging. For example, if you want to log the http request or response, you could set the
 *  * level to {@link HttpLogDetailLevel#BASIC} or some other levels.
 */
public class HttpLoggingConfigurationProperties implements HttpLoggingOptionsProvider.HttpLoggingOptions {

    /**
     * The level of detail to log on HTTP messages.
     */
    private HttpLogDetailLevel level;
    /**
     * Comma-delimited list of allowlist headers that should be logged.
     */
    private final Set<String> allowedHeaderNames = new HashSet<>();
    /**
     * Comma-delimited list of allowlist query parameters.
     */
    private final Set<String> allowedQueryParamNames = new HashSet<>();
    /**
     * Whether to pretty print the message bodies.
     */
    private Boolean prettyPrintBody;

    @Override
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

    @Override
    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames;
    }

    @Override
    public Set<String> getAllowedQueryParamNames() {
        return allowedQueryParamNames;
    }

    @Override
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
