// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
import static com.azure.core.util.CoreUtils.getDefaultTimeoutFromEnvironment;

/**
 * Utilities shared with HttpClient implementations.
 */
public final class HttpUtils {
    private static final String TEXT_EVENT_STREAM = "text/event-stream";
    private static final ClientLogger LOGGER = new ClientLogger(HttpUtils.class);

    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    private static final Duration DEFAULT_CONNECT_TIMEOUT;
    private static final Duration DEFAULT_WRITE_TIMEOUT;
    private static final Duration DEFAULT_RESPONSE_TIMEOUT;
    private static final Duration DEFAULT_READ_TIMEOUT;

    static {
        Configuration configuration = Configuration.getGlobalConfiguration();
        DEFAULT_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, Duration.ofSeconds(10), LOGGER);
        DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
            Duration.ofSeconds(60), LOGGER);
        DEFAULT_RESPONSE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, Duration.ofSeconds(60), LOGGER);
        DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_READ_TIMEOUT,
            Duration.ofSeconds(60), LOGGER);
    }

    /**
     * Context key used to indicate to an HttpClient implementation if it should eagerly read the response from the
     * network.
     */
    public static final String AZURE_EAGERLY_READ_RESPONSE = "azure-eagerly-read-response";

    /**
     * Context key that instructs REST proxy response ownership and HTTP response logging to preserve the response body
     * as a live stream. HTTP client implementations do not consume this key.
     */
    public static final String AZURE_PRESERVE_RESPONSE_BODY_AS_STREAM = "azure-preserve-response-body-as-stream";

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
     * Determines whether the response body must be preserved as a live stream.
     *
     * @param context Contextual information about the request.
     * @return Whether the response body must be preserved as a live stream.
     */
    public static boolean shouldPreserveResponseBodyAsStream(Context context) {
        return Boolean.TRUE.equals(context.getData(AZURE_PRESERVE_RESPONSE_BODY_AS_STREAM).orElse(false));
    }

    /**
     * Determines whether an Accept header contains an enabled {@code text/event-stream} media range.
     *
     * @param headerValue The header value.
     * @return Whether the header contains an enabled {@code text/event-stream} media range.
     */
    public static boolean acceptsTextEventStream(String headerValue) {
        if (headerValue == null) {
            return false;
        }

        for (String value : headerValue.split(",")) {
            String[] mediaRange = value.split(";");
            if (TEXT_EVENT_STREAM.equalsIgnoreCase(mediaRange[0].trim()) && hasPositiveQuality(mediaRange)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines whether a Content-Type header identifies exactly one {@code text/event-stream} representation.
     *
     * @param headerValue The Content-Type header value.
     * @return Whether the header identifies a {@code text/event-stream} representation.
     */
    public static boolean isTextEventStreamContentType(String headerValue) {
        if (headerValue == null || headerValue.indexOf(',') >= 0) {
            return false;
        }

        String[] mediaTypeAndParameters = headerValue.split(";");
        if (!TEXT_EVENT_STREAM.equalsIgnoreCase(mediaTypeAndParameters[0].trim())) {
            return false;
        }

        for (int i = 1; i < mediaTypeAndParameters.length; i++) {
            String parameter = mediaTypeAndParameters[i].trim();
            int equalsIndex = parameter.indexOf('=');
            if (equalsIndex > 0
                && "charset".equalsIgnoreCase(parameter.substring(0, equalsIndex).trim())
                && !isUtf8(parameter.substring(equalsIndex + 1).trim())) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasPositiveQuality(String[] mediaRange) {
        boolean qualityFound = false;
        for (int i = 1; i < mediaRange.length; i++) {
            String parameter = mediaRange[i].trim();
            int equalsIndex = parameter.indexOf('=');
            if (equalsIndex > 0 && "q".equalsIgnoreCase(parameter.substring(0, equalsIndex).trim())) {
                if (qualityFound
                    || !parameter.regionMatches(true, 0, "q=", 0, 2)
                    || !isValidPositiveQuality(parameter.substring(2))) {
                    return false;
                }
                qualityFound = true;
            }
        }

        return true;
    }

    private static boolean isValidPositiveQuality(String quality) {
        return quality.matches("0(?:\\.[0-9]{0,3})?|1(?:\\.0{0,3})?") && !quality.matches("0(?:\\.0{0,3})?");
    }

    private static boolean isUtf8(String charset) {
        return "utf-8".equalsIgnoreCase(unquote(charset));
    }

    private static String unquote(String value) {
        return value.length() > 1 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"'
            ? value.substring(1, value.length() - 1)
            : value;
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
