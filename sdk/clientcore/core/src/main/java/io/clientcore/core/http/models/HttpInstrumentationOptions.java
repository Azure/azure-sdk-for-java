// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.configuration.ConfigurationProperty;
import io.clientcore.core.util.configuration.ConfigurationPropertyBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration options for HTTP logging and tracing.
 *
 * @param <T> The type of the instrumentation provider.
 */
public final class HttpInstrumentationOptions<T> extends InstrumentationOptions<T> {
    private boolean isHttpLoggingEnabled;
    private boolean isContentLoggingEnabled;
    private boolean isRedactedHeaderNamesLoggingEnabled;
    private Set<HttpHeaderName> allowedHeaderNames;
    private Set<String> allowedQueryParamNames;
    private static final List<HttpHeaderName> DEFAULT_HEADERS_ALLOWLIST
        = Arrays.asList(HttpHeaderName.TRACEPARENT, HttpHeaderName.ACCEPT, HttpHeaderName.CACHE_CONTROL,
            HttpHeaderName.CONNECTION, HttpHeaderName.CONTENT_LENGTH, HttpHeaderName.CONTENT_TYPE, HttpHeaderName.DATE,
            HttpHeaderName.ETAG, HttpHeaderName.EXPIRES, HttpHeaderName.IF_MATCH, HttpHeaderName.IF_MODIFIED_SINCE,
            HttpHeaderName.IF_NONE_MATCH, HttpHeaderName.IF_UNMODIFIED_SINCE, HttpHeaderName.LAST_MODIFIED,
            HttpHeaderName.PRAGMA, HttpHeaderName.RETRY_AFTER, HttpHeaderName.SERVER, HttpHeaderName.TRANSFER_ENCODING,
            HttpHeaderName.USER_AGENT, HttpHeaderName.WWW_AUTHENTICATE);

    private static final List<String> DEFAULT_QUERY_PARAMS_ALLOWLIST = Collections.singletonList("api-version");

    private static final ConfigurationProperty<Boolean> HTTP_LOGGING_ENABLED
        = ConfigurationPropertyBuilder.ofBoolean("http.logging.enabled")
            .shared(true)
            .environmentVariableName(Configuration.PROPERTY_HTTP_LOGGING_ENABLED)
            .defaultValue(false)
            .build();

    private static final boolean DEFAULT_HTTP_LOGGING_ENABLED
        = Configuration.getGlobalConfiguration().get(HTTP_LOGGING_ENABLED);

    /**
     * Creates a new instance that does not log any information about HTTP requests or responses.
     */
    public HttpInstrumentationOptions() {
        super();
        isHttpLoggingEnabled = DEFAULT_HTTP_LOGGING_ENABLED;
        isContentLoggingEnabled = false;
        isRedactedHeaderNamesLoggingEnabled = true;
        allowedHeaderNames = new HashSet<>(DEFAULT_HEADERS_ALLOWLIST);
        allowedQueryParamNames = new HashSet<>(DEFAULT_QUERY_PARAMS_ALLOWLIST);
    }

    /**
     * Flag indicating whether HTTP request and response logging is enabled.
     * False by default.
     * <p>
     * When HTTP logging is disabled, basic information about the request and response is still recorded
     * via distributed tracing.
     *
     * @return True if logging is enabled, false otherwise.
     */
    public boolean isHttpLoggingEnabled() {
        return isHttpLoggingEnabled;
    }

    /**
     * Flag indicating whether HTTP request and response header values are added to the logs
     * when their name is not explicitly allowed via {@link HttpInstrumentationOptions#setAllowedHeaderNames(Set)} or
     * {@link HttpInstrumentationOptions#addAllowedHeaderName(HttpHeaderName)}.
     * True by default.
     *
     * @return True if redacted header names logging is enabled, false otherwise.
     */
    public boolean isRedactedHeaderNamesLoggingEnabled() {
        return isRedactedHeaderNamesLoggingEnabled;
    }

    /**
     * Enables or disables logging of redacted header names.
     * @param redactedHeaderNamesLoggingEnabled True to enable logging of redacted header names, false otherwise.
     *                                          Default is true.
     * @return The updated {@link HttpInstrumentationOptions} object.
     */
    public HttpInstrumentationOptions<T>
        setRedactedHeaderNamesLoggingEnabled(boolean redactedHeaderNamesLoggingEnabled) {
        isRedactedHeaderNamesLoggingEnabled = redactedHeaderNamesLoggingEnabled;
        return this;
    }

    /**
     * Flag indicating whether HTTP request and response body is logged.
     * False by default.
     * <p>
     * Note: even when content logging is explicitly enabled, it's not logged in the
     * following cases:
     * <ul>
     *     <li>When the content length is not known.</li>
     *     <li>When the content length is greater than 16KB.</li>
     * </ul>
     *
     * @return True if content logging is enabled, false otherwise.
     */
    public boolean isContentLoggingEnabled() {
        return isContentLoggingEnabled;
    }

    /**
     * Enables or disables logging of HTTP request and response.
     * False by default.
     * <p>
     * When HTTP logging is disabled, basic information about the request and response is still recorded
     * via distributed tracing.
     *
     * @param isHttpLoggingEnabled True to enable HTTP logging, false otherwise.
     * @return The updated {@link HttpInstrumentationOptions} object.
     */
    public HttpInstrumentationOptions<T> setHttpLoggingEnabled(boolean isHttpLoggingEnabled) {
        this.isHttpLoggingEnabled = isHttpLoggingEnabled;
        return this;
    }

    /**
     * Enables or disables logging of HTTP request and response body.
     * False by default.
     * <p>
     * Note: even when content logging is explicitly enabled, it's not logged in the
     * following cases:
     * <ul>
     *     <li>When the content length is not known.</li>
     *     <li>When the content length is greater than 16KB.</li>
     * </ul>
     *
     * @param isContentLoggingEnabled True to enable content logging, false otherwise.
     * @return The updated {@link HttpInstrumentationOptions} object.
     */
    public HttpInstrumentationOptions<T> setContentLoggingEnabled(boolean isContentLoggingEnabled) {
        this.isHttpLoggingEnabled |= isContentLoggingEnabled;
        this.isContentLoggingEnabled = isContentLoggingEnabled;
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
     * Note: headers are not recorded on traces.
     *
     * <p>
     * This method sets the provided header names to be the allowed header names which will be logged for all HTTP
     * requests and responses, overwriting any previously configured headers. Additionally, users can use
     * {@link HttpInstrumentationOptions#addAllowedHeaderName(HttpHeaderName)} or {@link HttpInstrumentationOptions#getAllowedHeaderNames()} to add or
     * remove more headers names to the existing set of allowed header names.
     * </p>
     *
     * @param allowedHeaderNames The list of allowed header names from the user.
     *
     * @return The updated HttpLogOptions object.
     */
    public HttpInstrumentationOptions<T> setAllowedHeaderNames(final Set<HttpHeaderName> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames == null ? new HashSet<>() : allowedHeaderNames;

        return this;
    }

    /**
     * Sets the given allowed header to the default header set that should be logged.
     * <p>
     * Note: headers are not recorded on traces.
     *
     * @param allowedHeaderName The allowed header name from the user.
     *
     * @return The updated HttpLogOptions object.
     *
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     */
    public HttpInstrumentationOptions<T> addAllowedHeaderName(final HttpHeaderName allowedHeaderName) {
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
     * Sets the given allowed query params to be recorded on logs and traces.
     *
     * @param allowedQueryParamNames The list of allowed query params from the user.
     *
     * @return The updated {@code allowedQueryParamName} object.
     */
    public HttpInstrumentationOptions<T> setAllowedQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames = allowedQueryParamNames == null ? new HashSet<>() : allowedQueryParamNames;

        return this;
    }

    /**
     * Sets the given allowed query param that can be recorded on logs and traces.
     *
     * @param allowedQueryParamName The allowed query param name from the user.
     *
     * @return The updated {@link HttpInstrumentationOptions} object.
     *
     * @throws NullPointerException If {@code allowedQueryParamName} is {@code null}.
     */
    public HttpInstrumentationOptions<T> addAllowedQueryParamName(final String allowedQueryParamName) {
        this.allowedQueryParamNames.add(allowedQueryParamName);
        return this;
    }

    @Override
    public HttpInstrumentationOptions<T> setTracingEnabled(boolean isTracingEnabled) {
        super.setTracingEnabled(isTracingEnabled);
        return this;
    }

    @Override
    public HttpInstrumentationOptions<T> setProvider(T provider) {
        super.setProvider(provider);
        return this;
    }
}
