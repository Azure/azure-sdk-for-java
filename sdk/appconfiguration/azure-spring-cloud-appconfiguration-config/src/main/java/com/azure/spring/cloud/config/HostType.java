// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

/**
 * The Types of Hosts checked in request tracing.
 */
public enum HostType {

    UNIDENTIFIED(""),
    AZURE_WEB_APP("AzureWebApp"),
    AZURE_FUNCTION("AzureFunction"),
    KUBERNETES("Kubernetes");

    private final String text;

    /**
     * @param text Host that is connecting to Azure App Configuration.
     */
    HostType(final String text) {
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
