// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.configuration;

/**
 * Represents the well-known, commonly expected, environment variables.
 */
public final class BaseConfigurations {

    private BaseConfigurations() {
    }

    /**
     * URI of the proxy for HTTP connections.
     *
     * No proxy is configured by default.
     */
    public static final String HTTP_PROXY = "HTTP_PROXY";

    /**
     * URI of the proxy for HTTPS connections.
     *
     * No proxy is configured by default..
     */
    public static final String HTTPS_PROXY = "HTTPS_PROXY";

    /**
     * List of hosts or CIDR to not proxy.
     *
     * Not proxy is empty by default.
     */
    public static final String NO_PROXY = "NO_PROXY";

    /**
     * Minimum severity for a message to be logged.
     *
     * Logging is disabled by default.
     */
    public static final String AZURE_LOG_LEVEL = "AZURE_LOG_LEVEL";

    /**
     * Disables telemetry.
     */
    public static final String AZURE_TELEMETRY_DISABLED = "AZURE_TELEMETRY_DISABLED";

    /**
     * Disables tracing.
     */
    public static final String AZURE_TRACING_DISABLED = "AZURE_TRACING_DISABLED";

    /*
     * Configurations that are loaded into the global configuration store when the application starts.
     */
    static final String[] DEFAULT_CONFIGURATIONS = {
        HTTP_PROXY,
        HTTPS_PROXY,
        NO_PROXY,
        AZURE_LOG_LEVEL,
        AZURE_TELEMETRY_DISABLED,
        AZURE_TRACING_DISABLED
    };
}
