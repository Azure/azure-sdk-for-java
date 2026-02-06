// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import com.azure.security.keyvault.jca.implementation.mocking.MockCertificate;
import com.azure.security.keyvault.jca.implementation.mocking.MockKey;
import com.azure.security.keyvault.jca.implementation.mocking.MockKeyVaultClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyVaultCertificatesTest {
    private final Key key = new MockKey();
    private final Certificate certificate = new MockCertificate();

    // This needs to be mutable.
    private List<String> aliases = new ArrayList<>(Collections.singletonList("myalias"));
    private KeyVaultCertificates keyVaultCertificates;

    @BeforeEach
    public void beforeEach() {
        KeyVaultClient keyVaultClient = new MockKeyVaultClient() {
            @Override
            public List<String> getAliases() {
                return aliases;
            }

            @Override
            public Key getKey(String alias, char[] password) {
                return "myalias".equals(alias) && password == null ? key : null;
            }

            @Override
            public Certificate getCertificate(String alias) {
                return "myalias".equals(alias) ? certificate : null;
            }

            @Override
            public Certificate[] getCertificateChain(String alias) {
                return null;
            }

            @Override
            public byte[] getSignedWithPrivateKey(String digestName, String digestValue, String keyId) {
                return null;
            }
        };
        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient);
    }

    @Test
    public void testGetAliases() {
        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
    }

    @Test
    public void testGetKey() {
        Assertions.assertTrue(keyVaultCertificates.getCertificateKeys().containsValue(key));
    }

    @Test
    public void testGetCertificate() {
        Assertions.assertTrue(keyVaultCertificates.getCertificates().containsValue(certificate));
    }

    @Test
    public void testRefreshAndGetAliasByCertificate() {
        Assertions.assertEquals("myalias", keyVaultCertificates.refreshAndGetAliasByCertificate(certificate));
        Assertions.assertEquals(keyVaultCertificates.getCertificates().get("myalias"), certificate);

        this.aliases = null;
        Assertions.assertNotEquals("myalias", keyVaultCertificates.refreshAndGetAliasByCertificate(certificate));
        Assertions.assertNull(keyVaultCertificates.getCertificates().get("myalias"));
    }

    @Test
    public void testDeleteAlias() {
        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
        keyVaultCertificates.deleteEntry("myalias");
        Assertions.assertFalse(keyVaultCertificates.getAliases().contains("myalias"));
    }

}
