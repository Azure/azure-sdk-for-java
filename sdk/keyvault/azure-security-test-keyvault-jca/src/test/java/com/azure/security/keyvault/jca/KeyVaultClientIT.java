// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@Disabled
public class KeyVaultClientIT {
    private KeyVaultClient keyVaultClient;

    @BeforeEach
    public void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.aad-authentication-url"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
    }

    @Test
    public void testGetAliases() {
        assertNotNull(keyVaultClient.getAliases());
    }

    @Test
    public void testGetCertificate() {
        assertNotNull(keyVaultClient.getCertificate("myalias"));
    }

    @Test
    public void testGetKey() {
        assertNull(keyVaultClient.getKey("myalias", null));
    }
}
