// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.configuration;

import com.azure.core.implementation.util.ImplUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static List<? extends ConfigurationGetter> getters;
    private static Map<String, String> environmentCache;

    static {
        ConfigurationManager.getters = Arrays.asList(
            new RuntimeConfigurationGetter(),
            new ConfigurationStoreConfigurationGetter(), // This might no be what I think it is
            new EnvironmentConfigurationGetter());

        environmentCache = buildEnvironmentConfigurations();
    }

    public static void setConfigurationGetters(List<? extends ConfigurationGetter> getters) {
        ConfigurationManager.getters = getters;
    }

    public static String getConfiguration(EnvironmentConfigurations configuration) {
        return System.getenv(getConfigurationName(configuration));
    }

    public static String getConfiguration(String configurationName) {
        for (ConfigurationGetter getter : getters) {
            String configurationValue = getter.getConfiguration(configurationName);
            if (!ImplUtils.isNullOrEmpty(configurationValue)) {
                logger.info(getter.logMessage(configurationName));
                return configurationValue;
            }
        }

        return null;
    }

    private static String getConfigurationName(EnvironmentConfigurations configuration) {
        return configuration.toString().toUpperCase(Locale.US);
    }

    private static Map<String, String> buildEnvironmentConfigurations() {
        Map<String, String> environmentConfigurations = new HashMap<>();

        // Might need support for different casing, eg http_proxy, HTTP_PROXY, http_PROXY.
        for (EnvironmentConfigurations configuration : EnvironmentConfigurations.values()) {
            String configurationName = getConfigurationName(configuration);
            String configurationValue = System.getenv(configurationName);
            if (!ImplUtils.isNullOrEmpty(configurationValue)) {
                environmentConfigurations.put(configurationName, configurationValue);
            }
        }

        return environmentConfigurations;
    }
}
