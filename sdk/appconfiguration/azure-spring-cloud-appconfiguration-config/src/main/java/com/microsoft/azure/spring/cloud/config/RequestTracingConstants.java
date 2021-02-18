// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

/**
 * Request Tracing values used to check
 */
public enum RequestTracingConstants {

    REQUEST_TRACING_DISABLED_ENVIRONMENT_VARIABLE("AZURE_APP_CONFIGURATION_TRACING_DISABLED"),
    AZURE_FUNCTIONS_ENVIRONMENT_VARIABLE("FUNCTIONS_EXTENSION_VERSION"),
    AZURE_WEB_APP_ENVIRONMENT_VARIABLE("WEBSITE_SITE_NAME"),
    KUBERNETES_ENVIRONMENT_VARIABLE("KUBERNETES_PORT"),
    REQUEST_TYPE_KEY("RequestType"),
    HOST_TYPE_KEY("Host"),
    CORRELATION_CONTEXT_HEADER("Correlation-Context");

    private final String text;

    /**
     * @param text
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
