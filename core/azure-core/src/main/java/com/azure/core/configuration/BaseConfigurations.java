// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.configuration;

/**
 * Represents the well-known, commonly expected, environment variables.
 */
public final class BaseConfigurations {

    private BaseConfigurations() {
    }

    // Proxy Settings
    /**
     * URI of the proxy for HTTP connections.
     *
     * No proxy is configured by default.
     */
    public static final String HTTP_PROXY = "HTTP_PROXY";

    /**
     * URI of the proxy for HTTPS connections.
     *
     * No proxy is configured by default.
     */
    public static final String HTTPS_PROXY = "HTTPS_PROXY";

    /**
     * List of hosts or CIDR to not proxy.
     *
     * Not proxy is empty by default.
     */
    public static final String NO_PROXY = "NO_PROXY";

    // Identity
    /**
     * AAD MSI Credentials.
     */
    public static final String MSI_ENDPOINT = "MSI_ENDPOINT";

    /**
     * AAD MSI Credentials.
     */
    public static final String MSI_SECRET = "MSI_SECRET";

    /**
     * Azure subscription.
     */
    public static final String AZURE_SUBSCRIPTION_ID = "AZURE_SUBSCRIPTION_ID";

    /**
     * Azure username for U/P Auth.
     */
    public static final String AZURE_USERNAME = "AZURE_USERNAME";

    /**
     * Azure password for U/P Auth.
     */
    public static final String AZURE_PASSWORD = "AZURE_PASSWORD";

    /**
     * AAD
     */
    public static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";

    /**
     * AAD
     */
    public static final String AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";

    /**
     * AAD
     */
    public static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";

    /**
     * Azure resource group.
     */
    public static final String AZURE_RESOURCE_GROUP = "AZURE_RESOURCE_GROUP";

    /**
     * mooncake, govcloud, etc.
     */
    public static final String AZURE_CLOUD = "AZURE_CLOUD";

    // Pipeline Configuration
    /**
     * Disables telemetry.
     */
    public static final String AZURE_TELEMETRY_DISABLED = "AZURE_TELEMETRY_DISABLED";

    /**
     * Enable console logging by setting a log level.
     */
    public static final String AZURE_LOG_LEVEL = "AZURE_LOG_LEVEL";

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
        MSI_ENDPOINT,
        MSI_SECRET,
        AZURE_SUBSCRIPTION_ID,
        AZURE_USERNAME,
        AZURE_PASSWORD,
        AZURE_CLIENT_ID,
        AZURE_CLIENT_SECRET,
        AZURE_TENANT_ID,
        AZURE_RESOURCE_GROUP,
        AZURE_CLOUD,
        AZURE_TELEMETRY_DISABLED,
        AZURE_LOG_LEVEL,
        AZURE_TRACING_DISABLED,
    };
}
