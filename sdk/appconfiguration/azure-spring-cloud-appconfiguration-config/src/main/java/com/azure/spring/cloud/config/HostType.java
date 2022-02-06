// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

/**
 * The Types of Hosts checked in request tracing.
 */
public enum HostType {

    /**
     * Unidentified Host
     */
    UNIDENTIFIED(""),
    
    /**
     * Host is Azure Web App
     */
    AZURE_WEB_APP("AzureWebApp"),
    
    /**
     * Host is Azure Function
     */
    AZURE_FUNCTION("AzureFunction"),
    
    /**
     * Host is Kubernetes
     */
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
