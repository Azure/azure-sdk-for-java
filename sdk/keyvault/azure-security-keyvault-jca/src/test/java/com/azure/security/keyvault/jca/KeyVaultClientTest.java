// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for the KeyVaultClient class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultClientTest {

    /**
     * Stores the KeyVault URI.
     */
    private static String keyVaultURI;

    /**
     * Setup before any test.
     */
    @BeforeAll
    public static void setUpClass() {
        keyVaultURI = System.getProperty("azure.keyvault.uri");
    }

    /**
     * Test getCertificate method.
     */
    @Test
    public void testGetCertificate() {
        KeyVaultClient client = new KeyVaultClient(keyVaultURI);
        assertNull(client.getCertificate("myalias"));
    }
}
