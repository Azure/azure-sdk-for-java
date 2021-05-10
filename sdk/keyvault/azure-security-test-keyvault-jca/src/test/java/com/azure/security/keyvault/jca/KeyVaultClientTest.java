// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultClientTest {
    private KeyVaultClient keyVaultClient;
    private String certificateName;

    @BeforeEach
    public void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testGetAliases() {
        assertEquals(keyVaultClient.getAliases().get(0),"myalias");
    }

    @Test
    public void testGetCertificate() {
        assertEquals(keyVaultClient.getCertificate(certificateName).toString(),"myalias");
    }

    @Test
    public void testGetKey() {
        assertNotNull(keyVaultClient.getKey(certificateName, null));
    }
}
