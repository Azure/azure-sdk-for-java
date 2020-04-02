// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_TRACING_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        String configurationValue = Configuration.getGlobalConfiguration()
            .get(environmentConfigurationName, defaultConfiguration);

        assertEquals(environmentConfiguration, configurationValue);
    }

    /**
     * Verifies that when a configuration value isn't found the default will be returned.
     */
    @Test
    public void fallbackToDefaultConfiguration() {
        String configurationValue = Configuration.getGlobalConfiguration()
            .get("invalidConfiguration", defaultConfiguration);

        assertEquals(defaultConfiguration, configurationValue);
    }

    /**
     * Verifies that a found configuration value is able to be mapped.
     */
    @Test
    public void foundConfigurationIsConverted() {
        String configurationValue = Configuration.getGlobalConfiguration()
            .get(runtimeConfigurationName, String::toUpperCase);

        assertEquals(runtimeConfiguration.toUpperCase(), configurationValue);
    }

    /**
     * Verifies that when a configuration isn't found the converter returns null.
     */
    @Test
    public void notFoundConfigurationIsConvertedToNull() {
        assertNull(new Configuration().get("invalidConfiguration", String::toUpperCase));
    }

    @Test
    public void cloneConfiguration() {
        Configuration configuration = new Configuration()
            .put("variable1", "value1")
            .put("variable2", "value2");

        Configuration configurationClone = configuration.clone();

        // Verify that the clone has the expected values.
        assertEquals(configuration.get("variable1"), configurationClone.get("variable1"));
        assertEquals(configuration.get("variable2"), configurationClone.get("variable2"));

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("variable2");
        assertTrue(configuration.contains("variable2"));
    }

    @Test
    public void loadValueTwice() {
        Configuration configuration = new Configuration();
        String tracingDisabled = configuration.get(PROPERTY_AZURE_TRACING_DISABLED);
        String tracingDisabled2 = configuration.get(PROPERTY_AZURE_TRACING_DISABLED);

        assertEquals(tracingDisabled, tracingDisabled2);
    }

    @ParameterizedTest
    @MethodSource("getOrDefaultSupplier")
    public void getOrDefault(String configurationValue, Object defaultValue, Object expectedValue) {
        Configuration configuration = new Configuration()
            .put("getOrDefault", configurationValue);

        assertEquals(expectedValue, configuration.get("getOrDefault", defaultValue));
    }

    private static Stream<Arguments> getOrDefaultSupplier() {
        return Stream.of(
            Arguments.of(String.valueOf((byte) 42), (byte) 12, (byte) 42),
            Arguments.of(String.valueOf((short) 42), (short) 12, (short) 42),
            Arguments.of(String.valueOf(42), 12, 42),
            Arguments.of(String.valueOf(42L), 12L, 42L),
            Arguments.of(String.valueOf(42F), 12F, 42F),
            Arguments.of(String.valueOf(42D), 12D, 42D),
            Arguments.of(String.valueOf(true), false, true),
            Arguments.of("42", "12", "42")
        );
    }

    @Test
    public void getOrDefaultReturnsDefault() {
        assertEquals("42", new Configuration().get("empty", "42"));
    }
}
