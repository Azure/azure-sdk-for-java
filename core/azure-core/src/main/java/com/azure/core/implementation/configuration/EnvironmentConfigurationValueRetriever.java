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
public final class EnvironmentConfigurationValueRetriever implements ConfigurationValueRetriever {
    private static final String LOG_MESSAGE = "Found configuration {} in the environment variables.";
    private static final Map<String, String> ENVIRONMENT_CACHE = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(EnvironmentConfigurationValueRetriever.class);

    @Override
    public String retrieve(String name) {
        if (ENVIRONMENT_CACHE.containsKey(name)) {
            return ENVIRONMENT_CACHE.get(name);
        }

        try {
            String environmentConfiguration = System.getenv(name);
            ENVIRONMENT_CACHE.put(name, environmentConfiguration);

            if (!ImplUtils.isNullOrEmpty(environmentConfiguration)) {
                logger.info(LOG_MESSAGE, name);
            }

            return environmentConfiguration;
        } catch (SecurityException | NullPointerException ex) {
            return null;
        }
    }
}
