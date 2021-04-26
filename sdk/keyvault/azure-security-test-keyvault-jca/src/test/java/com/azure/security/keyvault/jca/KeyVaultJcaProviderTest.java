// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.security.KeyStore;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The JUnit tests for the KeyVaultProvider class.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = ".*")
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
        KeyStore keystore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_ENDPOINT"),
            System.getenv("SPRING_TENANT_ID"),
            System.getenv("SPRING_CLIENT_ID"),
            System.getenv("SPRING_CLIENT_SECRET"));
        keystore.load(parameter);
        assertNull(keystore.getCertificate(System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME")));
    }
}
