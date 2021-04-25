// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KeyVaultClientTest {
    private KeyVaultClient keyVaultClient;
    private String certificateName;

    @BeforeEach
    public void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            System.getenv("AZURE_KEYVAULT_ENDPOINT"),
            System.getenv("azure.keyvault.aad-authentication-url"),
            System.getenv("SPRING_TENANT_ID"),
            System.getenv("SPRING_CLIENT_ID"),
            System.getenv("SPRING_CLIENT_SECRET"));
        certificateName = System.getenv("AZURE_CERTIFICATE_NAME");
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
        assertNull(keyVaultClient.getKey(certificateName, null));
    }
}
