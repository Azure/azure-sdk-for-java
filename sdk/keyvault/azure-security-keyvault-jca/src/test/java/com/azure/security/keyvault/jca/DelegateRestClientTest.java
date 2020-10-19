// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the DelegateRestClient class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class DelegateRestClientTest {

    /**
     * Test of getDelegate method, of class DelegateRestClient.
     */
    @Test
    public void testGetDelegate() {
        DelegateRestClient client = new DelegateRestClient(RestClientFactory.createClient());
        assertNotNull(client.getDelegate());
    }
}
