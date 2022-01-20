// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.logging.ClientLogger;

/**
 * The level of detail to log on HTTP messages.
 */
public enum HttpLogDetailLevel {
    /**
     * Logging is turned off.
     */
    NONE,

    /**
     * Logs only URLs, HTTP methods, and time to finish the request.
     */
    BASIC,

    /**
     * Logs everything in BASIC, plus all the request and response headers.
     */
    HEADERS,

    /**
     * Logs everything in BASIC, plus all the request and response body.
     * Note that only payloads in plain text or plain text encoded in GZIP
     * will be logged.
     */
    BODY,

    /**
     * Logs everything in HEADERS and BODY.
     */
    BODY_AND_HEADERS;

    static final String BASIC_VALUE = "basic";
    static final String HEADERS_VALUE = "headers";
    static final String BODY_VALUE = "body";
    static final String BODY_AND_HEADERS_VALUE = "body_and_headers";
    static final String BODYANDHEADERS_VALUE = "bodyandheaders";

    private static final ConfigurationProperty.ValueProcessor<HttpLogDetailLevel> LOG_LEVEL_CONVERTER = new ConfigurationProperty.ValueProcessor<HttpLogDetailLevel>() {
        @Override
        public HttpLogDetailLevel processAndConvert(String value, HttpLogDetailLevel defaultValue, String propertyName, ClientLogger logger) {
            HttpLogDetailLevel logDetailLevel = defaultValue;
            if (BASIC_VALUE.equalsIgnoreCase(value)) {
                logDetailLevel = BASIC;
            } else if (HEADERS_VALUE.equalsIgnoreCase(value)) {
                logDetailLevel = HEADERS;
            } else if (BODY_VALUE.equalsIgnoreCase(value)) {
                logDetailLevel = BODY;
            } else if (BODY_AND_HEADERS_VALUE.equalsIgnoreCase(value)
                || BODYANDHEADERS_VALUE.equalsIgnoreCase(value)) {
                logDetailLevel = BODY_AND_HEADERS;
            }

            return logDetailLevel;
        }
    };

    static final ConfigurationProperty<HttpLogDetailLevel> LOG_LEVEL_PROP = new ConfigurationProperty<>("http.logging.level", LOG_LEVEL_CONVERTER, false,
        Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL, NONE, null, new ClientLogger(HttpLogDetailLevel.class));

    static final HttpLogDetailLevel ENVIRONMENT_HTTP_LOG_DETAIL_LEVEL = Configuration.getGlobalConfiguration().get(LOG_LEVEL_PROP);

    /**
     * @return a value indicating whether a request's URL should be logged.
     */
    public boolean shouldLogUrl() {
        return this != NONE;
    }

    /**
     * @return a value indicating whether HTTP message headers should be logged.
     */
    public boolean shouldLogHeaders() {
        return this == HEADERS || this == BODY_AND_HEADERS;
    }

    /**
     * @return a value indicating whether HTTP message bodies should be logged.
     */
    public boolean shouldLogBody() {
        return this == BODY || this == BODY_AND_HEADERS;
    }
}
