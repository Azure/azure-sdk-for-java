// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyVaultCertificateInfoTest {

    private final KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);

    private Key key = mock(Key.class);

    private Certificate certificate = mock(Certificate.class);

    @BeforeEach
    public void setEnv() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        when(keyVaultClient.getAliases()).thenReturn(aliases);
        when(keyVaultClient.getKey("myalias", null)).thenReturn(key);
        when(keyVaultClient.getCertificate("myalias")).thenReturn(certificate);
    }

    @Test
    public void testGetAliases() {
        KeyVaultCertificatesInfo keyVaultCertificatesInfo = new KeyVaultCertificatesInfo();
        Assertions.assertTrue(keyVaultCertificatesInfo.getAliases(keyVaultClient).contains("myalias"));
    }

    @Test
    public void testGetKey() {
        KeyVaultCertificatesInfo keyVaultCertificatesInfo = new KeyVaultCertificatesInfo();
        Assertions.assertTrue(keyVaultCertificatesInfo.getCertificateKeys(keyVaultClient).containsValue(key));
    }

    @Test
    public void testGetCertificate() {
        KeyVaultCertificatesInfo keyVaultCertificatesInfo = new KeyVaultCertificatesInfo();
        Assertions.assertTrue(keyVaultCertificatesInfo.getCertificates(keyVaultClient).containsValue(certificate));
    }

    @Test
    public void testGetAliasByCertInTime() {
        KeyVaultCertificatesInfo keyVaultCertificatesInfo = new KeyVaultCertificatesInfo();
        Assertions.assertEquals(keyVaultCertificatesInfo.getAliasByCertInTime(certificate, keyVaultClient), "myalias");
        when(keyVaultClient.getAliases()).thenReturn(null);
        Assertions.assertNotEquals(keyVaultCertificatesInfo.getAliasByCertInTime(certificate, keyVaultClient), "myalias");
    }

    @Test
    public void testDeleteAlias() {
        KeyVaultCertificatesInfo keyVaultCertificatesInfo = new KeyVaultCertificatesInfo();
        Assertions.assertTrue(keyVaultCertificatesInfo.getAliases(keyVaultClient).contains("myalias"));
        keyVaultCertificatesInfo.deleteEntry("myalias");
        Assertions.assertFalse(keyVaultCertificatesInfo.getAliases(keyVaultClient).contains("myalias"));
    }

}
