// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.TestConfigurationSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests the configuration API.
 */
public class EnvironmentConfigurationTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        EnvironmentConfiguration configuration = spy(emptyConfiguration());
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(null);

        assertEquals(EXPECTED_VALUE, configuration.getSystemProperty(MY_CONFIGURATION));
        assertEquals(EXPECTED_VALUE, configuration.getAny(MY_CONFIGURATION));
        assertNull(configuration.getEnvironmentVariable(MY_CONFIGURATION));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    @Test
    public void environmentConfigurationFound() {
        EnvironmentConfiguration configuration = spy(emptyConfiguration());
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(null);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.getEnvironmentVariable(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        EnvironmentConfiguration configuration = emptyConfiguration();
        assertNull(configuration.getEnvironmentVariable(MY_CONFIGURATION));
    }

    /**
     * Verifies that runtime parameters are preferred over environment variables.
     */
    @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        EnvironmentConfiguration configuration = spy(emptyConfiguration());
        when(configuration.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(configuration.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(UNEXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, configuration.getSystemProperty(MY_CONFIGURATION));
    }

    private EnvironmentConfiguration emptyConfiguration() {
        return new EnvironmentConfiguration(new TestConfigurationSource(), new TestConfigurationSource());
    }

    @Test
    public void cloneConfiguration() {
        EnvironmentConfiguration configuration = new EnvironmentConfiguration(new TestConfigurationSource()
            .add("variable1", "value1"), path -> Collections.emptyMap());

        EnvironmentConfiguration configurationClone = new EnvironmentConfiguration(configuration);

        configuration.put("variable2", "value2");
        // Verify that the clone has the expected values.
        assertEquals(configuration.getEnvironmentVariable("variable1"), configurationClone.getEnvironmentVariable("variable1"));
        assertEquals(configuration.getEnvironmentVariable("variable2"), configurationClone.getEnvironmentVariable("variable2"));

        // The clone should be a separate instance, verify its modifications won't affect the original copy.
        configurationClone.remove("variable2");
        assertNull(configuration.getEnvironmentVariable("variable2"));
    }
}
