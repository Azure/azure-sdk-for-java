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

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = ".*")
public class KeyVaultKeyManagerTest {

    private KeyVaultKeyManager manager;
    private String certificateName;

    public static void putEnvironmentPropertyToSystemProperty(String key) {
        Optional.of(key)
                .map(System::getenv)
                .filter(StringUtils::hasText)
                .ifPresent(value -> System.getProperties().put(
                    key.toLowerCase().replaceAll("_", "."), value));
    }

    @BeforeEach
    public void setEnvironmentProperty() throws KeyStoreException, NoSuchAlgorithmException, IOException,
        CertificateException {
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_URI");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.aad-authentication-url");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_TENANT-ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT-ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT-SECRET");
        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-SECRET"));
        keyStore.load(parameter);
        manager = new KeyVaultKeyManager(keyStore, null);
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testPrivateKey() {
        assertNotNull(manager.getPrivateKey("myalias"));
    }


    @Test
    public void testGetCertificateChain() {
        assertNotNull(manager.getCertificateChain(certificateName));
    }
}
