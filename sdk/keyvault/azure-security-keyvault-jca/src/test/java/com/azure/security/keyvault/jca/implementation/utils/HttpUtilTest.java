// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_USER_AGENT_VALUE_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.VERSION;
import static org.junit.jupiter.api.Assertions.*;

public class HttpUtilTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX, HttpUtil.getUserAgentPrefix());
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX + VERSION, HttpUtil.USER_AGENT_VALUE);
    }

    @Test
    @Disabled("Disable this because it will cause pipeline failure: https://dev.azure.com/azure-sdk/internal/_build/results?buildId=1196171&view=logs&j=4a83f3be-c53d-53dd-7954-86872056fb11&t=54174aae-5a55-579d-08e2-94fb446f7b77&l=29")
    public void testHttpUtilGet() {
        String url = "https://mvnrepository.com/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Disabled("This is only used to test in localhost manually")
    public void testHttpUtilGet1() {
        String url = "http://localhost:8000/";
        String result = HttpUtil.get(url, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
