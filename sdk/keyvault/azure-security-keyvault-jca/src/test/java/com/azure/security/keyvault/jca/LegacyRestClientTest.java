// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the LegacyRestClient class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class LegacyRestClientTest {

    /**
     * Test constructor.
     */
    @Test
    public void testConstructor() {
        LegacyRestClient client = new LegacyRestClient();
        assertNotNull(client);
    }
}
