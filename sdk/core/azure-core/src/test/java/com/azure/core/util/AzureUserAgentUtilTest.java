// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testUnknownUserAgentUtil() {
        assertNotNull(AzureUserAgentUtil.getUserAgentProperties("foo.properties").getVersion());
        assertNotNull(AzureUserAgentUtil.getUserAgentProperties("foo.properties").getName());
        assertEquals("UnknownVersion", AzureUserAgentUtil.getUserAgentProperties("foo.properties").getVersion());
        assertEquals("UnknownName", AzureUserAgentUtil.getUserAgentProperties("foo.properties").getName());

    }
}
