// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.azure.core.util.CoreUtils.getDefaultTimeoutFromEnvironment;

/**
 * Utilities shared with HttpClient implementations.
 */
public final class HttpUtils {
    private static final String TEXT_EVENT_STREAM = "text/event-stream";
    private static final ClientLogger LOGGER = new ClientLogger(HttpUtils.class);

    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);

    /**
     * Context key used to indicate to an HttpClient implementation if it should eagerly read the response from the
     * network.
     */
    public static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";

    /**
     * Context key used to indicate to an HttpClient implementation if the response body should be ignored and eagerly
     * drained from the network.
     */
    public static final String AZURE_IGNORE_RESPONSE_BODY = "azure-ignore-response-body";

    /**
     * Context key used to indicate to an HttpClient a per-call response timeout.
     */
    public static final String AZURE_RESPONSE_TIMEOUT = "azure-response-timeout";

    /**
     * Context key used to indicate to an HttpClient if the implementation specific HTTP headers should be converted to
     * Azure Core HttpHeaders.
     */
    public static final String AZURE_EAGERLY_CONVERT_HEADERS = "azure-eagerly-convert-headers";

    /**
     * Determines whether a Content-Type header identifies exactly one {@code text/event-stream} representation.
     * Charset parameters don't affect this determination as event streams are always decoded as UTF-8.
     *
     * @param headerValue The Content-Type header value.
     * @return Whether the header identifies a {@code text/event-stream} representation.
     */
    public static boolean isTextEventStreamContentType(String headerValue) {
        if (headerValue == null) {
            return false;
        }

        List<String> mediaTypeAndParameters = splitHeaderValue(headerValue, ';');
        if (mediaTypeAndParameters.size() == 0
            || !TEXT_EVENT_STREAM.equalsIgnoreCase(mediaTypeAndParameters.get(0).trim())
            || splitHeaderValue(headerValue, ',').size() != 1) {
            return false;
        }

        return true;
    }

    private static List<String> splitHeaderValue(String value, char delimiter) {
        List<String> segments = new ArrayList<>();
        int start = 0;
        boolean quoted = false;
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (quoted && character == '\\') {
                escaped = true;
            } else if (character == '"') {
                quoted = !quoted;
            } else if (!quoted && character == delimiter) {
                segments.add(value.substring(start, i));
                start = i + 1;
            }
        }

        if (quoted) {
            return Collections.emptyList();
        }

        segments.add(value.substring(start));
        return segments;
    }

    /**
     * Gets the default connect timeout.
     *
     * @return The default connect timeout.
     */
    public static Duration getDefaultConnectTimeout() {
        return DEFAULT_CONNECT_TIMEOUT;
    }

    /**
     * Gets the default write timeout.
     *
     * @return The default write timeout.
     */
    public static Duration getDefaultWriteTimeout() {
        return DEFAULT_WRITE_TIMEOUT;
    }

    /**
     * Gets the default response timeout.
     *
     * @return The default response timeout.
     */
    public static Duration getDefaultResponseTimeout() {
        return DEFAULT_RESPONSE_TIMEOUT;
    }

    /**
     * Gets the default read timeout.
     *
     * @return The default read timeout.
     */
    public static Duration getDefaultReadTimeout() {
        return DEFAULT_READ_TIMEOUT;
    }

    /**
     * Resolves a timeout from the explicit value, the builder's configuration, the global configuration, and finally
     * the default, in that order. Missing, empty, or invalid configuration values fall back to the next source.
     * Nonpositive values disable the timeout; positive values less than one millisecond use one millisecond.
     *
     * @param configuredTimeout The explicitly configured timeout, or null.
     * @param configuration The builder's configuration, or null to use the global configuration.
     * @param timeoutPropertyName The timeout configuration property name.
     * @param defaultTimeout The fallback timeout.
     * @return The timeout to use.
     */
    public static Duration getTimeout(Duration configuredTimeout, Configuration configuration,
        String timeoutPropertyName, Duration defaultTimeout) {
        if (configuredTimeout != null) {
            return getTimeout(configuredTimeout, defaultTimeout);
        }

        Configuration globalConfiguration = Configuration.getGlobalConfiguration();
        Duration timeout = (configuration == null || configuration == globalConfiguration)
            ? null
            : getDefaultTimeoutFromEnvironment(configuration, timeoutPropertyName, null, LOGGER);

        return timeout == null
            ? getDefaultTimeoutFromEnvironment(globalConfiguration, timeoutPropertyName, defaultTimeout, LOGGER)
            : timeout;
    }

    /**
     * Returns the timeout Duration to use based on the configured timeout and the default timeout.
     * <p>
     * If the configured timeout is null the default timeout will be used. If the timeout is less than or equal to zero
     * no timeout will be used. If the timeout is less than one millisecond a timeout of one millisecond will be used.
     *
     * @param configuredTimeout The configured timeout.
     * @param defaultTimeout The default timeout.
     * @return The timeout to use.
     */
    public static Duration getTimeout(Duration configuredTimeout, Duration defaultTimeout) {
        // Timeout is null, use the default timeout.
        if (configuredTimeout == null) {
            return defaultTimeout;
        }

        // Timeout is less than or equal to zero, return no timeout.
        if (configuredTimeout.isZero() || configuredTimeout.isNegative()) {
            return Duration.ZERO;
        }

        // Return the maximum of the timeout period and the minimum allowed timeout period.
        if (configuredTimeout.compareTo(MINIMUM_TIMEOUT) < 0) {
            return MINIMUM_TIMEOUT;
        } else {
            return configuredTimeout;
        }
    }

    private HttpUtils() {
    }
}
