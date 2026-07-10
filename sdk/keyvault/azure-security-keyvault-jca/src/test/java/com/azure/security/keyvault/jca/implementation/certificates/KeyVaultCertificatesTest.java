// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KeyVaultCertificatesTest {

    private final KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);

    private final Key key = mock(Key.class);

    private final Certificate certificate = mock(Certificate.class);

    private final Certificate[] certificateChain = new Certificate[] { certificate };

    private KeyVaultCertificates keyVaultCertificates;

    @BeforeEach
    public void beforeEach() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        when(keyVaultClient.getAliases()).thenReturn(aliases);
        when(keyVaultClient.getKey("myalias", null)).thenReturn(key);
        when(keyVaultClient.getCertificate("myalias")).thenReturn(certificate);
        when(keyVaultClient.getCertificateChain("myalias")).thenReturn(certificateChain);
        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient);
    }

    @Test
    public void testGetAliases() {
        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
    }

    @Test
    public void testGetKey() {
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
    }

    @Test
    public void testGetCertificate() {
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
    }

    @Test
    public void testGetCertificateChain() {
        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
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

    @Test
    public void testGetAliasesDoesNotLoadCertificateDetailsEagerly() {
        keyVaultCertificates.getAliases();

        verify(keyVaultClient, never()).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificate("myalias");
        verify(keyVaultClient, never()).getCertificateChain("myalias");
    }

    @Test
    public void testLoadCertificateDetailsForRequestedAliasOnly() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        aliases.add("otheralias");

        Key otherKey = mock(Key.class);
        Certificate otherCertificate = mock(Certificate.class);

        when(keyVaultClient.getAliases()).thenReturn(aliases);
        when(keyVaultClient.getKey("otheralias", null)).thenReturn(otherKey);
        when(keyVaultClient.getCertificate("otheralias")).thenReturn(otherCertificate);

        keyVaultCertificates.getCertificate("myalias");

        verify(keyVaultClient, times(1)).getKey("myalias", null);
        verify(keyVaultClient, times(1)).getCertificate("myalias");
        verify(keyVaultClient, times(1)).getCertificateChain("myalias");
        verify(keyVaultClient, never()).getKey("otheralias", null);
        verify(keyVaultClient, never()).getCertificate("otheralias");
        verify(keyVaultClient, never()).getCertificateChain("otheralias");
    }

    @Test
    public void testConfiguredAliasesFilter() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        aliases.add("otheralias");
        when(keyVaultClient.getAliases()).thenReturn(aliases);

        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("myalias"));

        List<String> result = keyVaultCertificates.getAliases();
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains("myalias"));
        Assertions.assertFalse(result.contains("otheralias"));
    }

    @Test
    public void testConfiguredAliasesFilterAfterRefresh() {
        AtomicInteger invocationCount = new AtomicInteger(0);
        when(keyVaultClient.getAliases()).thenAnswer(invocation -> {
            if (invocationCount.getAndIncrement() == 0) {
                return Arrays.asList("myalias", "otheralias");
            }
            return Arrays.asList("otheralias", "myalias", "new");
        });

        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("myalias"));

        Assertions.assertEquals(Collections.singletonList("myalias"), keyVaultCertificates.getAliases());

        keyVaultCertificates.refreshCertificates();

        List<String> refreshedAliases = keyVaultCertificates.getAliases();
        Assertions.assertEquals(1, refreshedAliases.size());
        Assertions.assertTrue(refreshedAliases.contains("myalias"));
        Assertions.assertFalse(refreshedAliases.contains("otheralias"));
        Assertions.assertFalse(refreshedAliases.contains("new"));
    }

    @Test
    public void testGetCertificateWithUnconfiguredAliasDoesNotFetchDetails() {
        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("myalias"));

        Assertions.assertNull(keyVaultCertificates.getCertificate("otheralias"));

        verify(keyVaultClient, never()).getKey("otheralias", null);
        verify(keyVaultClient, never()).getCertificate("otheralias");
        verify(keyVaultClient, never()).getCertificateChain("otheralias");
    }

    @Test
    public void testAliasLoadFailureIsRetriedOnNextAccess() {
        when(keyVaultClient.getKey("myalias", null)).thenThrow(new RuntimeException("transient error")).thenReturn(key);

        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
        verify(keyVaultClient, times(2)).getKey("myalias", null);
    }

}
