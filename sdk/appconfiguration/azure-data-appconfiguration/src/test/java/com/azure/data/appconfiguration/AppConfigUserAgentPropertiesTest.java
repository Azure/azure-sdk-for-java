package com.azure.data.appconfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.azure.core.util.AzureUserAgentUtil;
import org.junit.Test;

/**
 * Unit test for reading app config user agent properties.
 */
public class AppConfigUserAgentPropertiesTest {

    @Test
    public void testAzureConfiguration() {
        assertNotNull(AzureUserAgentUtil.getUserAgentProperties("azure-data-appconfiguration.properties").getVersion());
        assertNotNull(AzureUserAgentUtil.getUserAgentProperties("azure-data-appconfiguration.properties").getName());
        assertTrue(AzureUserAgentUtil.getUserAgentProperties("azure-data-appconfiguration.properties").getVersion()
            .matches("\\d.\\d.\\d([-a-zA-Z0-9.])*"));
    }
}
