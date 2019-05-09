// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

/**
 * Represents the well-known, commonly expected, environment variables.
 */
public enum EnvironmentConfigurations {

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

    MSI_CREDENTIALS,

    /**
     * Automatically generated secret for MSI.
     */
    MSI_SECRET,

    AZURE_SUBSCRIPTION_ID,

    AZURE_USERNAME,

    AZURE_PASSWORD,

    AZURE_CLIENT_ID,

    AZURE_CLIENT_SECRET,

    AZURE_TENANT_ID,

    AZURE_RESOURCE_GROUP,

    AZURE_CLOUD,

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
    AZURE_TRACING_DISABLED,

    AZURE_TELEMETRY_DISABLED
}
