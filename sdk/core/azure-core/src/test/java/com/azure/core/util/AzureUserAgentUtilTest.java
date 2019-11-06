// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link AzureUserAgentUtil}.
 */
public class AzureUserAgentUtilTest {

    @Test
    public void testUserAgentUtil() {
        assertNotNull(AzureUserAgentUtil.getUserAgentProperties("azure-core.properties").getVersion());
        assertNotNull(AzureUserAgentUtil.getUserAgentProperties("azure-core.properties").getName());
        assertTrue(AzureUserAgentUtil.getUserAgentProperties("azure-core.properties").getVersion()
            .matches("\\d.\\d.\\d([-a-zA-Z0-9.])*"));
    }
}
