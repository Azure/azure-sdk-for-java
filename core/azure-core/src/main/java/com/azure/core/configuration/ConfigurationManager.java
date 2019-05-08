// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.configuration;

import com.azure.core.implementation.util.ImplUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ConfigurationManager {
    private static List<? extends ConfigurationGetter> getters = Arrays.asList(
        new RuntimeConfigurationGetter(),
        new ConfigurationStoreConfigurationGetter(),
        new EnvironmentConfigurationGetter());

    public static void setConfigurationGetters(List<? extends ConfigurationGetter> getters) {
        ConfigurationManager.getters = getters;
    }

    public static String getConfiguration(Configurations configuration) {
        String configurationName = configuration.toString().toUpperCase(Locale.US);

        for (ConfigurationGetter getter : getters) {
            String configurationValue = getter.getConfiguration(configurationName);
            if (!ImplUtils.isNullOrEmpty(configurationValue)) {
                return configurationValue;
            }
        }

        return null;
    }
}
