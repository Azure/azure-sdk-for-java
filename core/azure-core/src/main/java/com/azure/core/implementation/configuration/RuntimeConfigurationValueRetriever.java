// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.configuration;

import com.azure.core.implementation.util.ImplUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves configurations from the application runtime.
 */
public final class RuntimeConfigurationValueRetriever implements ConfigurationValueRetriever {
    private static final String LOG_MESSAGE = "Found configuration {} in the runtime variables.";
    private final Logger logger = LoggerFactory.getLogger(RuntimeConfigurationValueRetriever.class);

    @Override
    public String retrieve(String name) {
        try {
            String runtimeConfiguration = System.getProperty(name);

            if (!ImplUtils.isNullOrEmpty(runtimeConfiguration)) {
                logger.info(LOG_MESSAGE, name);
            }

            return runtimeConfiguration;
        } catch (SecurityException | NullPointerException | IllegalArgumentException ex) {
            return null;
        }
    }
}
