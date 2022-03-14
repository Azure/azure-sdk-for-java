// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_TRACING_DISABLED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests the configuration API.
 */
public class ConfigurationTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";
    private static final String DEFAULT_VALUE = "theDefaultValueGhi789";

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(null);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    @Test
    public void environmentConfigurationFound() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(null);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        Configuration configuration = new Configuration();
        assertNull(configuration.get(MY_CONFIGURATION));
        assertFalse(configuration.contains(MY_CONFIGURATION));
    }

    /**
     * Verifies that runtime parameters are preferred over environment variables.
     */
    @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(UNEXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that a found configuration value is preferred over the default value.
     */
    @Test
    public void foundConfigurationPreferredOverDefault() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION, DEFAULT_VALUE));
    }

    /**
     * Verifies that when a configuration value isn't found the default will be returned.
     */
    @Test
    public void fallbackToDefaultConfiguration() {
        Configuration configuration = new Configuration();

        assertEquals(DEFAULT_VALUE, configuration.get(MY_CONFIGURATION, DEFAULT_VALUE));
    }

    /**
     * Verifies that a found configuration value is able to be mapped.
     */
    @Test
    public void foundConfigurationIsConverted() {
        Configuration configuration = spy(Configuration.class);
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE.toUpperCase(), configuration.get(MY_CONFIGURATION, String::toUpperCase));
    }

    /**
     * Verifies that when a configuration isn't found the converter returns null.
     */
    @Test
    public void notFoundConfigurationIsConvertedToNull() {
        assertNull(new Configuration().get(MY_CONFIGURATION, String::toUpperCase));
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
