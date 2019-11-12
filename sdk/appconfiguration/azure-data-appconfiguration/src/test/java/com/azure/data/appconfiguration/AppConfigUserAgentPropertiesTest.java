// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

/**
 * Unit test for reading app config user agent properties.
 */
public class AppConfigUserAgentPropertiesTest {

    @Test
    public void testAzureConfiguration() {
        assertNotNull(CoreUtils.getProperties("azure-data-appconfiguration.properties").get("version"));
        assertNotNull(CoreUtils.getProperties("azure-data-appconfiguration.properties").get("name"));
        assertTrue(CoreUtils.getProperties("azure-data-appconfiguration.properties").get("version")
            .matches("\\d.\\d.\\d([-a-zA-Z0-9.])*"));
    }
}
