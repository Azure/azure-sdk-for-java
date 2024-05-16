// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;

import static com.azure.core.util.Configuration.getGlobalConfiguration;

/**
 * The {@code HttpLogDetailLevel} class is an enumeration of the levels of detail to log on HTTP messages.
 *
 * <p>This class is useful when you need to control the amount of information that is logged during the execution
 * of HTTP requests. It provides several levels of detail, ranging from no logging at all to logging of headers and
 * body content.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, an {@code HttpLogOptions} is created and the log level is set to
 * {@code HttpLogDetailLevel.BODY_AND_HEADERS}. This means that the URL, HTTP method, headers, and body content of
 * each request and response will be logged. The {@code HttpLogOptions} is then used to create an
 * {@code HttpLoggingPolicy}, which can then be added to the pipeline.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.HttpLogDetailLevel.constructor -->
 * <pre>
 * HttpLogOptions logOptions = new HttpLogOptions&#40;&#41;;
 * logOptions.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;;
 * HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy&#40;logOptions&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.HttpLogDetailLevel.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpLoggingPolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
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
     * Values of the headers will be logged only for allowed headers. See {@link HttpLogOptions#getAllowedHeaderNames()}.
     */
    HEADERS,

    /**
     * Logs everything in BASIC, plus all the request and response body.
     * Note that only payloads in plain text or plain text encoded in GZIP will be logged.
     * The response body will be buffered into memory even if it is never consumed by an application, possibly impacting
     * performance.
     */
    BODY,

    /**
     * Logs everything in HEADERS and BODY.
     * Values of the headers will be logged only for allowed headers. See {@link HttpLogOptions#getAllowedHeaderNames()}.
     * The response body will be buffered into memory even if it is never consumed by an application, possibly impacting
     * performance.
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
     * Whether a URL should be logged.
     *
     * @return Whether a URL should be logged.
     */
    public boolean shouldLogUrl() {
        return this != NONE;
    }

    /**
     * Whether headers should be logged.
     *
     * @return Whether headers should be logged.
     */
    public boolean shouldLogHeaders() {
        return this == HEADERS || this == BODY_AND_HEADERS;
    }

    /**
     * Whether a body should be logged.
     *
     * @return Whether a body should be logged.
     */
    public boolean shouldLogBody() {
        return this == BODY || this == BODY_AND_HEADERS;
    }
}
