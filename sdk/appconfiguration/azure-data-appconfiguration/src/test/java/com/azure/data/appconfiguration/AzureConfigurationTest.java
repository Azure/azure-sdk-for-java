package com.azure.data.appconfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for {@link AzureConfiguration}.
 */
public class AzureConfigurationTest {

    @Test
    public void testAzureConfiguration() {
        assertNotNull(AzureConfiguration.getVersion());
        assertNotNull(AzureConfiguration.getName());
        assertTrue(AzureConfiguration.getVersion().matches("\\d.\\d.\\d([-a-zA-Z0-9.])*"));
    }
}
