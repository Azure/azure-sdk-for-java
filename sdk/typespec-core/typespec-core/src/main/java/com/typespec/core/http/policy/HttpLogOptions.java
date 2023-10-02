// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.util.ClientOptions;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.logging.ClientLogger;

import java.util.Arrays;
import java.util.Collections;
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

    private HttpRequestLogger requestLogger;
    private HttpResponseLogger responseLogger;

    // HttpLogOptions is a commonly used model, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpLogOptions.class);

    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private static final String INVALID_APPLICATION_ID_LENGTH = "'applicationId' length cannot be greater than "
        + MAX_APPLICATION_ID_LENGTH;
    private static final String INVALID_APPLICATION_ID_SPACE = "'applicationId' cannot contain spaces.";
    private static final List<String> DEFAULT_HEADERS_ALLOWLIST = Arrays.asList(
        "x-ms-request-id",
        "x-ms-client-request-id",
        "x-ms-return-client-request-id",
        "traceparent",
        "MS-CV",

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
        "User-Agent",
        "WWW-Authenticate"
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
     * Gets the allowed headers that should be logged.
     *
     * @return The list of allowed headers.
     */
    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames;
    }

    /**
     * Sets the given allowed headers that should be logged.
     *
     * <p>
     * This method sets the provided header names to be the allowed header names which will be logged for all HTTP
     * requests and responses, overwriting any previously configured headers. Additionally, users can use {@link
     * HttpLogOptions#addAllowedHeaderName(String)} or {@link HttpLogOptions#getAllowedHeaderNames()} to add or remove
     * more headers names to the existing set of allowed header names.
     * </p>
     *
     * @param allowedHeaderNames The list of allowed header names from the user.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setAllowedHeaderNames(final Set<String> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames == null ? new HashSet<>() : allowedHeaderNames;
        return this;
    }

    /**
     * Sets the given allowed header to the default header set that should be logged.
     *
     * @param allowedHeaderName The allowed header name from the user.
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     */
    public HttpLogOptions addAllowedHeaderName(final String allowedHeaderName) {
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
        return allowedQueryParamNames;
    }

    /**
     * Sets the given allowed query params to be displayed in the logging info.
     *
     * @param allowedQueryParamNames The list of allowed query params from the user.
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
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedQueryParamName} is {@code null}.
     */
    public HttpLogOptions addAllowedQueryParamName(final String allowedQueryParamName) {
        this.allowedQueryParamNames.add(allowedQueryParamName);
        this.getClass().getName();
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
     *
     * @return The updated HttpLogOptions object.
     *
     * @throws IllegalArgumentException If {@code applicationId} contains spaces or is larger than 24 characters in
     * length.
     *
     * @deprecated Use {@link ClientOptions} to configure {@code applicationId}.
     */
    @Deprecated
    public HttpLogOptions setApplicationId(final String applicationId) {
        if (!CoreUtils.isNullOrEmpty(applicationId)) {
            if (applicationId.length() > MAX_APPLICATION_ID_LENGTH) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(INVALID_APPLICATION_ID_LENGTH));
            } else if (applicationId.contains(" ")) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(INVALID_APPLICATION_ID_SPACE));
            }
        }

        this.applicationId = applicationId;

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
     * Gets the {@link HttpRequestLogger} that will be used to log HTTP requests.
     * <p>
     * A default {@link HttpRequestLogger} will be used if one isn't supplied.
     *
     * @return The {@link HttpRequestLogger} that will be used to log HTTP requests.
     */
    public HttpRequestLogger getRequestLogger() {
        return requestLogger;
    }

    /**
     * Sets the {@link HttpRequestLogger} that will be used to log HTTP requests.
     * <p>
     * A default {@link HttpRequestLogger} will be used if one isn't supplied.
     *
     * @param requestLogger The {@link HttpRequestLogger} that will be used to log HTTP requests.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setRequestLogger(HttpRequestLogger requestLogger) {
        this.requestLogger = requestLogger;
        return this;
    }

    /**
     * Gets the {@link HttpResponseLogger} that will be used to log HTTP responses.
     * <p>
     * A default {@link HttpResponseLogger} will be used if one isn't supplied.
     *
     * @return The {@link HttpResponseLogger} that will be used to log HTTP responses.
     */
    public HttpResponseLogger getResponseLogger() {
        return responseLogger;
    }

    /**
     * Sets the {@link HttpResponseLogger} that will be used to log HTTP responses.
     * <p>
     * A default {@link HttpResponseLogger} will be used if one isn't supplied.
     *
     * @param responseLogger The {@link HttpResponseLogger} that will be used to log HTTP responses.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setResponseLogger(HttpResponseLogger responseLogger) {
        this.responseLogger = responseLogger;
        return this;
    }
}
