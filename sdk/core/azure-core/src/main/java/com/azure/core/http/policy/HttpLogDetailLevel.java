// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;

import static com.azure.core.util.Configuration.getGlobalConfiguration;

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

    static final HttpLogDetailLevel ENVIRONMENT_HTTP_LOG_DETAIL_LEVEL = fromConfiguration(getGlobalConfiguration());

    static HttpLogDetailLevel fromConfiguration(Configuration configuration) {
        String detailLevel = configuration.get(Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL, "none");

        HttpLogDetailLevel logDetailLevel;
        if (BASIC_VALUE.equalsIgnoreCase(detailLevel)) {
            logDetailLevel = BASIC;
        } else if (HEADERS_VALUE.equalsIgnoreCase(detailLevel)) {
            logDetailLevel = HEADERS;
        } else if (BODY_VALUE.equalsIgnoreCase(detailLevel)) {
            logDetailLevel = BODY;
        } else if (BODY_AND_HEADERS_VALUE.equalsIgnoreCase(detailLevel)
            || BODYANDHEADERS_VALUE.equalsIgnoreCase(detailLevel)) {
            logDetailLevel = BODY_AND_HEADERS;
        } else {
            logDetailLevel = NONE;
        }

        return logDetailLevel;
    }

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
