// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyVaultCertificatesTest {

    private final Key key = new Key() {
        @Override
        public String getAlgorithm() {
            return "";
        }

        @Override
        public String getFormat() {
            return "";
        }

        @Override
        public byte[] getEncoded() {
            return new byte[0];
        }
    };

    private final Certificate certificate = new MockCertificate(null);

    private KeyVaultCertificates keyVaultCertificates;

    private List<String> aliases = new ArrayList<>(Collections.singletonList("myalias"));

    @BeforeEach
    public void beforeEach() {
        KeyVaultClient keyVaultClient = new KeyVaultClient("https://fake.vault.azure.net/", null, null, null) {
            @Override
            public List<String> getAliases() {
                return aliases;
            }

            @Override
            public Key getKey(String alias, char[] password) {
                return key;
            }

            @Override
            public Certificate getCertificate(String alias) {
                return certificate;
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
        Assertions.assertEquals(keyVaultCertificates.refreshAndGetAliasByCertificate(certificate), "myalias");
        Assertions.assertEquals(keyVaultCertificates.getCertificates().get("myalias"), certificate);
        aliases = null;
        Assertions.assertNotEquals(keyVaultCertificates.refreshAndGetAliasByCertificate(certificate), "myalias");
        Assertions.assertNull(keyVaultCertificates.getCertificates().get("myalias"));
    }

    @Test
    public void testDeleteAlias() {
        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
        keyVaultCertificates.deleteEntry("myalias");
        Assertions.assertFalse(keyVaultCertificates.getAliases().contains("myalias"));
    }

}
