/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

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
     * @param text
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
