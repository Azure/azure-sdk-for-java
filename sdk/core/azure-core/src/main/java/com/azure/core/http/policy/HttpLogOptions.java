// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpRequest;
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
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The log configurations for HTTP messages.
 */
public class HttpLogOptions {
    private String applicationId;
    private HttpLogDetailLevel logLevel;
    private Set<String> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;
    private boolean prettyPrintBody;
    private Function<HttpRequest, LogLevel> requestLogLevelFunction;
    private BiFunction<HttpResponse, Duration, LogLevel> responseLogLevelFunction;
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
     * Gets the {@link Function} used to determine which log level to log the outgoing request.
     * <p>
     * By default {@link LogLevel#INFORMATIONAL} will be used.
     *
     * @return The {@link Function} used to determine the log level to log the outgoing request.
     */
    public Function<HttpRequest, LogLevel> getRequestLogLevelFunction() {
        return requestLogLevelFunction;
    }

    /**
     * Sets the {@link Function} used to determine which log level to log the outgoing request.
     * <p>
     * By default {@link LogLevel#INFORMATIONAL} will be used.
     *
     * @param requestLogLevelFunction The {@link Function} used to determine the log level to log the outgoing request.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setRequestLogLevelFunction(Function<HttpRequest, LogLevel> requestLogLevelFunction) {
        this.requestLogLevelFunction = requestLogLevelFunction;
        return this;
    }

    /**
     * Gets the {@link BiFunction} used to determine which log level to log the incoming response.
     * <p>
     * The {@link HttpResponse} and the duration taken for a response to be returned will be passed to determine the log
     * level.
     * <p>
     * By default {@link LogLevel#INFORMATIONAL} will be used.
     *
     * @return The {@link BiFunction} used to determine the log level to log the incoming response.
     */
    public BiFunction<HttpResponse, Duration, LogLevel> getResponseLogLevelFunction() {
        return responseLogLevelFunction;
    }

    /**
     * Sets the {@link BiFunction} used to determine which log level to log the incoming response.
     * <p>
     * The {@link HttpResponse} and the duration for a response to be returned will be passed to determine the log
     * level.
     * <p>
     * By default {@link LogLevel#INFORMATIONAL} will be used.
     *
     * @param responseLogLevelFunction The {@link BiFunction} used to determine the log level to log the incoming
     * response.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setResponseLogLevelFunction(
        BiFunction<HttpResponse, Duration, LogLevel> responseLogLevelFunction) {
        this.responseLogLevelFunction = responseLogLevelFunction;
        return this;
    }
}
