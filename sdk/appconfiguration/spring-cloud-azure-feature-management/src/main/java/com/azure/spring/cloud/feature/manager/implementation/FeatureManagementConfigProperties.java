// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.implementation;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Feature Management configuration file properties.
 */
@ConfigurationProperties(prefix = FeatureManagementConfigProperties.CONFIG_PREFIX)
public class FeatureManagementConfigProperties {

    /**
     * Prefix used for defining feature management properties.
     */
    public static final String CONFIG_PREFIX = "spring.cloud.azure.feature.management";

    private boolean failFast = true;

    /**
     * @return the failFast
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * @param failFast the failFast to set
     */
    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * @return the configPrefix
     */
    public static String getConfigPrefix() {
        return CONFIG_PREFIX;
    }

}
