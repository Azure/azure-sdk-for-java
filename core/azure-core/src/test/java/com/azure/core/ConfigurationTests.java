// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.configuration.BaseConfigurations;
import com.azure.core.configuration.ConfigurationManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the configuration API.
 */
public class ConfigurationTests {
    private final String runtimeConfigurationName = "configurationAPIRuntimeFound";
    private final String runtimeConfiguration = "runtimeConfiguration";

    private final String environmentConfigurationName = "configurationAPIEnvironmentFound";
    private final String environmentConfiguration = "environmentConfiguration";

    private final String runtimeOverEnvironmentName = "configurationAPIUseRuntimeFirst";
    private final String defaultConfiguration = "defaultConfiguration";

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        assertNotNull(ConfigurationManager.getConfiguration().get(runtimeConfigurationName));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    @Test
    public void environmentConfigurationFound() {
        assertNotNull(ConfigurationManager.getConfiguration().get(environmentConfigurationName));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        assertNull(ConfigurationManager.getConfiguration().get("invalidConfiguration"));
    }

    /**
     * Verifies that runtime parameters are preferred over environment variables.
     */
    @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        String configurationValue = ConfigurationManager.getConfiguration().get(runtimeOverEnvironmentName);
        assertEquals(runtimeConfiguration, configurationValue);
    }

    /**
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        String configurationValue = ConfigurationManager.getConfiguration().get(environmentConfigurationName, defaultConfiguration);
        assertEquals(environmentConfiguration, configurationValue);
    }

    /**
     * Verifies that when a configuration value isn't found the default will be returned.
     */
    @Test
    public void fallbackToDefaultConfiguration() {
        String configurationValue = ConfigurationManager.getConfiguration().get("invalidConfiguration", defaultConfiguration);
        assertEquals(defaultConfiguration, configurationValue);
    }

    /**
     * Verifies that a found configuration value is able to be mapped.
     */
    @Test
    public void foundConfigurationIsConverted() {
        String configurationValue = ConfigurationManager.getConfiguration().get(runtimeConfigurationName, String::toUpperCase);
        assertEquals(runtimeConfiguration.toUpperCase(), configurationValue);
    }

    /**
     * Verifies that when a configuration isn't found the converter returns null.
     */
    @Test
    public void notFoundConfigurationIsConvertedToNull() {
        assertNull(ConfigurationManager.getConfiguration().get("invalidConfiguration", String::toUpperCase));
    }

    @Test
    public void logLevelAndTracingDisabledUpdateInstantly() {
        assertNull(ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_LOG_LEVEL));
        System.setProperty(BaseConfigurations.AZURE_LOG_LEVEL, "2");
        assertEquals(2, (int) ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_LOG_LEVEL, 5));

        assertNull(ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_TELEMETRY_DISABLED));
        System.setProperty(BaseConfigurations.AZURE_TELEMETRY_DISABLED, Boolean.toString(true));
        assertTrue(ConfigurationManager.getConfiguration().get(BaseConfigurations.AZURE_TELEMETRY_DISABLED, false));
    }
}
