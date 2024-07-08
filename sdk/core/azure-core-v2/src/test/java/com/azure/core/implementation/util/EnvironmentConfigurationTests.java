// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.v2.util.ConfigurationSource;
import com.azure.core.v2.util.TestConfigurationSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the configuration API.
 */
public class EnvironmentConfigurationTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        EnvironmentConfiguration configuration = new EnvironmentConfiguration(
            new TestConfigurationSource().put(MY_CONFIGURATION, EXPECTED_VALUE), EMPTY_SOURCE);

        assertEquals(EXPECTED_VALUE, configuration.getSystemProperty(MY_CONFIGURATION));
        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
        assertNull(configuration.getEnvironmentVariable(MY_CONFIGURATION));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    @Test
    public void environmentConfigurationFound() {
        EnvironmentConfiguration configuration = new EnvironmentConfiguration(EMPTY_SOURCE,
            new TestConfigurationSource().put(MY_CONFIGURATION, EXPECTED_VALUE));

        assertEquals(EXPECTED_VALUE, configuration.getEnvironmentVariable(MY_CONFIGURATION));
        assertEquals(EXPECTED_VALUE, configuration.get(MY_CONFIGURATION));
        assertNull(configuration.getSystemProperty(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        EnvironmentConfiguration configuration = emptyConfiguration();
        assertNull(configuration.getEnvironmentVariable(MY_CONFIGURATION));
        assertNull(configuration.getSystemProperty(MY_CONFIGURATION));
        assertNull(configuration.get(MY_CONFIGURATION));
    }

    /**
     * Verifies that runtime parameters are preferred over environment variables.
     */
    @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        EnvironmentConfiguration configuration
            = new EnvironmentConfiguration(new TestConfigurationSource().put(MY_CONFIGURATION, EXPECTED_VALUE),
                new TestConfigurationSource().put(MY_CONFIGURATION, UNEXPECTED_VALUE));

        assertEquals(EXPECTED_VALUE, configuration.getSystemProperty(MY_CONFIGURATION));
    }

    @Test
    public void cloneConfiguration() {
        EnvironmentConfiguration configuration = new EnvironmentConfiguration(
            new TestConfigurationSource().put("sys", "sysVal"), new TestConfigurationSource().put("env", "envVal"));

        EnvironmentConfiguration configurationClone = new EnvironmentConfiguration(configuration);

        configuration.put("foo", "bar");

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("foo");
        assertNull(configurationClone.get("foo"));
        assertEquals("bar", configuration.get("foo"));
    }

    @Test
    public void removeDoesNotChangeEnvironmentOrSystemVariables() {
        EnvironmentConfiguration configuration = new EnvironmentConfiguration(
            new TestConfigurationSource().put("sys", "sysVal"), new TestConfigurationSource().put("env", "envVal"));

        configuration.put("foo", "bar");

        // Verify that the clone has the expected values.
        assertEquals("envVal", configuration.getEnvironmentVariable("env"));
        assertEquals("sysVal", configuration.getSystemProperty("sys"));
        assertEquals("bar", configuration.get("foo"));

        configuration.remove("foo");
        configuration.remove("env");
        configuration.remove("sys");
        assertNull(configuration.get("foo"));

        assertEquals("envVal", configuration.getEnvironmentVariable("env"));
        assertEquals("sysVal", configuration.getSystemProperty("sys"));
    }

    @Test
    public void putAndRemoveOverride() {
        EnvironmentConfiguration configuration = new EnvironmentConfiguration(
            new TestConfigurationSource().put("sys", "sysVal"), new TestConfigurationSource().put("env", "envVal"));

        configuration.put("env", "bar1");
        configuration.put("sys", "bar2");
        configuration.put("foo", "bar");

        assertEquals("bar1", configuration.get("env"));
        assertEquals("envVal", configuration.getEnvironmentVariable("env"));
        assertEquals("bar2", configuration.get("sys"));
        assertEquals("sysVal", configuration.getSystemProperty("sys"));
        assertEquals("bar", configuration.get("foo"));

        configuration.remove("foo");
        configuration.remove("env");
        configuration.remove("sys");
        assertNull(configuration.get("foo"));

        assertEquals("envVal", configuration.get("env"));
        assertEquals("envVal", configuration.getEnvironmentVariable("env"));
        assertEquals("sysVal", configuration.get("sys"));
        assertEquals("sysVal", configuration.getSystemProperty("sys"));
    }

    private EnvironmentConfiguration emptyConfiguration() {
        return new EnvironmentConfiguration(EMPTY_SOURCE, EMPTY_SOURCE);
    }

}
