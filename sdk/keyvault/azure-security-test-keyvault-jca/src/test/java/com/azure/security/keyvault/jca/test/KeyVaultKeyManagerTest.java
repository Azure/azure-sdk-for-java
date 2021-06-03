// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.test;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.security.keyvault.jca.KeyVaultKeyManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;

import static com.azure.security.keyvault.jca.test.PropertyConvertorUtils.SYSTEM_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultKeyManagerTest {

    private static KeyVaultKeyManager manager;
    private static String certificateName;

    @BeforeAll
    public static void setEnvironmentProperty() throws KeyStoreException, NoSuchAlgorithmException, IOException,
        CertificateException {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(SYSTEM_PROPERTIES);
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        KeyStore keyStore = PropertyConvertorUtils.getKeyVaultKeyStore();
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
