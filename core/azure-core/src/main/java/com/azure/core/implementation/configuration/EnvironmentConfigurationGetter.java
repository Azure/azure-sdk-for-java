// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.util.ImplUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Retrieves configuration from the environment variables.
 */
public final class EnvironmentConfigurationGetter implements ConfigurationGetter {
    private static final String LOG_MESSAGE = "Found configuration {} in the environment variables.";
    private static final Map<String, String> ENVIRONMENT_CACHE = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(EnvironmentConfigurationGetter.class);

    @Override
    public String getConfiguration(String configurationName) {
        if (ENVIRONMENT_CACHE.containsKey(configurationName)) {
            return ENVIRONMENT_CACHE.get(configurationName);
        }

        try {
            String environmentConfiguration = System.getenv(configurationName);
            ENVIRONMENT_CACHE.put(configurationName, environmentConfiguration);

            if (!ImplUtils.isNullOrEmpty(environmentConfiguration)) {
                logger.info(LOG_MESSAGE, configurationName);
            }

            return environmentConfiguration;
        } catch (SecurityException | NullPointerException ex) {
            return null;
        }
    }
}
