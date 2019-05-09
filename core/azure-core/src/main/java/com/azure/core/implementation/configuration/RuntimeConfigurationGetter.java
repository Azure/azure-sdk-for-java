// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.util.ImplUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves configurations from the application runtime.
 */
public final class RuntimeConfigurationGetter implements ConfigurationGetter {
    private static final String LOG_MESSAGE = "Found configuration {} in the runtime variables.";
    private final Logger logger = LoggerFactory.getLogger(RuntimeConfigurationGetter.class);

    @Override
    public String getConfiguration(String configurationName) {
        try {
            String runtimeConfiguration = System.getProperty(configurationName);

            if (!ImplUtils.isNullOrEmpty(runtimeConfiguration)) {
                logger.info(LOG_MESSAGE, configurationName);
            }

            return runtimeConfiguration;
        } catch (SecurityException | NullPointerException | IllegalArgumentException ex) {
            return null;
        }
    }
}
