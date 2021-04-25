// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeyVaultKeyManagerTest {

    private KeyVaultKeyManager manager;
    private String certificateName;

    @BeforeEach
    public void setEnvironmentProperty() throws KeyStoreException, NoSuchAlgorithmException, IOException,
        CertificateException {
        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_ENDPOINT"),
            System.getenv("SPRING_TENANT_ID"),
            System.getenv("SPRING_CLIENT_ID"),
            System.getenv("SPRING_CLIENT_SECRET"));
        keyStore.load(parameter);
        manager = new KeyVaultKeyManager(keyStore, null);
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testPrivateKey() {
        assertNotNull(manager.getPrivateKey(certificateName));
    }


    @Test
    public void testGetCertificateChain() {
        assertNotNull(manager.getCertificateChain(certificateName));
    }
}
