// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.utils.configuration.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration options for HTTP instrumentation.
 * <p>
 * The instrumentation emits distributed traces following <a href="https://github.com/open-telemetry/semantic-conventions/blob/main/docs/http/http-spans.md">OpenTelemetry HTTP semantic conventions</a>
 * and, when enabled, detailed HTTP logs.
 * <p>
 * The following information is recorded on distributed traces:
 * <ul>
 *     <li>Request method, URI. The URI is sanitized based on allowed query parameters configurable with {@link #setAllowedQueryParamNames(Set)} and {@link #addAllowedQueryParamName(String)}</li>
 *     <li>Response status code</li>
 *     <li>Error details if the request fails</li>
 *     <li>Time it takes to receive response</li>
 *     <li>Correlation identifiers</li>
 * </ul>
 *
 The following information is recorded on detailed HTTP logs:
 * <ul>
 *     <li>Request method, URI, and body size. URI is sanitized based on allowed query parameters configurable with {@link #setAllowedQueryParamNames(Set)} and {@link #addAllowedQueryParamName(String)}</li>
 *     <li>Response status code and body size</li>
 *     <li>Request and response headers from allow-list configured via {@link #setAllowedHeaderNames(Set)} and {@link #addAllowedHeaderName(HttpHeaderName)}.</li>
 *     <li>Error details if the request fails</li>
 *     <li>Time it takes to receive response</li>
 *     <li>Correlation identifiers</li>
 *     <li>When content logging is enabled via {@link HttpLogLevel#BODY_AND_HEADERS}: request and response body, and time-to-last-byte</li>
 * </ul>
 *
 * Client libraries auto-discover global OpenTelemetry SDK instance configured by the java agent or
 * in the application code. Just create a client instance as usual as shown in the following code snippet:
 *
 * <p><strong>Clients auto-discover global OpenTelemetry</strong></p>
 *
 * <!-- src_embed io.clientcore.core.telemetry.useglobalopentelemetry -->
 * <pre>
 *
 * AutoConfiguredOpenTelemetrySdk.initialize&#40;&#41;;
 *
 * SampleClient client = new SampleClientBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; this call will be traced using OpenTelemetry SDK initialized globally
 * client.clientCall&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.useglobalopentelemetry -->
 * <p>
 *
 * Alternatively, application developers can pass OpenTelemetry SDK instance explicitly to the client libraries.
 *
 * <p><strong>Pass configured OpenTelemetry instance explicitly</strong></p>
 *
 * <!-- src_embed io.clientcore.core.telemetry.useexplicitopentelemetry -->
 * <pre>
 *
 * OpenTelemetry openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize&#40;&#41;.getOpenTelemetrySdk&#40;&#41;;
 * HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions&#40;&#41;
 *     .setTelemetryProvider&#40;openTelemetry&#41;;
 *
 * SampleClient client = new SampleClientBuilder&#40;&#41;.instrumentationOptions&#40;instrumentationOptions&#41;.build&#40;&#41;;
 *
 * &#47;&#47; this call will be traced using OpenTelemetry SDK provided explicitly
 * client.clientCall&#40;&#41;;
 *
 * </pre>
 * <!-- end io.clientcore.core.telemetry.useexplicitopentelemetry -->
 */
@Metadata(properties = MetadataProperties.FLUENT)
public final class HttpInstrumentationOptions extends InstrumentationOptions {
    private HttpLogLevel logLevel;
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

    static final HttpLogLevel ENVIRONMENT_HTTP_LOG_LEVEL
        = HttpLogLevel.fromConfiguration(Configuration.getGlobalConfiguration());
    private static final List<String> DEFAULT_QUERY_PARAMS_ALLOWLIST = Collections.singletonList("api-version");

    /**
     * Creates a new instance using default options:
     * <ul>
     *     <li>HTTP logging is disabled.</li>
     *     <li>Distributed tracing is enabled.</li>
     * </ul>
     */
    public HttpInstrumentationOptions() {
        super();
        logLevel = ENVIRONMENT_HTTP_LOG_LEVEL;
        isRedactedHeaderNamesLoggingEnabled = true;
        allowedHeaderNames = new HashSet<>(DEFAULT_HEADERS_ALLOWLIST);
        allowedQueryParamNames = new HashSet<>(DEFAULT_QUERY_PARAMS_ALLOWLIST);
    }

    /**
     * Gets the level for HTTP request logs. Default is {@link HttpLogLevel#NONE}.
     * <p>
     * When HTTP logging is disabled, basic information about the request and response is still recorded
     * on distributed tracing spans.
     *
     * @return The {@link HttpLogLevel}.
     */
    public HttpLogLevel getHttpLogLevel() {
        return logLevel;
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
    public HttpInstrumentationOptions setRedactedHeaderNamesLoggingEnabled(boolean redactedHeaderNamesLoggingEnabled) {
        isRedactedHeaderNamesLoggingEnabled = redactedHeaderNamesLoggingEnabled;
        return this;
    }

    /**
     * Sets the level for HTTP request logs.
     * Default is {@link HttpLogLevel#NONE}.
     *
     * @param logLevel The {@link HttpLogLevel}.
     *
     * @return The updated {@link HttpInstrumentationOptions} object.
     */
    public HttpInstrumentationOptions setHttpLogLevel(HttpLogLevel logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * Gets the allowed headers that should be logged when they appear on the request or response.
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
    public HttpInstrumentationOptions setAllowedHeaderNames(final Set<HttpHeaderName> allowedHeaderNames) {
        this.allowedHeaderNames = allowedHeaderNames == null ? new HashSet<>() : new HashSet<>(allowedHeaderNames);

        return this;
    }

    /**
     * Sets the given allowed header to the default header set that should be logged when they appear on the request or response.
     * <p>
     * Note: headers are not recorded on traces.
     *
     * @param allowedHeaderName The allowed header name from the user.
     *
     * @return The updated HttpLogOptions object.
     *
     * @throws NullPointerException If {@code allowedHeaderName} is {@code null}.
     */
    public HttpInstrumentationOptions addAllowedHeaderName(final HttpHeaderName allowedHeaderName) {
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
    public HttpInstrumentationOptions setAllowedQueryParamNames(final Set<String> allowedQueryParamNames) {
        this.allowedQueryParamNames
            = allowedQueryParamNames == null ? new HashSet<>() : new HashSet<>(allowedQueryParamNames);

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
    public HttpInstrumentationOptions addAllowedQueryParamName(final String allowedQueryParamName) {
        this.allowedQueryParamNames.add(allowedQueryParamName);
        return this;
    }

    @Override
    public HttpInstrumentationOptions setTracingEnabled(boolean isTracingEnabled) {
        super.setTracingEnabled(isTracingEnabled);
        return this;
    }

    @Override
    public HttpInstrumentationOptions setMetricsEnabled(boolean isMetricsEnabled) {
        super.setMetricsEnabled(isMetricsEnabled);
        return this;
    }

    @Override
    public HttpInstrumentationOptions setTelemetryProvider(Object telemetryProvider) {
        super.setTelemetryProvider(telemetryProvider);
        return this;
    }

    /**
     * The level for HTTP request logs.
     */
    public enum HttpLogLevel {
        /**
         * HTTP logging is turned off.
         */
        NONE,

        /**
         * Enables logging the following information on detailed HTTP logs
         * <ul>
         *     <li>Request method, URI, and body size. URI is sanitized based on allowed query parameters configurable with {@link #setAllowedQueryParamNames(Set)} and {@link #addAllowedQueryParamName(String)}</li>
         *     <li>Response status code and body size</li>
         *     <li>Request and response headers from allow-list configured via {@link #setAllowedHeaderNames(Set)} and {@link #addAllowedHeaderName(HttpHeaderName)}.</li>
         *     <li>Error details if the request fails</li>
         *     <li>Time it takes to receive response</li>
         *     <li>Correlation identifiers</li>
         * </ul>
         */
        HEADERS,

        /**
         * Enables logging the following information on detailed HTTP logs
         * <ul>
         *     <li>Request method, URI, and body size. URI is sanitized based on allowed query parameters configurable with {@link #setAllowedQueryParamNames(Set)} and {@link #addAllowedQueryParamName(String)}</li>
         *     <li>Response status code and body size</li>
         *     <li>Error details if the request fails</li>
         *     <li>Time it takes to receive response</li>
         *     <li>Correlation identifiers</li>
         *     <li>Request and response bodies</li>
         *     <li>Time-to-last-byte</li>
         * </ul>
         *
         * <p>
         * The request and response body will be buffered into memory even if it is never consumed by an application, possibly impacting
         * performance.
         * <p>
         * Body is not logged (and not buffered) for requests and responses where the content length is not known or greater than 16KB.
         */
        BODY,

        /**
         * Enables logging everything in {@link #HEADERS} and {@link #BODY}.
         *
         * <p>
         * The request and response body will be buffered into memory even if it is never consumed by an application, possibly impacting
         * performance.
         * <p>
         * Body is not logged (and not buffered) for requests and responses where the content length is not known or greater than 16KB.
         */
        BODY_AND_HEADERS;

        private static final String HEADERS_VALUE = "headers";
        private static final String BODY_VALUE = "body";
        private static final String BODY_AND_HEADERS_VALUE = "body_and_headers";

        static HttpLogLevel fromConfiguration(Configuration configuration) {
            String logLevel = configuration.get(Configuration.HTTP_LOG_LEVEL);
            if (logLevel == null) {
                logLevel = configuration.get("http.log.level");
            }

            if (HEADERS_VALUE.equalsIgnoreCase(logLevel)) {
                return HEADERS;
            } else if (BODY_VALUE.equalsIgnoreCase(logLevel)) {
                return BODY;
            } else if (BODY_AND_HEADERS_VALUE.equalsIgnoreCase(logLevel)) {
                return BODY_AND_HEADERS;
            } else {
                return NONE;
            }
        }
    }
}
