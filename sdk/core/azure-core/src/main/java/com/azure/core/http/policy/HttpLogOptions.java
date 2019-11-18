// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The log configurations for HTTP messages.
 */
public class HttpLogOptions {
    private HttpLogDetailLevel logLevel;
    private Set<String> allowedHeaderNames;
    private Set<String> allowedHeaderPatterns;
    private Set<String> allowedQueryParamNames;
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
        allowedHeaderPatterns = new HashSet<>();
        allowedQueryParamNames = new HashSet<>();
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
     * requests and responses, overwriting any previously configured headers, including the default set. Additionally,
     * users can use {@link HttpLogOptions#addAllowedHeaderName(String)} or
     * {@link HttpLogOptions#getAllowedHeaderNames()} to add or remove more headers names to the existing set of
     * allowed header names.
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
     * Gets the header patterns that are used to match header names to determine if they should be logged.
     *
     * @return The list of header patterns.
     */
    public Set<String> getAllowedHeaderPatterns() {
        return allowedHeaderPatterns;
    }

    /**
     * Sets the given header patterns that are used to match header names to determine if they should be logged.
     *
     * <p>This method sets the header patterns that are used in all HTTP requests and responses, overwriting any
     * previously configured patterns, including the default set. Additionally, users can use {@link
     * #addAllowedHeaderPattern(String)} or {@link #getAllowedHeaderPatterns()} to add or remove patterns to the
     * existing set of allowed patterns.</p>
     *
     * @param allowedHeaderPatterns The list of header patterns.
     * @return The updated HttpLogOptions object.
     */
    public HttpLogOptions setAllowedHeaderPatterns(final Set<String> allowedHeaderPatterns) {
        this.allowedHeaderPatterns = (allowedHeaderPatterns == null) ? new HashSet<>() : allowedHeaderPatterns;
        return this;
    }

    /**
     * Adds the given pattern to the header patterns set that determine if a header should be logged.
     *
     * <p>The pattern allows headers to be match via a wildcard, for example {@code x-ms-meta-*} will match all headers
     * that begin with {@code x-ms-meta-}.</p>
     *
     * @param allowedHeaderPattern The header pattern.
     * @return The updated HttpLogOptions object.
     * @throws NullPointerException If {@code allowedHeaderPattern} is {@code null}.
     * @throws PatternSyntaxException If {@code allowedHeaderPattern} is an invalid pattern.
     */
    public HttpLogOptions addAllowedHeaderPattern(final String allowedHeaderPattern) {
        Pattern.compile(allowedHeaderPattern);
        this.allowedHeaderPatterns.add(Objects.requireNonNull(allowedHeaderPattern));
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
}
