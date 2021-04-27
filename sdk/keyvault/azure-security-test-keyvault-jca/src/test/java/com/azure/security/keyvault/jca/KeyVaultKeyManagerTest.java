// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "azure.keyvault.certificate-name", matches = ".*")
public class KeyVaultKeyManagerTest {

    private KeyVaultKeyManager manager;
    private String certificateName;

    public static void putEnvironmentPropertyToSystemProperty(String key) {
        Optional.of(key)
                .map(System::getenv)
                .filter(StringUtils::hasText)
                .ifPresent(value -> System.getProperties().put(key, value));
    }

    @BeforeEach
    public void setEnvironmentProperty() throws KeyStoreException, NoSuchAlgorithmException, IOException,
        CertificateException {
        putEnvironmentPropertyToSystemProperty("azure.keyvault.uri");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.aad-authentication-url");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.tenant-id");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.client-id");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.client-secret");
        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("azure.keyvault.uri"),
            System.getenv("azure.keyvault.tenant-id"),
            System.getenv("azure.keyvault.client-id"),
            System.getenv("azure.keyvault.client-secret"));
        keyStore.load(parameter);
        manager = new KeyVaultKeyManager(keyStore, null);
        certificateName = System.getenv("azure.keyvault.certificate-name");
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
