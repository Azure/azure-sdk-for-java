// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KeyVaultCertificatesTest {

    private final KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);

    private final Key key = mock(Key.class);

    private final Certificate certificate = mock(Certificate.class);

    private KeyVaultCertificates keyVaultCertificates;

    @BeforeEach
    public void beforeEach() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        when(keyVaultClient.getAliases()).thenReturn(aliases);
        when(keyVaultClient.getKey("myalias", null)).thenReturn(key);
        when(keyVaultClient.getCertificate("myalias")).thenReturn(certificate);
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
        when(keyVaultClient.getAliases()).thenReturn(null);
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
