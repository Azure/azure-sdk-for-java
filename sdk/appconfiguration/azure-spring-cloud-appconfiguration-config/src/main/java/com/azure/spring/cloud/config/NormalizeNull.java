// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import com.azure.data.appconfiguration.models.ConfigurationSetting;

/**
 * Used for Normalizing the Label value. A null label needs to be \0 while searching with null returns all
 * configurations with any label.
 *
 */
public class NormalizeNull {

    private static final String EMPTY_LABEL = "\0";

    /**
     * Normalizes the null value to \0.
     * 
     * @param setting ConfigurationSetting
     * @return ConfigurationSetting with label corrected from null to \0
     */
    public static ConfigurationSetting normalizeNullLabel(ConfigurationSetting setting) {
        return setting.getLabel() == null ? setting.setLabel(EMPTY_LABEL) : setting;
    }

}
