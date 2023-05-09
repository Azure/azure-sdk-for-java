// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config;

/**
 * Status of the last connection attempt to the App Configuration Store
 */
public enum AppConfigurationStoreHealth {
    
    /**
     * The last connection to the App Configuration store was successful.
     */
    UP("UP"),
    
    /**
     * The last connection to the App Configuration store was unsuccessful.
     */
    DOWN("DOWN"),
    
    /**
     * Store is configured but disabled.
     */
    NOT_LOADED("NOT_LOADED");
    
    private final String text;
    
    AppConfigurationStoreHealth(final String text) {
        this.text = text;
    }
    
    @Override
    public String toString() {
        return text;
    }

}
