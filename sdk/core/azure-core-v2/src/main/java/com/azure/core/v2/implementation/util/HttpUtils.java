// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.v2.implementation.util;

import io.clientcore.core.util.configuration.Configuration;
import io.clientcore.core.util.ClientLogger;

import java.time.Duration;

//import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
//import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
//import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT;
//import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;
//import static com.azure.core.util.CoreUtils.getDefaultTimeoutFromEnvironment;

/**
 * Utilities shared with HttpClient implementations.
 */
public final class HttpUtils {
    private static final ClientLogger LOGGER = new ClientLogger(HttpUtils.class);

    private static final Duration MINIMUM_TIMEOUT = Duration.ofMillis(1);
    //private static final Duration DEFAULT_CONNECT_TIMEOUT;
    //private static final Duration DEFAULT_WRITE_TIMEOUT;
    //private static final Duration DEFAULT_RESPONSE_TIMEOUT;
    //private static final Duration DEFAULT_READ_TIMEOUT;

    //static {
    //    Configuration configuration = Configuration.getGlobalConfiguration();
    //    DEFAULT_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
    //        PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT, Duration.ofSeconds(10), LOGGER);
    //    DEFAULT_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT,
    //        Duration.ofSeconds(60), LOGGER);
    //    DEFAULT_RESPONSE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
    //        PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT, Duration.ofSeconds(60), LOGGER);
    //    DEFAULT_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration, PROPERTY_AZURE_REQUEST_READ_TIMEOUT,
    //        Duration.ofSeconds(60), LOGGER);
    //}

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
     * Gets the default connect timeout.
     *
     * @return The default connect timeout.
     */
    public static Duration getDefaultConnectTimeout() {
        return null;
        // return DEFAULT_CONNECT_TIMEOUT;
    }

    /**
     * Gets the default write timeout.
     *
     * @return The default write timeout.
     */
    public static Duration getDefaultWriteTimeout() {
        return null;
        // return DEFAULT_WRITE_TIMEOUT;
    }

    /**
     * Gets the default response timeout.
     *
     * @return The default response timeout.
     */
    public static Duration getDefaultResponseTimeout() {
        return null;
        // return DEFAULT_RESPONSE_TIMEOUT;
    }

    /**
     * Gets the default read timeout.
     *
     * @return The default read timeout.
     */
    public static Duration getDefaultReadTimeout() {
        return null;
        // return DEFAULT_READ_TIMEOUT;
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
