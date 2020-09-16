// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import java.security.KeyStore;
import java.security.Security;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * The JUnit tests for the KeyVaultProvider class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultJcaProviderTest {

    /**
     * Test the constructor.
     */
    @Test
    public void testConstructor() {
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        assertNotNull(provider);
    }
    
    /**
     * Test getting a certificate using the Provider.
     * 
     * @throws Exception when an error occurs.
     */
    @Test
    public void testGetCertificate() throws Exception {
        Security.addProvider(new KeyVaultJcaProvider());
        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        keyStore.load(null, null);
        assertNull(keyStore.getCertificate("myalias"));
    }
}
