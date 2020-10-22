// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the RestClientFactory class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class RestClientFactoryTest {

    /**
     * Test createClient method.
     */
    @Test
    public void testCreateClient() {
        assertNotNull(RestClientFactory.createClient());
    }
}
