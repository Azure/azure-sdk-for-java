// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.LegacyRestClient;
import org.junit.jupiter.api.Test;

import static com.azure.security.keyvault.jca.implementation.LegacyRestClient.DEFAULT_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpUtilTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals("az-sp-kv-ct/", LegacyRestClient.getUserAgentPrefix());
        assertEquals("az-sp-kv-ct/" + DEFAULT_VERSION, LegacyRestClient.USER_AGENT_VALUE);
    }
}
