// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import org.junit.jupiter.api.Test;

import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.USER_AGENT_VALUE;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpUtilTest {

    @Test
    void userAgent() {
        assertEquals("az-sp-kv-jca/", HttpUtil.getUserAgentPrefix());
        assertEquals("az-sp-kv-jca/" + VERSION, USER_AGENT_VALUE);
    }
}
