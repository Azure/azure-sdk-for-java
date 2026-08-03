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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KeyVaultCertificatesTest {

    private static final long TIMEOUT_MILLIS = 10_000;

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
    public void testGetAliasesReturnsSnapshot() {
        List<String> aliasSnapshot = keyVaultCertificates.getAliases();
        Assertions.assertTrue(aliasSnapshot.contains("myalias"));

        aliasSnapshot.clear();

        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
    }

    @Test
    public void testGetKey() {
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
    }

    @Test
    public void testGetCertificateKeysReturnsSnapshot() {
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));

        Map<String, Key> keySnapshot = keyVaultCertificates.getCertificateKeys();
        Assertions.assertEquals(key, keySnapshot.get("myalias"));

        keySnapshot.clear();

        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKeys().get("myalias"));
    }

    @Test
    public void testGetCertificate() {
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
    }

    @Test
    public void testGetCertificatesReturnsSnapshot() {
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));

        Map<String, Certificate> certificateSnapshot = keyVaultCertificates.getCertificates();
        Assertions.assertEquals(certificate, certificateSnapshot.get("myalias"));

        certificateSnapshot.clear();

        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificates().get("myalias"));
    }

    @Test
    public void testGetCertificateChain() {
        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
    }

    @Test
    public void testGetCertificateChainReturnsClone() {
        Certificate[] firstRead = keyVaultCertificates.getCertificateChain("myalias");
        Assertions.assertNotNull(firstRead);

        firstRead[0] = null;

        Certificate[] secondRead = keyVaultCertificates.getCertificateChain("myalias");
        Assertions.assertNotNull(secondRead);
        Assertions.assertEquals(certificate, secondRead[0]);
    }

    @Test
    public void testGetCertificateChainsReturnsSnapshot() {
        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));

        Map<String, Certificate[]> chainSnapshot = keyVaultCertificates.getCertificateChains();
        Assertions.assertArrayEquals(certificateChain, chainSnapshot.get("myalias"));

        chainSnapshot.clear();

        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChains().get("myalias"));
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
    public void testRefreshAndGetAliasByCertificateWithNullCertificate() {
        Assertions.assertNull(keyVaultCertificates.refreshAndGetAliasByCertificate(null));
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

        verify(keyVaultClient, times(1)).getCertificate("myalias");
        verify(keyVaultClient, never()).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificateChain("myalias");
        verify(keyVaultClient, never()).getKey("otheralias", null);
        verify(keyVaultClient, never()).getCertificate("otheralias");
        verify(keyVaultClient, never()).getCertificateChain("otheralias");
    }

    @Test
    public void testGetKeyLoadsOnlyKeyForRequestedAlias() {
        keyVaultCertificates.getCertificateKey("myalias");

        verify(keyVaultClient, times(1)).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificate("myalias");
        verify(keyVaultClient, never()).getCertificateChain("myalias");
    }

    @Test
    public void testGetCertificateChainLoadsOnlyChainForRequestedAlias() {
        keyVaultCertificates.getCertificateChain("myalias");

        verify(keyVaultClient, times(1)).getCertificateChain("myalias");
        verify(keyVaultClient, never()).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificate("myalias");
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
    public void testFilterPatternsIncludeRegex() {
        List<String> aliases = new ArrayList<>();
        aliases.add("prod-cert");
        aliases.add("dev-cert");
        when(keyVaultClient.getAliases()).thenReturn(aliases);

        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("^prod-.*"));

        Assertions.assertEquals(Collections.singletonList("prod-cert"), keyVaultCertificates.getAliases());
        verify(keyVaultClient, times(1)).getAliases();
    }

    @Test
    public void testFilterPatternsExcludeRegex() {
        List<String> aliases = new ArrayList<>();
        aliases.add("prod-active");
        aliases.add("prod-deprecated");
        when(keyVaultClient.getAliases()).thenReturn(aliases);

        Set<String> filterPatterns = new HashSet<>(Arrays.asList("^prod-.*", "!^prod-deprecated$"));
        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, filterPatterns);

        Assertions.assertEquals(Collections.singletonList("prod-active"), keyVaultCertificates.getAliases());
        verify(keyVaultClient, times(1)).getAliases();
    }

    @Test
    public void testConfiguredAliasesFilterAfterRefresh() {
        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("myalias"));

        Assertions.assertEquals(Collections.singletonList("myalias"), keyVaultCertificates.getAliases());

        keyVaultCertificates.refreshCertificates();

        List<String> refreshedAliases = keyVaultCertificates.getAliases();
        Assertions.assertEquals(1, refreshedAliases.size());
        Assertions.assertTrue(refreshedAliases.contains("myalias"));
        Assertions.assertFalse(refreshedAliases.contains("otheralias"));
        Assertions.assertFalse(refreshedAliases.contains("new"));
        verify(keyVaultClient, times(2)).getAliases();
    }

    @Test
    public void testConfiguredAliasesFilterUsesListApi() {
        when(keyVaultClient.getAliases()).thenReturn(Arrays.asList("configured-alias", "other-alias"));

        keyVaultCertificates
            = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("configured-alias"));

        Assertions.assertEquals(Collections.singletonList("configured-alias"), keyVaultCertificates.getAliases());
        verify(keyVaultClient, times(1)).getAliases();
    }

    @Test
    public void testConfiguredAliasesIgnoreNullEntries() {
        Set<String> configuredAliases = new HashSet<>(Arrays.asList("myalias", null));
        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, configuredAliases);

        Assertions.assertEquals(Collections.singletonList("myalias"), keyVaultCertificates.getAliases());
        verify(keyVaultClient, times(1)).getAliases();
    }

    @Test
    public void testInvalidFilterPatternThrows() {
        Set<String> filterPatterns = new HashSet<>(Collections.singletonList("[invalid"));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new KeyVaultCertificates(60_000, keyVaultClient, filterPatterns));
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
    public void testAliasCertificateLoadFailureIsRetriedOnNextAccess() {
        when(keyVaultClient.getCertificate("myalias")).thenThrow(new RuntimeException("transient error"))
            .thenReturn(certificate);

        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
        verify(keyVaultClient, times(2)).getCertificate("myalias");
        verify(keyVaultClient, never()).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificateChain("myalias");
    }

    @Test
    public void testAliasCertificateNullLoadIsRetriedOnNextAccess() {
        when(keyVaultClient.getCertificate("myalias")).thenReturn(null).thenReturn(certificate);

        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
        verify(keyVaultClient, times(2)).getCertificate("myalias");
        verify(keyVaultClient, never()).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificateChain("myalias");
    }

    @Test
    public void testAliasKeyLoadFailureIsRetriedOnNextAccess() {
        when(keyVaultClient.getKey("myalias", null)).thenThrow(new RuntimeException("transient error")).thenReturn(key);

        Assertions.assertNull(keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
        verify(keyVaultClient, times(2)).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificate("myalias");
        verify(keyVaultClient, never()).getCertificateChain("myalias");
    }

    @Test
    public void testAliasKeyNullLoadIsRetriedOnNextAccess() {
        when(keyVaultClient.getKey("myalias", null)).thenReturn(null).thenReturn(key);

        Assertions.assertNull(keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
        verify(keyVaultClient, times(2)).getKey("myalias", null);
        verify(keyVaultClient, never()).getCertificate("myalias");
        verify(keyVaultClient, never()).getCertificateChain("myalias");
    }

    @Test
    public void testAliasChainLoadFailureIsRetriedOnNextAccess() {
        when(keyVaultClient.getCertificateChain("myalias")).thenThrow(new RuntimeException("transient error"))
            .thenReturn(certificateChain);

        Assertions.assertNull(keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
        verify(keyVaultClient, times(2)).getCertificateChain("myalias");
        verify(keyVaultClient, never()).getCertificate("myalias");
        verify(keyVaultClient, never()).getKey("myalias", null);
    }

    @Test
    public void testAliasChainEmptyLoadIsRetriedOnNextAccess() {
        when(keyVaultClient.getCertificateChain("myalias")).thenReturn(new Certificate[0]).thenReturn(certificateChain);

        Assertions.assertNull(keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
        verify(keyVaultClient, times(2)).getCertificateChain("myalias");
        verify(keyVaultClient, never()).getCertificate("myalias");
        verify(keyVaultClient, never()).getKey("myalias", null);
    }

    @Test
    public void testUpdateKeyVaultClientClearsCachedState() {
        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));

        keyVaultCertificates.updateKeyVaultClient(null, null, null, null, null, null, false);

        Assertions.assertTrue(keyVaultCertificates.getAliases().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificates().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificateChains().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificateKeys().isEmpty());
        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
    }

    @Test
    public void testConcurrentForceRefreshAppliesLatestAliases() throws Exception {
        CountDownLatch firstListCallStarted = new CountDownLatch(1);
        CountDownLatch firstListCallMayFinish = new CountDownLatch(1);
        AtomicInteger listCallCount = new AtomicInteger();

        when(keyVaultClient.getAliases()).thenAnswer(invocation -> {
            if (listCallCount.getAndIncrement() == 0) {
                firstListCallStarted.countDown();
                awaitLatch(firstListCallMayFinish);
                return Collections.singletonList("stale-alias");
            }
            return Collections.singletonList("fresh-alias");
        });

        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient);

        Thread slowRefresh = new Thread(keyVaultCertificates::refreshCertificates);
        slowRefresh.start();
        awaitLatch(firstListCallStarted);

        Thread fastRefresh = new Thread(keyVaultCertificates::refreshCertificates);
        fastRefresh.start();
        awaitThreadsParked(Collections.singletonList(fastRefresh));

        firstListCallMayFinish.countDown();
        slowRefresh.join(TIMEOUT_MILLIS);
        fastRefresh.join(TIMEOUT_MILLIS);

        Assertions.assertEquals(Collections.singletonList("fresh-alias"), keyVaultCertificates.getAliases());
    }

    @Test
    public void testConcurrentRefreshIssuesSingleAliasListCall() throws Exception {
        CountDownLatch listCallStarted = new CountDownLatch(1);
        CountDownLatch listCallMayFinish = new CountDownLatch(1);

        when(keyVaultClient.getAliases()).thenAnswer(invocation -> {
            listCallStarted.countDown();
            awaitLatch(listCallMayFinish);
            return Collections.singletonList("myalias");
        });

        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient);

        List<Thread> readers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Thread reader = new Thread(keyVaultCertificates::getAliases);
            readers.add(reader);
            reader.start();
        }

        awaitLatch(listCallStarted);
        awaitThreadsParked(readers);
        listCallMayFinish.countDown();

        for (Thread reader : readers) {
            reader.join(TIMEOUT_MILLIS);
        }

        verify(keyVaultClient, times(1)).getAliases();
    }

    private static void awaitLatch(CountDownLatch latch) throws InterruptedException {
        if (!latch.await(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            throw new IllegalStateException("Timed out waiting for the test latch.");
        }
    }

    /**
     * Waits until none of the threads can still reach Key Vault, so the pending call cannot be released too early.
     */
    private static void awaitThreadsParked(List<Thread> threads) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TIMEOUT_MILLIS);

        while (System.nanoTime() < deadline) {
            boolean parked = threads.stream()
                .map(Thread::getState)
                .noneMatch(state -> state == Thread.State.NEW || state == Thread.State.RUNNABLE);

            if (parked) {
                return;
            }

            Thread.sleep(10);
        }

        throw new IllegalStateException("Timed out waiting for the threads to park.");
    }

}
