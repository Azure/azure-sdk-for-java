package com.azure.core.implementation.configuration;

public enum Configurations {

    /**
     * URI of the proxy for HTTP connections.
     *
     * No proxy is configured by default.
     */
    HTTP_PROXY,

    /**
     * URI of the proxy for HTTPS connections.
     *
     * No proxy is configured by default..
     */
    HTTPS_PROXY,

    /**
     * List of hosts or CIDR to not proxy.
     *
     * Not proxy is empty by default.
     */
    NO_PROXY,

    /**
     * Automatically generated secret for MSI.
     */
    MSI_SECRET,

    /**
     * User agent to use for HTTP requests.
     */
    AZURE_USER_AGENT,

    /**
     * Minimum logging level to log.
     *
     * Logging is disabled by default.
     */
    AZURE_LOG_LEVEL,

    /**
     * Log location (e.g. console).
     */
    AZURE_LOG_LOCATION,

    /**
     * If set, disables distributed tracing.
     *
     * Tracing is enabled by default.
     */
    AZURE_NO_TRACING
}
