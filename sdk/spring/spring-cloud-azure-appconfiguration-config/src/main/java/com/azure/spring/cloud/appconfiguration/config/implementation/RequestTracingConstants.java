// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

/**
 * Request Tracing values used to check
 */
public enum RequestTracingConstants {

    /**
     * Constant for Disabling Tracing
     */
    REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE("AZURE_APP_CONFIGURATION_TRACING_DISABLED"),
    
    /**
     * Constant for checking for use in Azure Functions
     */
    AZURE_FUNCTIONS_ENVIRONMENT_VARIABLE("FUNCTIONS_EXTENSION_VERSION"),
    
    /**
     * Constant for checking for use in Azure Web App
     */
    AZURE_WEB_APP_ENVIRONMENT_VARIABLE("WEBSITE_SITE_NAME"),
    
    /**
     * Constant for checking for use in Kubernetes
     */
    KUBERNETES_ENVIRONMENT_VARIABLE("KUBERNETES_PORT"),
    
    /**
     * Constant for checking for use in Container App.
     */
    CONTAINER_APP_ENVIRONMENT_VARIABLE("CONTAINER_APP_NAME"),
    
    /**
     * Constant for checking Service Fabric
     */
    SERVICE_FABRIC_ENVIRONMENT_VARIABLE("Fabric_NodeName"),
    
    /**
     * Constant for tracing the type of request
     */
    REQUEST_TYPE_KEY("RequestType"),
    
    /**
     * Constant for tracing the type of host
     */
    HOST_TYPE_KEY("Host"),
    
    /**
     * Constant for http Header Correlation Context
     */
    CORRELATION_CONTEXT_HEADER("Correlation-Context"),
    
    /**
     * Constant for number of replicas, not including origin configured.
     */
    REPLICA_COUNT("ReplicaCount"),

    /**
     * Constant for tracing the max variants count.
     */
    MAX_VARIANTS_KEY("MaxVariants"),

    /**
     * Constant for tracing feature flag features.
     */
    FF_FEATURES_KEY("FFFeatures"),

    /**
     * Constant for tracing general features.
     */
    FEATURES_KEY("Features"),

    /**
     * Constant for tracing the feature management version.
     */
    FM_SPRING_VER_KEY("FMSpVer"),

    /**
     * Constant for tracing if this is a failover request.
     */
    FAILOVER_TAG("Failover"),

    /**
     * Constant for tracing the filter key.
     */
    FILTER_KEY("Filter");

    private final String text;

    /**
     * @param text Request Tracing Constant Type
     */
    RequestTracingConstants(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

}
