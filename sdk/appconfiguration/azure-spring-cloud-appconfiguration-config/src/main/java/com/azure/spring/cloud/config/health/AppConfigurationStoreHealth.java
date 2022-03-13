// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.health;

/**
 * App Configuration Health states
 */
public enum AppConfigurationStoreHealth {
    
    /**
     * Last connection attempt to Configuration Store succeeded.
     */
    UP,
    /**
     * Last connection attempt to Configuration Store failed.
     */
    DOWN,
    /**
     * Configuration Store isn't loaded.
     */
    NOT_LOADED

}
