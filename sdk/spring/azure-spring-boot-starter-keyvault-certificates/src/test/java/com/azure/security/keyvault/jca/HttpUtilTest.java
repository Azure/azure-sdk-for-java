// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import org.junit.jupiter.api.Test;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.DEFAULT_VERSION;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.USER_AGENT_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpUtilTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals("az-sp-kv-ct/", HttpUtil.getUserAgentPrefix());
        assertEquals("az-sp-kv-ct/" + DEFAULT_VERSION, USER_AGENT_VALUE);
    }
}
