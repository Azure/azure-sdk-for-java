// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.cert.Certificate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Disabled
public class KeyVaultClientTest {

    @Test
    public void testGetAliases() {
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        KeyVaultClient keyVaultClient = new KeyVaultClient(
            keyVaultUri, System.getProperty("azure.keyvault.aad-authentication-url"),
            tenantId,
            clientId,
            clientSecret);
        List<String> result = keyVaultClient.getAliases();
        assertNotNull(result);
    }

    @Test
    public void testGetCertificate() {
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        KeyVaultClient keyVaultClient = new KeyVaultClient(
            keyVaultUri, System.getProperty("azure.keyvault.aad-authentication-url"),
            tenantId,
            clientId,
            clientSecret);
        Certificate certificate = keyVaultClient.getCertificate("myalias");
        assertNotNull(certificate);
    }

    @Test
    public void testGetKey() {
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        KeyVaultClient keyVaultClient = new KeyVaultClient(
            keyVaultUri, System.getProperty("azure.keyvault.aad-authentication-url"), tenantId, clientId, clientSecret);
        assertNull(keyVaultClient.getKey("myalias", null));
    }
}
