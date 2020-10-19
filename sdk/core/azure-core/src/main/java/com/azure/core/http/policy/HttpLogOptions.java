// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The log configurations for HTTP messages.
 */
public class HttpLogOptions {
    private String applicationId;
    private HttpLogDetailLevel logLevel;
    private Set<String> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;
    private boolean prettyPrintBody;

    private LogLevel defaultLogLevel;
    private HttpRequestLogger requestLogger;
    private HttpResponseLogger responseLogger;

    private final ClientLogger logger = new ClientLogger(HttpLogOptions.class);

    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private static final List<String> DEFAULT_HEADERS_WHITELIST = Arrays.asList(
        "x-ms-client-request-id",
        "x-ms-return-client-request-id",
        "traceparent",
        "Accept",
        "Cache-Control",
        "Connection",
        "Content-Length",
        "Content-Type",
        "Date",
        "ETag",
        "Expires",
        "If-Match",
        "If-Modified-Since",
        "If-None-Match",
        "If-Unmodified-Since",
        "Last-Modified",
        "Pragma",
        "Request-Id",
        "Retry-After",
        "Server",
        "Transfer-Encoding",
        "User-Agent"
    );

    /**
     * Creates a new instance that does not log any information about HTTP requests or responses.
     */
    public HttpLogOptions() {
        logLevel = HttpLogDetailLevel.NONE;
        allowedHeaderNames = new HashSet<>(DEFAULT_HEADERS_WHITELIST);
        allowedQueryParamNames = new HashSet<>();
        applicationId = null;
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
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setLogLevel(final HttpLogDetailLevel logLevel) {
        this.logLevel = logLevel == null ? HttpLogDetailLevel.NONE : logLevel;
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
     * <p>
     * This method sets the provided header names to be the whitelisted header names which will be logged for all HTTP
     * requests and responses, overwriting any previously configured headers. Additionally, users can use {@link
     * HttpLogOptions#addAllowedHeaderName(String)} or {@link HttpLogOptions#getAllowedHeaderNames()} to add or remove
     * more headers names to the existing set of allowed header names.
     * </p>
     *
     * @param allowedHeaderNames The list of whitelisted header names from the user.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setAllowedHeaderNames(final Set<String> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames == null ? new HashSet<>() : allowedHeaderNames;
        return this;
    }

    /**
     * Sets the given whitelisted header to the default header set that should be logged.
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
     */
    public HttpLogOptions setAllowedQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames = allowedQueryParamNames == null ? new HashSet<>() : allowedQueryParamNames;
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
        this.allowedQueryParamNames.add(allowedQueryParamName);
        return this;
    }

    /**
     * Gets the application specific id.
     *
     * @return The application specific id.
     * @deprecated Use {@link ClientOptions} to configure {@code applicationId}.
     */
    @Deprecated
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * Sets the custom application specific id supplied by the user of the client library.
     *
     * @param applicationId The user specified application id.
     * @return The updated HttpLogOptions object.
     * @deprecated Use {@link ClientOptions} to configure {@code applicationId}.
     */
    @Deprecated
    public HttpLogOptions setApplicationId(final String applicationId) {
        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.length() > MAX_APPLICATION_ID_LENGTH) {
                throw logger
                    .logExceptionAsError(new IllegalArgumentException("'applicationId' length cannot be greater than "
                        + MAX_APPLICATION_ID_LENGTH));
            } else if (applicationId.contains(" ")) {
                throw logger
                    .logExceptionAsError(new IllegalArgumentException("'applicationId' must not contain a space."));
            } else {
                this.applicationId = applicationId;
            }
        }
        return this;
    }

    /**
     * Gets flag to allow pretty printing of message bodies.
     *
     * @return true if pretty printing of message bodies is allowed.
     */
    public boolean isPrettyPrintBody() {
        return prettyPrintBody;
    }

    /**
     * Sets flag to allow pretty printing of message bodies.
     *
     * @param prettyPrintBody If true, pretty prints message bodies when logging. If the detailLevel does not include
     * body logging, this flag does nothing.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setPrettyPrintBody(boolean prettyPrintBody) {
        this.prettyPrintBody = prettyPrintBody;
        return this;
    }

    /**
     * Gets the {@link LogLevel} used by default when logging requests and responses.
     * <p>
     * {@link HttpRequestLogger#getLogLevel(LogLevel, HttpPipelineCallContext)} and {@link
     * HttpResponseLogger#getLogLevel(LogLevel, HttpResponse, Duration)} can be used to set the {@link LogLevel} for
     * each request and response being logged.
     * <p>
     * By default {@link LogLevel#INFORMATIONAL} is used.
     *
     * @return The {@link LogLevel} used by default when logging requests and responses.
     */
    public LogLevel getDefaultLogLevel() {
        return defaultLogLevel;
    }

    /**
     * Sets the {@link LogLevel} used by default when logging requests and responses.
     * <p>
     * {@link HttpRequestLogger#getLogLevel(LogLevel, HttpPipelineCallContext)} and {@link
     * HttpResponseLogger#getLogLevel(LogLevel, HttpResponse, Duration)} can be used to set the {@link LogLevel} for
     * each request and response being logged.
     * <p>
     * By default {@link LogLevel#INFORMATIONAL} is used.
     *
     * @param defaultLogLevel The default log level.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setDefaultLogLevel(LogLevel defaultLogLevel) {
        this.defaultLogLevel = defaultLogLevel;
        return this;
    }

    /**
     * Gets the {@link HttpRequestLogger} that will be used to log requests.
     * <p>
     * A default logger will be used if one isn't supplied.
     *
     * @return The {@link HttpRequestLogger} that will be used to log requests.
     */
    public HttpRequestLogger getRequestLogger() {
        return requestLogger;
    }

    /**
     * Sets the {@link HttpRequestLogger} that will be used to log requests.
     * <p>
     * A default logger will be used if one isn't supplied.
     *
     * @param requestLogger The {@link HttpRequestLogger} that will be used to log requests.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setRequestLogger(HttpRequestLogger requestLogger) {
        this.requestLogger = requestLogger;
        return this;
    }

    /**
     * Gets the {@link HttpResponseLogger} that will be used to log responses.
     * <p>
     * A default logger will be used if one isn't supplied.
     *
     * @return The {@link HttpResponseLogger} that will be used to log responses.
     */
    public HttpResponseLogger getResponseLogger() {
        return responseLogger;
    }

    /**
     * Sets the {@link HttpResponseLogger} that will be used to log responses.
     * <p>
     * A default logger will be sued if one isn't supplied.
     *
     * @param responseLogger The {@link HttpResponseLogger} that will be used to log responses.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setResponseLogger(HttpResponseLogger responseLogger) {
        this.responseLogger = responseLogger;
        return this;
    }

    /**
     * Options passed into HTTP request logging functions.
     */
    public static final class HttpRequestLoggingOptions {
        private final ClientLogger logger;
        private final LogLevel logLevel;
        private final HttpPipelineCallContext httpPipelineCallContext;

        HttpRequestLoggingOptions(ClientLogger logger, LogLevel logLevel,
            HttpPipelineCallContext httpPipelineCallContext) {
            this.logger = logger;
            this.logLevel = logLevel;
            this.httpPipelineCallContext = httpPipelineCallContext;
        }

        /**
         * The {@link ClientLogger} used to log the request.
         *
         * @return The ClientLogger used to log the request.
         */
        public ClientLogger getLogger() {
            return logger;
        }

        /**
         * The {@link LogLevel} used when logging the request.
         *
         * @return The LogLevel used when logging the request.
         */
        public LogLevel getLogLevel() {
            return logLevel;
        }

        /**
         * The contextual information for the request such as headers, body, and metadata.
         *
         * @return The contextual information for the request.
         */
        public HttpPipelineCallContext getHttpPipelineCallContext() {
            return httpPipelineCallContext;
        }
    }

    /**
     * Options passed into HTTP response logging functions.
     */
    public static final class HttpResponseLoggingOptions {
        private final ClientLogger logger;
        private final LogLevel logLevel;
        private final HttpResponse httpResponse;
        private final Duration httpResponseDuration;

        HttpResponseLoggingOptions(ClientLogger logger, LogLevel logLevel, HttpResponse httpResponse,
            Duration httpResponseDuration) {
            this.logger = logger;
            this.logLevel = logLevel;
            this.httpResponse = httpResponse;
            this.httpResponseDuration = httpResponseDuration;
        }

        /**
         * The {@link ClientLogger} used to log the response.
         *
         * @return The ClientLogger used to log the response.
         */
        public ClientLogger getLogger() {
            return logger;
        }

        /**
         * The {@link LogLevel} used when logging the response.
         *
         * @return The LogLevel used when logging the response.
         */
        public LogLevel getLogLevel() {
            return logLevel;
        }

        /**
         * The HTTP response being logged.
         *
         * @return The HTTP response being logged.
         */
        public HttpResponse getHttpResponse() {
            return httpResponse;
        }

        /**
         * The duration of time between sending the request and receiving the response.
         *
         * @return The duration of time between sending the request and receiving the response.
         */
        public Duration getHttpResponseDuration() {
            return httpResponseDuration;
        }
    }
}
