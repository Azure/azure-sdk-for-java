// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.spring.core.aware.ClientAware;

import java.util.HashSet;
import java.util.Set;

/**
 * Options related to http logging. For example, if you want to log the http request or response, you could set the
 * level to {@link HttpLogDetailLevel#BASIC} or some other levels.
 */
public class LoggingProperties implements ClientAware.Logging {

    private HttpLogDetailLevel level;
    private final Set<String> allowedHeaderNames = new HashSet<>();
    private final Set<String> allowedQueryParamNames = new HashSet<>();
    private Boolean prettyPrintBody;

    public HttpLogDetailLevel getLevel() {
        return level;
    }

    public void setLevel(HttpLogDetailLevel level) {
        this.level = level;
    }

    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames;
    }

    public Set<String> getAllowedQueryParamNames() {
        return allowedQueryParamNames;
    }

    public Boolean getPrettyPrintBody() {
        return prettyPrintBody;
    }

    public void setPrettyPrintBody(Boolean prettyPrintBody) {
        this.prettyPrintBody = prettyPrintBody;
    }

}
