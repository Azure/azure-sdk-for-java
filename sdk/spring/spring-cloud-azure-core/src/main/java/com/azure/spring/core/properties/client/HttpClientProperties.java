// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.client;

import com.azure.core.http.policy.HttpLogDetailLevel;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Properties shared by all http client builders.
 */
public class HttpClientProperties extends ClientProperties {

    private Duration writeTimeout;
    private Duration responseTimeout;
    private Duration readTimeout;
    private final Logging logging = new Logging();

    public Duration getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Logging getLogging() {
        return logging;
    }

    /**
     * Options related to http logging. For example, if you want to log the http request or response, you could set the
     * level to {@link HttpLogDetailLevel#BASIC} or some other levels.
     */
    public static class Logging {

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
