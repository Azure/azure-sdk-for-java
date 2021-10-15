// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.core.http.policy.HttpLogDetailLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Properties shared by all azure service client builders.
 */
public class ClientProperties {

    private String applicationId;

    private final List<HeaderProperties> headers = new ArrayList<>();

    private final LoggingProperties logging = new LoggingProperties();

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public List<HeaderProperties> getHeaders() {
        return headers;
    }

    public LoggingProperties getLogging() {
        return logging;
    }

    /**
     * Options related to http logging. For example, if you want to log the http request or response, you could set the
     * level to {@link HttpLogDetailLevel#BASIC} or some other levels.
     */
    public static class LoggingProperties {

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

}
