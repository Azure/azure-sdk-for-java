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
     * User agent to use for HTTP requests.
     */
    public static final String AZURE_USER_AGENT = "AZURE_USER_AGENT";

    /**
     * Minimum logging level to log.
     *
     * Logging is disabled by default.
     */
    public static final String AZURE_LOG_LEVEL = "AZURE_LOG_LEVEL";

    /**
     * Whether logging happens.
     */
    public static final String AZURE_LOGGING_ENABLED = "AZURE_LOGGING_ENABLED";

    /**
     * Whether tracing happens.
     */
    public static final String AZURE_TRACING_ENABLED = "AZURE_TRACING_ENABLED";

    /*
     * Configurations that are loaded into the global configuration store when the application starts.
     */
    static final String[] DEFAULT_CONFIGURATIONS = {
        HTTP_PROXY,
        HTTPS_PROXY,
        NO_PROXY,
        AZURE_USER_AGENT,
        AZURE_LOG_LEVEL,
        AZURE_LOGGING_ENABLED,
        AZURE_TRACING_ENABLED
    };
}
