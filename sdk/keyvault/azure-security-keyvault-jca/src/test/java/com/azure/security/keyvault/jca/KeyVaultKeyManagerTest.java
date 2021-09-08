// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
public class KeyVaultKeyManagerTest {

    private KeyVaultKeyManager manager;

    @BeforeEach
    public void setEnvironmentProperty() throws KeyStoreException, NoSuchAlgorithmException, IOException,
        CertificateException {
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            System.getProperty("azure.keyvault.tenant-id"),
            System.getProperty("azure.keyvault.client-id"),
            System.getProperty("azure.keyvault.client-secret"));
        keyStore.load(parameter);
        manager = new KeyVaultKeyManager(keyStore, null);
    }

    @Test
    public void testPrivateKey() {
        assertNotNull(manager.getPrivateKey("myalias"));
    }


    @Test
    public void testGetCertificateChain() {
        assertNotNull(manager.getCertificateChain("myalias"));
    }
}
