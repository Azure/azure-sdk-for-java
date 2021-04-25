// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.http.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the RestClientFactory class.
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
