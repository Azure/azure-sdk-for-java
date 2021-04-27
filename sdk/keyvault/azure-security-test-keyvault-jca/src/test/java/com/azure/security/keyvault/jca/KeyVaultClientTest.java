// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "azure.keyvault.certificate-name", matches = ".*")
public class KeyVaultClientTest {
    private KeyVaultClient keyVaultClient;
    private String certificateName;

    @BeforeEach
    public void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            System.getenv("azure.keyvault.uri"),
            System.getenv("azure.keyvault.tenant-id"),
            System.getenv("azure.keyvault.client-id"),
            System.getenv("azure.keyvault.client-secret"));
        certificateName = System.getenv("azure.keyvault.certificate-name");
    }

    @Test
    public void testGetAliases() {
        assertNotNull(keyVaultClient.getAliases());
    }

    @Test
    public void testGetCertificate() {
        assertNotNull(keyVaultClient.getCertificate(certificateName));
    }

    @Test
    public void testGetKey() {
        assertNotNull(keyVaultClient.getKey(certificateName, null));
    }
}
