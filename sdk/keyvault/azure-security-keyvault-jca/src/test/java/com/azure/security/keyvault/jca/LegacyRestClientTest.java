// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;

import static com.azure.security.keyvault.jca.LegacyRestClient.DEFAULT_USER_AGENT_VALUE_PREFIX;
import static com.azure.security.keyvault.jca.LegacyRestClient.DEFAULT_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LegacyRestClientTest {

    @Test
    public void getUserAgentPrefixTest() {
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX, LegacyRestClient.getUserAgentPrefix());
        assertEquals(DEFAULT_USER_AGENT_VALUE_PREFIX + DEFAULT_VERSION, LegacyRestClient.USER_AGENT_VALUE);
    }
}
