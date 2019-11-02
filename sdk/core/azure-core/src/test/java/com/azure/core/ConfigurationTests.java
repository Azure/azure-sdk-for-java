// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import org.junit.jupiter.api.Disabled;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the configuration API.
 */
@Disabled("TEMPORARY")
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
        assertNotNull(Configuration.getGlobalConfiguration().get(runtimeConfigurationName));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    @Test
    public void environmentConfigurationFound() {
        assertNotNull(Configuration.getGlobalConfiguration().get(environmentConfigurationName));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        assertNull(Configuration.getGlobalConfiguration().get("invalidConfiguration"));
    }

    /**
     * Verifies that runtime parameters are preferred over environment variables.
     */
    @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        String configurationValue = Configuration.getGlobalConfiguration().get(runtimeOverEnvironmentName);
        assertEquals(runtimeConfiguration, configurationValue);
    }

    /**
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        String configurationValue = Configuration.getGlobalConfiguration().get(environmentConfigurationName, defaultConfiguration);
        assertEquals(environmentConfiguration, configurationValue);
    }

    /**
     * Verifies that when a configuration value isn't found the default will be returned.
     */
    @Test
    public void fallbackToDefaultConfiguration() {
        String configurationValue = Configuration.getGlobalConfiguration().get("invalidConfiguration", defaultConfiguration);
        assertEquals(defaultConfiguration, configurationValue);
    }

    /**
     * Verifies that a found configuration value is able to be mapped.
     */
    @Test
    public void foundConfigurationIsConverted() {
        String configurationValue = Configuration.getGlobalConfiguration().get(runtimeConfigurationName, String::toUpperCase);
        assertEquals(runtimeConfiguration.toUpperCase(), configurationValue);
    }

    /**
     * Verifies that when a configuration isn't found the converter returns null.
     */
    @Test
    public void notFoundConfigurationIsConvertedToNull() {
        assertNull(Configuration.getGlobalConfiguration().get("invalidConfiguration", String::toUpperCase));
    }

    @Test
    public void logLevelUpdatesInstantly() {
        String initialLogLevel = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL);

        try {
            System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, "123456789");
            assertNotEquals(initialLogLevel, Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_LOG_LEVEL));
        } finally {
            // Cleanup the test
            if (initialLogLevel != null) {
                System.setProperty(Configuration.PROPERTY_AZURE_LOG_LEVEL, initialLogLevel);
            }
        }
    }

    @Test
    public void tracingDisabledUpdatesInstantly() {
        boolean initialTracingDisabled = Boolean.parseBoolean(Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED));

        try {
            System.setProperty(Configuration.PROPERTY_AZURE_TRACING_DISABLED, Boolean.toString(!initialTracingDisabled));
            assertNotEquals(initialTracingDisabled, Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED));
        } finally {
            // Cleanup the test
            System.setProperty(Configuration.PROPERTY_AZURE_TRACING_DISABLED, Boolean.toString(initialTracingDisabled));
        }
    }
}
