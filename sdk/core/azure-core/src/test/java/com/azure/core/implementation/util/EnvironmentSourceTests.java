// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests the environment configuration source.
 */
public class EnvironmentSourceTests {
    private static final String MY_CONFIGURATION = "myConfigurationABC123";
    private static final String EXPECTED_VALUE = "aConfigurationValueAbc123";
    private static final String UNEXPECTED_VALUE = "notMyConfigurationValueDef456";
    private static final String DEFAULT_VALUE = "theDefaultValueGhi789";

    /**
     * Verifies that a runtime parameter is able to be retrieved.
     */
    @Test
    public void runtimeConfigurationFound() {
        EnvironmentConfigurationSource source = spy(EnvironmentConfigurationSource.class);
        when(source.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(source.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(null);

        assertEquals(EXPECTED_VALUE, source.getValue(MY_CONFIGURATION));
    }

    /**
     * Verifies that an environment variable is able to be retrieved.
     */
    @Test
    public void environmentConfigurationFound() {
        EnvironmentConfigurationSource source = spy(EnvironmentConfigurationSource.class);
        when(source.loadFromProperties(MY_CONFIGURATION)).thenReturn(null);
        when(source.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, source.getValue(MY_CONFIGURATION));
    }

    /**
     * Verifies that null is returned when a configuration isn't found.
     */
    @Test
    public void configurationNotFound() {
        EnvironmentConfigurationSource source = new EnvironmentConfigurationSource();
        assertNull(source.getValue(MY_CONFIGURATION));
    }

    /**
     * Verifies that runtime parameters are preferred over environment variables.
     */
    @Test
    public void runtimeConfigurationPreferredOverEnvironmentConfiguration() {
        EnvironmentConfigurationSource source = spy(EnvironmentConfigurationSource.class);
        when(source.loadFromProperties(MY_CONFIGURATION)).thenReturn(EXPECTED_VALUE);
        when(source.loadFromEnvironment(MY_CONFIGURATION)).thenReturn(UNEXPECTED_VALUE);

        assertEquals(EXPECTED_VALUE, source.getValue(MY_CONFIGURATION));
    }

    /**
     * Verifies that all supported environment variables are returned by getChildKeys.
     */
    @Test
    public void getChildKeysReturnsAllSupportedEnvVars() {
        EnvironmentConfigurationSource source = new EnvironmentConfigurationSource();

        Set<String> envVars = source.getChildKeys(null);
        assertTrue(envVars.contains(Configuration.PROPERTY_HTTP_PROXY));
        assertTrue(envVars.contains(Configuration.PROPERTY_HTTPS_PROXY));
        assertTrue(envVars.contains(Configuration.PROPERTY_IDENTITY_ENDPOINT));
        assertTrue(envVars.contains(Configuration.PROPERTY_IDENTITY_HEADER));
        assertTrue(envVars.contains(Configuration.PROPERTY_NO_PROXY));
        assertTrue(envVars.contains(Configuration.PROPERTY_MSI_ENDPOINT));
        assertTrue(envVars.contains(Configuration.PROPERTY_MSI_SECRET));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_USERNAME));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_PASSWORD));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_CLIENT_ID));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_CLIENT_SECRET));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_TENANT_ID));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_IDENTITY_DISABLE_CP1));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_RESOURCE_GROUP));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_CLOUD));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_AUTHORITY_HOST));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_LOG_LEVEL));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_TRACING_DISABLED));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_POD_IDENTITY_TOKEN_URL));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_REGIONAL_AUTHORITY_NAME));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT));
        assertTrue(envVars.contains(Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT));

        assertEquals(29, envVars.size());
    }

    /**
     * Verifies that get with prefix returns proper items.
     */
    @Test
    public void getChildKeysReturnsAllEnvVarsWithPrefix() {
        EnvironmentConfigurationSource source = new EnvironmentConfigurationSource();

        Set<String> envVars = source.getChildKeys("HTTP");
        assertTrue(envVars.contains(Configuration.PROPERTY_HTTP_PROXY));
        assertEquals(1, envVars.size());
        assertEquals(0, source.getChildKeys("http").size());
    }

    /**
     * Verifies that value can be returned for items out of initial set - environment source is special.
     */
    @Test
    public void getChildKeysReturnsValuesNotInInitialCollection() {
        EnvironmentConfigurationSource source = new EnvironmentConfigurationSource();

        String randomFoo = UUID.randomUUID().toString();
        System.setProperty(randomFoo, "bar");
        try {
            assertEquals("bar", source.getValue(randomFoo));
        } finally {
            System.clearProperty(randomFoo);
        }
    }
}
