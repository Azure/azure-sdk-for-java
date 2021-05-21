// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.security.KeyStore;
import java.security.Security;

import static com.azure.security.keyvault.jca.PropertyConvertorUtils.SYSTEM_PROPERTIES;
import static com.azure.security.keyvault.jca.PropertyConvertorUtils.getKeyVaultKeyStore;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultKeyManagerTest {

    private static KeyVaultKeyManager manager;
    private static String certificateName;

    @BeforeAll
    public static void setEnvironmentProperty() throws Exception {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(SYSTEM_PROPERTIES);
        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        KeyStore keyStore = getKeyVaultKeyStore();
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
