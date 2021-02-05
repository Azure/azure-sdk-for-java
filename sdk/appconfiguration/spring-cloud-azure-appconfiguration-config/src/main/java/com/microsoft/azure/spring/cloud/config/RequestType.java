/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

/**
 * The types of requests made to the App Configuration service.
 */
public enum RequestType {

    STARTUP("Startup"),
    WATCH("Watch");

    private final String text;

    /**
     * @param text
     */
    RequestType(final String text) {
        this.text = text;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

}
