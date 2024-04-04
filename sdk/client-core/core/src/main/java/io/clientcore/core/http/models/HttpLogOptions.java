// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.http.pipeline.HttpRequestLogger;
import io.clientcore.core.http.pipeline.HttpResponseLogger;
import io.clientcore.core.util.configuration.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.clientcore.core.util.configuration.Configuration.getGlobalConfiguration;

/**
 * The log configurations for HTTP messages.
 */
public final class HttpLogOptions {
    private HttpLogDetailLevel logLevel;
    private Set<HttpHeaderName> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;
    private HttpRequestLogger requestLogger;
    private HttpResponseLogger responseLogger;
    private static final List<HttpHeaderName> DEFAULT_HEADERS_ALLOWLIST = Arrays.asList(
        HttpHeaderName.TRACEPARENT,
        HttpHeaderName.ACCEPT,
        HttpHeaderName.CACHE_CONTROL,
        HttpHeaderName.CONNECTION,
        HttpHeaderName.CONTENT_LENGTH,
        HttpHeaderName.CONTENT_TYPE,
        HttpHeaderName.DATE,
        HttpHeaderName.ETAG,
        HttpHeaderName.EXPIRES,
        HttpHeaderName.IF_MATCH,
        HttpHeaderName.IF_MODIFIED_SINCE,
        HttpHeaderName.IF_NONE_MATCH,
        HttpHeaderName.IF_UNMODIFIED_SINCE,
        HttpHeaderName.LAST_MODIFIED,
        HttpHeaderName.PRAGMA,
        HttpHeaderName.CLIENT_REQUEST_ID,
        HttpHeaderName.RETRY_AFTER,
        HttpHeaderName.SERVER,
        HttpHeaderName.TRANSFER_ENCODING,
        HttpHeaderName.USER_AGENT,
        HttpHeaderName.WWW_AUTHENTICATE
    );

    private static final List<String> DEFAULT_QUERY_PARAMS_ALLOWLIST = Collections.singletonList(
        "api-version"
    );

    /**
     * Creates a new instance that does not log any information about HTTP requests or responses.
     */
    public HttpLogOptions() {
        logLevel = HttpLogDetailLevel.ENVIRONMENT_HTTP_LOG_DETAIL_LEVEL;
        allowedHeaderNames = new HashSet<>(DEFAULT_HEADERS_ALLOWLIST);
        allowedQueryParamNames = new HashSet<>(DEFAULT_QUERY_PARAMS_ALLOWLIST);
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
     * <p>If logLevel is not provided, default value of {@link HttpLogDetailLevel#NONE} is set.</p>
     *
     * @param logLevel The {@link HttpLogDetailLevel}.
     *
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setLogLevel(final HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel == null ? HttpLogDetailLevel.NONE : logLevel;

        return this;
    }

    /**
     * Gets the allowed headers that should be logged.
     *
     * @return The list of allowed headers.
     */
    public Set<HttpHeaderName> getAllowedHeaderNames() {
        return Collections.unmodifiableSet(allowedHeaderNames);
    }

    /**
     * Sets the given allowed headers that should be logged.
     *
     * <p>
     * This method sets the provided header names to be the allowed header names which will be logged for all HTTP
     * requests and responses, overwriting any previously configured headers. Additionally, users can use
     * {@link HttpLogOptions#addAllowedHeaderName(HttpHeaderName)} or {@link HttpLogOptions#getAllowedHeaderNames()} to add or
     * remove more headers names to the existing set of allowed header names.
     * </p>
     *
     * @param allowedHeaderNames The list of allowed header names from the user.
     *
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setAllowedHeaderNames(final Set<HttpHeaderName> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames == null ? new HashSet<>() : allowedHeaderNames;

        return this;
    }

    /**
     * Sets the given allowed header to the default header set that should be logged.
     *
     * @param allowedHeaderName The allowed header name from the user.
     *
     * @return The updated HttpLogOptions object.
     *
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     */
    public HttpLogOptions addAllowedHeaderName(final HttpHeaderName allowedHeaderName) {
        Objects.requireNonNull(allowedHeaderName);
        this.allowedHeaderNames.add(allowedHeaderName);

        return this;
    }

    /**
     * Gets the allowed query parameters.
     *
     * @return The list of allowed query parameters.
     */
    public Set<String> getAllowedQueryParamNames() {
        return Collections.unmodifiableSet(allowedQueryParamNames);
    }

    /**
     * Sets the given allowed query params to be displayed in the logging info.
     *
     * @param allowedQueryParamNames The list of allowed query params from the user.
     *
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setAllowedQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames = allowedQueryParamNames == null ? new HashSet<>() : allowedQueryParamNames;

        return this;
    }

    /**
     * Sets the given allowed query param that should be logged.
     *
     * @param allowedQueryParamName The allowed query param name from the user.
     *
     * @return The updated HttpLogOptions object.
     *
     * @throws NullPointerException If {@code allowedQueryParamName} is {@code null}.
     */
    public HttpLogOptions addAllowedQueryParamName(final String allowedQueryParamName) {
        this.allowedQueryParamNames.add(allowedQueryParamName);
        return this;
    }

    /**
     * Gets the {@link HttpRequestLogger} that will be used to log HTTP requests.
     *
     * <p>A default {@link HttpRequestLogger} will be used if one isn't supplied.
     *
     * @return The {@link HttpRequestLogger} that will be used to log HTTP requests.
     */
    public HttpRequestLogger getRequestLogger() {
        return requestLogger;
    }

    /**
     * Sets the {@link HttpRequestLogger} that will be used to log HTTP requests.
     *
     * <p>A default {@link HttpRequestLogger} will be used if one isn't supplied.
     *
     * @param requestLogger The {@link HttpRequestLogger} that will be used to log HTTP requests.
     *
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setRequestLogger(HttpRequestLogger requestLogger) {
        this.requestLogger = requestLogger;

        return this;
    }

    /**
     * Gets the {@link HttpResponseLogger} that will be used to log HTTP responses.
     *
     * <p>A default {@link HttpResponseLogger} will be used if one isn't supplied.
     *
     * @return The {@link HttpResponseLogger} that will be used to log HTTP responses.
     */
    public HttpResponseLogger getResponseLogger() {
        return responseLogger;
    }

    /**
     * Sets the {@link HttpResponseLogger} that will be used to log HTTP responses.
     *
     * <p>A default {@link HttpResponseLogger} will be used if one isn't supplied.
     *
     * @param responseLogger The {@link HttpResponseLogger} that will be used to log HTTP responses.
     *
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setResponseLogger(HttpResponseLogger responseLogger) {
        this.responseLogger = responseLogger;

        return this;
    }

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
         * Logs everything in BASIC, plus all allowed request and response headers.
         */
        HEADERS,

        /**
         * Logs everything in BASIC, plus all the request and response body. Note that only payloads in plain text or
         * plain text encoded in GZIP will be logged.
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
            String detailLevel = configuration.get(Configuration.PROPERTY_HTTP_LOG_DETAIL_LEVEL, "none");

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
}
