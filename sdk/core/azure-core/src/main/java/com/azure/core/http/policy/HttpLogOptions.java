// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@code HttpLogOptions} class provides configuration options for HTTP logging. This includes setting the log level,
 * specifying allowed header names and query parameters for logging, and controlling whether to pretty print the body
 * of HTTP messages.
 *
 * <p>This class is useful when you need to control the amount of information that is logged during the execution of
 * HTTP requests and responses. It allows you to specify the log level, which determines the amount of detail included
 * in the logs (such as the URL, headers, and body of requests and responses).</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, the {@code HttpLogOptions} is created and the log level is set to {@code HttpLogDetailLevel.BODY_AND_HEADERS}.
 * This means that the URL, HTTP method, headers, and body content of each request and response will be logged.
 * The allowed header names and query parameters for logging are also specified, and pretty printing of the body is enabled.
 * The {@code HttpLogOptions} is then used to create an {@code HttpLoggingPolicy}, which can then be added to the pipeline.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.HttpLogOptions.constructor -->
 * <pre>
 * HttpLogOptions logOptions = new HttpLogOptions&#40;&#41;;
 * logOptions.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;;
 * logOptions.setAllowedHttpHeaderNames&#40;new HashSet&lt;&gt;&#40;Arrays.asList&#40;HttpHeaderName.DATE,
 *     HttpHeaderName.X_MS_REQUEST_ID&#41;&#41;&#41;;
 * logOptions.setAllowedQueryParamNames&#40;new HashSet&lt;&gt;&#40;Arrays.asList&#40;&quot;api-version&quot;&#41;&#41;&#41;;
 * HttpLoggingPolicy loggingPolicy = new HttpLoggingPolicy&#40;logOptions&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.HttpLogOptions.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpLoggingPolicy
 * @see com.azure.core.http.policy.HttpLogDetailLevel
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public class HttpLogOptions {
    private static final HttpHeaderName X_MS_RETURN_CLIENT_REQUEST_ID
        = HttpHeaderName.fromString("x-ms-return-client-request-id");
    private static final HttpHeaderName MS_CV = HttpHeaderName.fromString("MS-CV");
    private static final HttpHeaderName REQUEST_ID = HttpHeaderName.fromString("Request-Id");

    private String applicationId;
    private HttpLogDetailLevel logLevel;
    private Set<HttpHeaderName> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;
    private boolean prettyPrintBody;
    private boolean disableRedactedHeaderLogging;

    private HttpRequestLogger requestLogger;
    private HttpResponseLogger responseLogger;

    // HttpLogOptions is a commonly used model, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(HttpLogOptions.class);

    private static final int MAX_APPLICATION_ID_LENGTH = 24;
    private static final String INVALID_APPLICATION_ID_LENGTH
        = "'applicationId' length cannot be greater than " + MAX_APPLICATION_ID_LENGTH;
    private static final String INVALID_APPLICATION_ID_SPACE = "'applicationId' cannot contain spaces.";
    static final Set<HttpHeaderName> DEFAULT_HEADERS_ALLOWLIST
        = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(HttpHeaderName.X_MS_REQUEST_ID,
            HttpHeaderName.X_MS_CLIENT_REQUEST_ID, X_MS_RETURN_CLIENT_REQUEST_ID, HttpHeaderName.TRACEPARENT, MS_CV,
            HttpHeaderName.ACCEPT, HttpHeaderName.CACHE_CONTROL, HttpHeaderName.CONNECTION,
            HttpHeaderName.CONTENT_LENGTH, HttpHeaderName.DATE, HttpHeaderName.ETAG, HttpHeaderName.EXPIRES,
            HttpHeaderName.IF_MATCH, HttpHeaderName.IF_MODIFIED_SINCE, HttpHeaderName.IF_NONE_MATCH,
            HttpHeaderName.IF_UNMODIFIED_SINCE, HttpHeaderName.LAST_MODIFIED, HttpHeaderName.PRAGMA, REQUEST_ID,
            HttpHeaderName.RETRY_AFTER, HttpHeaderName.RETRY_AFTER_MS, HttpHeaderName.SERVER,
            HttpHeaderName.TRANSFER_ENCODING, HttpHeaderName.USER_AGENT, HttpHeaderName.WWW_AUTHENTICATE)));

    static final List<String> DEFAULT_QUERY_PARAMS_ALLOWLIST = Collections.singletonList("api-version");

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
     * <p>
     * With the deprecation of this method, this will now return a new {@link HashSet} each time called where the values
     * are mapped from {@link HttpHeaderName#getCaseSensitiveName()}.
     *
     * @return The list of allowed headers.
     * @deprecated Use {@link #getAllowedHttpHeaderNames()} instead.
     */
    @Deprecated
    public Set<String> getAllowedHeaderNames() {
        return allowedHeaderNames.stream().map(HttpHeaderName::getCaseSensitiveName).collect(Collectors.toSet());
    }

    /**
     * Gets the allowed {@link HttpHeaderName HttpHeaderNames} that should be logged.
     *
     * @return The list of allowed {@link HttpHeaderName HttpHeaderNames}.
     */
    public Set<HttpHeaderName> getAllowedHttpHeaderNames() {
        return allowedHeaderNames;
    }

    /**
     * Sets the given allowed headers that should be logged.
     * <p>
     * This method sets the provided header names to be the allowed header names which will be logged for all HTTP
     * requests and responses, overwriting any previously configured headers. Additionally, users can use {@link
     * HttpLogOptions#addAllowedHeaderName(String)} or {@link HttpLogOptions#getAllowedHeaderNames()} to add or remove
     * more headers names to the existing set of allowed header names.
     * <p>
     * With the deprecation of this method, if {@code allowedHeaderNames} is non-null, this will map the passed
     * {@code allowedHeaderNames} to a set of {@link HttpHeaderName} using {@link HttpHeaderName#fromString(String)}
     * on each value in the set.
     *
     * @param allowedHeaderNames The list of allowed header names.
     * @return The updated HttpLogOptions object.
     * @deprecated Use {@link #setAllowedHttpHeaderNames(Set)} instead.
     */
    @Deprecated
    public HttpLogOptions setAllowedHeaderNames(Set<String> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames == null
            ? new HashSet<>()
            : allowedHeaderNames.stream().map(HttpHeaderName::fromString).collect(Collectors.toSet());
        return this;
    }

    /**
     * Sets the given allowed {@link HttpHeaderName HttpHeaderNames} that should be logged.
     * <p>
     * This method sets the provided header names to be the allowed header names which will be logged for all HTTP
     * requests and responses, overwriting any previously configured headers. Additionally, users can use {@link
     * HttpLogOptions#addAllowedHeaderName(String)} or {@link HttpLogOptions#getAllowedHeaderNames()} to add or remove
     * more headers names to the existing set of allowed header names.
     *
     * @param allowedHttpHeaderNames The list of allowed {@link HttpHeaderName HttpHeaderNames}.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setAllowedHttpHeaderNames(Set<HttpHeaderName> allowedHttpHeaderNames) {
        this.allowedHeaderNames = allowedHttpHeaderNames == null ? new HashSet<>() : allowedHttpHeaderNames;
        return this;
    }

    /**
     * Sets the given allowed header to the default header set that should be logged.
     * <p>
     * With the deprecation of this method, the passed {@code allowedHeaderName} will be converted to an
     * {@link HttpHeaderName} using {@link HttpHeaderName#fromString(String)}.
     *
     * @param allowedHeaderName The allowed header name.
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     * @deprecated Use {@link #addAllowedHttpHeaderName(HttpHeaderName)} instead.
     */
    @Deprecated
    public HttpLogOptions addAllowedHeaderName(String allowedHeaderName) {
        Objects.requireNonNull(allowedHeaderName);
        this.allowedHeaderNames.add(HttpHeaderName.fromString(allowedHeaderName));
        return this;
    }

    /**
     * Sets the given allowed {@link HttpHeaderName} to the default header set that should be logged.
     *
     * @param allowedHeaderName The allowed {@link HttpHeaderName}.
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     */
    public HttpLogOptions addAllowedHttpHeaderName(HttpHeaderName allowedHeaderName) {
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
     * @throws IllegalArgumentException If {@code applicationId} contains spaces or is larger than 24 characters in
     * length.
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
     * @deprecated Use {@link #setRequestLogger(HttpRequestLogger)} and {@link #setResponseLogger(HttpResponseLogger)}
     * to configure how requests and responses should be logged at a granular level instead.
     */
    @Deprecated
    public boolean isPrettyPrintBody() {
        return prettyPrintBody;
    }

    /**
     * Sets flag to allow pretty printing of message bodies.
     *
     * @param prettyPrintBody If true, pretty prints message bodies when logging. If the detailLevel does not include
     * body logging, this flag does nothing.
     * @return The updated HttpLogOptions object.
     * @deprecated Use {@link #setRequestLogger(HttpRequestLogger)} and {@link #setResponseLogger(HttpResponseLogger)}
     * to configure how requests and responses should be logged at a granular level instead.
     */
    @Deprecated
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

    /**
     * Sets the flag that controls if header names which value is redacted should be logged.
     * <p>
     * Applies only if logging request and response headers is enabled. See {@link #setLogLevel(HttpLogDetailLevel)} for
     * details. Defaults to false - redacted header names are logged.
     *
     * @param disableRedactedHeaderLogging If true, redacted header names are not logged.
     * Otherwise, they are logged as a comma separated list under redactedHeaders property.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions disableRedactedHeaderLogging(boolean disableRedactedHeaderLogging) {
        this.disableRedactedHeaderLogging = disableRedactedHeaderLogging;
        return this;
    }

    /**
     * Gets the flag that controls if header names with redacted values should be logged.
     *
     * @return true if header names with redacted values should be logged.
     */
    public boolean isRedactedHeaderLoggingDisabled() {
        return disableRedactedHeaderLogging;
    }
}
