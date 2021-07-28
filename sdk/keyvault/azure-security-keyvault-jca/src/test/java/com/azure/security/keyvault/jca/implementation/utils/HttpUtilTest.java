// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_USER_AGENT_VALUE_PREFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HttpUtilTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX, HttpUtil.getUserAgentPrefix());
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX + DEFAULT_VERSION, HttpUtil.USER_AGENT_VALUE);
    }
}
