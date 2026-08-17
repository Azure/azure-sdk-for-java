// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.azure.security.keyvault.jca.implementation.CertificateVersion;
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
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class KeyVaultCertificatesTest {

    private static final int CONCURRENT_READERS = 4;

    private static final long TIMEOUT_MILLIS = 10_000;

    private final KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);

    private final CertificateVersion certificateVersion = mock(CertificateVersion.class);

    private final Key key = mock(Key.class);

    private final Certificate certificate = mock(Certificate.class);

    private final Certificate[] certificateChain = new Certificate[] { certificate };

    private KeyVaultCertificates keyVaultCertificates;

    @BeforeEach
    public void beforeEach() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        when(keyVaultClient.getAliases()).thenReturn(aliases);
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenReturn(certificateVersion);
        when(keyVaultClient.getKeyForVersion(certificateVersion, null)).thenReturn(key);
        when(keyVaultClient.getCertificateForVersion(certificateVersion)).thenReturn(certificate);
        when(keyVaultClient.getCertificateChainForVersion(certificateVersion)).thenReturn(certificateChain);
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

        verify(keyVaultClient, never()).resolveCertificateVersion("myalias");
        verify(keyVaultClient, never()).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testLoadCertificateDetailsForRequestedAliasOnly() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        aliases.add("otheralias");

        when(keyVaultClient.getAliases()).thenReturn(aliases);

        keyVaultCertificates.getCertificate("myalias");

        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateChainForVersion(certificateVersion);
        verify(keyVaultClient, never()).resolveCertificateVersion("otheralias");
    }

    @Test
    public void testGetKeyLoadsOnlyKeyForRequestedAlias() {
        keyVaultCertificates.getCertificateKey("myalias");

        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testGetCertificateChainLoadsOnlyChainForRequestedAlias() {
        keyVaultCertificates.getCertificateChain("myalias");

        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(certificateVersion);
        verify(keyVaultClient, never()).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateForVersion(certificateVersion);
    }

    @Test
    public void testCertificateChainAndKeyUseSameResolvedVersionUntilRefresh() {
        CertificateVersion version1 = mock(CertificateVersion.class);
        CertificateVersion version2 = mock(CertificateVersion.class);
        Certificate version1Certificate = mock(Certificate.class);
        Certificate[] version1Chain = new Certificate[] { version1Certificate };
        Key version1Key = mock(Key.class);
        Key version2Key = mock(Key.class);

        // These stubs reproduce the pre-fix behavior when material was fetched independently by alias.
        when(keyVaultClient.getCertificateChain("myalias")).thenReturn(version1Chain);
        when(keyVaultClient.getKey("myalias", null)).thenReturn(version2Key);

        when(keyVaultClient.resolveCertificateVersion("myalias")).thenReturn(version1, version2);
        when(keyVaultClient.getCertificateForVersion(version1)).thenReturn(version1Certificate);
        when(keyVaultClient.getCertificateChainForVersion(version1)).thenReturn(version1Chain);
        when(keyVaultClient.getKeyForVersion(version1, null)).thenReturn(version1Key);
        when(keyVaultClient.getKeyForVersion(version2, null)).thenReturn(version2Key);

        Assertions.assertArrayEquals(version1Chain, keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertSame(version1Certificate, keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertSame(version1Key, keyVaultCertificates.getCertificateKey("myalias"));
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, never()).getKeyForVersion(version2, null);

        keyVaultCertificates.refreshCertificates();

        Assertions.assertSame(version2Key, keyVaultCertificates.getCertificateKey("myalias"));
        verify(keyVaultClient, times(2)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getKeyForVersion(version2, null);
    }

    @Test
    public void testConcurrentMaterialLoadsShareCertificateVersionResolution() throws Exception {
        BlockingAnswer<CertificateVersion> blockingAnswer = new BlockingAnswer<>(certificateVersion);
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenAnswer(blockingAnswer);
        CountDownLatch readersReady = new CountDownLatch(3);
        CountDownLatch readersMayStart = new CountDownLatch(1);
        List<Thread> readers = Arrays.asList(
            newMaterialReader(readersReady, readersMayStart, () -> keyVaultCertificates.getCertificate("myalias")),
            newMaterialReader(readersReady, readersMayStart, () -> keyVaultCertificates.getCertificateChain("myalias")),
            newMaterialReader(readersReady, readersMayStart, () -> keyVaultCertificates.getCertificateKey("myalias")));

        readers.forEach(Thread::start);
        awaitLatch(readersReady);
        readersMayStart.countDown();
        blockingAnswer.awaitStarted();
        awaitThreadsWaiting(readers);
        blockingAnswer.release();
        joinThreads(readers);

        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(certificateVersion);
        verify(keyVaultClient, times(1)).getKeyForVersion(certificateVersion, null);
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
    public void testFilterPatternWithBoundedQuantifier() {
        when(keyVaultClient.getAliases()).thenReturn(Arrays.asList("cert-42", "cert-1234567", "cert-abc"));

        keyVaultCertificates
            = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("^cert-\\d{1,5}$"));

        Assertions.assertEquals(Collections.singletonList("cert-42"), keyVaultCertificates.getAliases());
    }

    @Test
    public void testGetCertificateWithUnconfiguredAliasDoesNotFetchDetails() {
        keyVaultCertificates = new KeyVaultCertificates(60_000, keyVaultClient, Collections.singleton("myalias"));

        Assertions.assertNull(keyVaultCertificates.getCertificate("otheralias"));

        verify(keyVaultClient, never()).resolveCertificateVersion("otheralias");
    }

    @Test
    public void testAliasCertificateLoadFailureIsRetriedOnNextAccess() {
        RuntimeException loadFailure = new RuntimeException("transient certificate error");
        when(keyVaultClient.getCertificateForVersion(certificateVersion)).thenThrow(loadFailure)
            .thenReturn(certificate);

        RuntimeException thrown
            = Assertions.assertThrows(RuntimeException.class, () -> keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertSame(loadFailure, thrown);
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(2)).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testAliasCertificateNullLoadIsRetriedOnNextAccess() {
        when(keyVaultClient.getCertificateForVersion(certificateVersion)).thenReturn(null).thenReturn(certificate);

        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(2)).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testAliasKeyLoadFailureIsRetriedOnNextAccess() {
        RuntimeException loadFailure = new RuntimeException("transient key error");
        when(keyVaultClient.getKeyForVersion(certificateVersion, null)).thenThrow(loadFailure).thenReturn(key);

        RuntimeException thrown
            = Assertions.assertThrows(RuntimeException.class, () -> keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertSame(loadFailure, thrown);
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(2)).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testAliasKeyNullLoadIsRetriedOnNextAccess() {
        when(keyVaultClient.getKeyForVersion(certificateVersion, null)).thenReturn(null).thenReturn(key);

        Assertions.assertNull(keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(2)).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, never()).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testAliasChainLoadFailureIsRetriedOnNextAccess() {
        RuntimeException loadFailure = new RuntimeException("Key Vault returned HTTP 403");
        when(keyVaultClient.getCertificateChainForVersion(certificateVersion)).thenThrow(loadFailure)
            .thenReturn(certificateChain);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
            () -> keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertSame(loadFailure, thrown);
        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(2)).getCertificateChainForVersion(certificateVersion);
        verify(keyVaultClient, never()).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getKeyForVersion(certificateVersion, null);
    }

    @Test
    public void testAliasChainEmptyLoadIsCached() {
        when(keyVaultClient.getCertificateChainForVersion(certificateVersion)).thenReturn(new Certificate[0])
            .thenReturn(certificateChain);

        Assertions.assertArrayEquals(new Certificate[0], keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertArrayEquals(new Certificate[0], keyVaultCertificates.getCertificateChain("myalias"));
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(certificateVersion);
        verify(keyVaultClient, never()).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, never()).getKeyForVersion(certificateVersion, null);
    }

    @Test
    public void testCertificateVersionResolutionFailureIsRetriedOnNextAccess() {
        RuntimeException resolutionFailure = new RuntimeException("Key Vault returned HTTP 403");
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenThrow(resolutionFailure)
            .thenReturn(certificateVersion);

        RuntimeException thrown
            = Assertions.assertThrows(RuntimeException.class, () -> keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertSame(resolutionFailure, thrown);
        Assertions.assertSame(certificate, keyVaultCertificates.getCertificate("myalias"));
        verify(keyVaultClient, times(2)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateForVersion(certificateVersion);
    }

    @Test
    public void testConcurrentChainLoadFailureIsSharedAndRetried() throws Exception {
        RuntimeException loadFailure = new RuntimeException("Key Vault returned HTTP 429");
        BlockingFailureAnswer<Certificate[]> blockingFailure = new BlockingFailureAnswer<>(loadFailure);
        when(keyVaultClient.getCertificateChainForVersion(certificateVersion)).thenAnswer(blockingFailure)
            .thenReturn(certificateChain);

        assertConcurrentFailureIsShared(() -> keyVaultCertificates.getCertificateChain("myalias"), blockingFailure,
            loadFailure, "loadMaterialIfNeeded");
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(certificateVersion);

        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
        verify(keyVaultClient, times(2)).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testConcurrentCertificateVersionResolutionFailureIsSharedAndRetried() throws Exception {
        RuntimeException resolutionFailure = new RuntimeException("Key Vault returned HTTP 403");
        BlockingFailureAnswer<CertificateVersion> blockingFailure = new BlockingFailureAnswer<>(resolutionFailure);
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenAnswer(blockingFailure)
            .thenReturn(certificateVersion);

        assertConcurrentFailureIsShared(() -> keyVaultCertificates.getCertificate("myalias"), blockingFailure,
            resolutionFailure, "resolveCertificateVersionIfNeeded");
        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");

        Assertions.assertSame(certificate, keyVaultCertificates.getCertificate("myalias"));
        verify(keyVaultClient, times(2)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateForVersion(certificateVersion);
    }

    @Test
    public void testRefreshDiscardsInFlightFailureFromPreviousGeneration() throws Exception {
        RuntimeException staleFailure = new RuntimeException("stale generation failure");
        CertificateVersion freshVersion = mock(CertificateVersion.class);
        Certificate[] freshChain = new Certificate[] { mock(Certificate.class) };
        BlockingFailureAnswer<Certificate[]> blockingFailure = new BlockingFailureAnswer<>(staleFailure);
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenReturn(certificateVersion, freshVersion);
        when(keyVaultClient.getCertificateChainForVersion(certificateVersion)).thenAnswer(blockingFailure);
        when(keyVaultClient.getCertificateChainForVersion(freshVersion)).thenReturn(freshChain);
        List<Certificate[]> loadedChains = Collections.synchronizedList(new ArrayList<>());
        List<RuntimeException> failures = Collections.synchronizedList(new ArrayList<>());
        Thread reader = new Thread(() -> {
            try {
                loadedChains.add(keyVaultCertificates.getCertificateChain("myalias"));
            } catch (RuntimeException exception) {
                failures.add(exception);
            }
        });
        reader.start();

        blockingFailure.awaitStarted();
        keyVaultCertificates.refreshCertificates();
        blockingFailure.release();
        joinThreads(Collections.singletonList(reader));

        Assertions.assertTrue(failures.isEmpty());
        Assertions.assertEquals(1, loadedChains.size());
        Assertions.assertArrayEquals(freshChain, loadedChains.get(0));
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(certificateVersion);
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(freshVersion);
    }

    @Test
    public void testUpdateKeyVaultClientClearsCachedState() {
        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));

        keyVaultCertificates.updateKeyVaultClient(null, null, null, null, null, null, false, false);

        Assertions.assertTrue(keyVaultCertificates.getAliases().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificates().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificateChains().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificateKeys().isEmpty());
        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
    }

    @Test
    public void testConcurrentCertificateLoadsShareSingleRequest() throws Exception {
        BlockingAnswer<Certificate> blockingAnswer = new BlockingAnswer<>(certificate);
        when(keyVaultClient.getCertificateForVersion(certificateVersion)).thenAnswer(blockingAnswer);

        assertConcurrentLoadsShareSingleRequest(() -> keyVaultCertificates.getCertificate("myalias"), blockingAnswer,
            loadedCertificate -> Assertions.assertSame(certificate, loadedCertificate));

        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateForVersion(certificateVersion);
    }

    @Test
    public void testConcurrentCertificateChainLoadsShareSingleRequest() throws Exception {
        BlockingAnswer<Certificate[]> blockingAnswer = new BlockingAnswer<>(certificateChain);
        when(keyVaultClient.getCertificateChainForVersion(certificateVersion)).thenAnswer(blockingAnswer);

        assertConcurrentLoadsShareSingleRequest(() -> keyVaultCertificates.getCertificateChain("myalias"),
            blockingAnswer, loadedChain -> Assertions.assertArrayEquals(certificateChain, loadedChain));

        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(certificateVersion);
    }

    @Test
    public void testConcurrentKeyLoadsShareSingleRequest() throws Exception {
        BlockingAnswer<Key> blockingAnswer = new BlockingAnswer<>(key);
        when(keyVaultClient.getKeyForVersion(certificateVersion, null)).thenAnswer(blockingAnswer);

        assertConcurrentLoadsShareSingleRequest(() -> keyVaultCertificates.getCertificateKey("myalias"), blockingAnswer,
            loadedKey -> Assertions.assertSame(key, loadedKey));

        verify(keyVaultClient, times(1)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getKeyForVersion(certificateVersion, null);
    }

    @Test
    public void testRefreshDiscardsInFlightCertificateFromPreviousGeneration() throws Exception {
        Certificate freshCertificate = mock(Certificate.class);
        CertificateVersion freshVersion = mock(CertificateVersion.class);
        BlockingAnswer<Certificate> staleAnswer = new BlockingAnswer<>(certificate);
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenReturn(certificateVersion, freshVersion);
        when(keyVaultClient.getCertificateForVersion(certificateVersion)).thenAnswer(staleAnswer);
        when(keyVaultClient.getCertificateForVersion(freshVersion)).thenReturn(freshCertificate);

        assertRefreshDiscardsStaleLoad(() -> keyVaultCertificates.getCertificate("myalias"), staleAnswer,
            loadedCertificate -> Assertions.assertSame(freshCertificate, loadedCertificate));

        verify(keyVaultClient, times(2)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateForVersion(certificateVersion);
        verify(keyVaultClient, times(1)).getCertificateForVersion(freshVersion);
    }

    @Test
    public void testRefreshDiscardsInFlightCertificateChainFromPreviousGeneration() throws Exception {
        Certificate[] freshChain = new Certificate[] { mock(Certificate.class) };
        CertificateVersion freshVersion = mock(CertificateVersion.class);
        BlockingAnswer<Certificate[]> staleAnswer = new BlockingAnswer<>(certificateChain);
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenReturn(certificateVersion, freshVersion);
        when(keyVaultClient.getCertificateChainForVersion(certificateVersion)).thenAnswer(staleAnswer);
        when(keyVaultClient.getCertificateChainForVersion(freshVersion)).thenReturn(freshChain);

        assertRefreshDiscardsStaleLoad(() -> keyVaultCertificates.getCertificateChain("myalias"), staleAnswer,
            loadedChain -> Assertions.assertArrayEquals(freshChain, loadedChain));

        verify(keyVaultClient, times(2)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(certificateVersion);
        verify(keyVaultClient, times(1)).getCertificateChainForVersion(freshVersion);
    }

    @Test
    public void testRefreshDiscardsInFlightKeyFromPreviousGeneration() throws Exception {
        Key freshKey = mock(Key.class);
        CertificateVersion freshVersion = mock(CertificateVersion.class);
        BlockingAnswer<Key> staleAnswer = new BlockingAnswer<>(key);
        when(keyVaultClient.resolveCertificateVersion("myalias")).thenReturn(certificateVersion, freshVersion);
        when(keyVaultClient.getKeyForVersion(certificateVersion, null)).thenAnswer(staleAnswer);
        when(keyVaultClient.getKeyForVersion(freshVersion, null)).thenReturn(freshKey);

        assertRefreshDiscardsStaleLoad(() -> keyVaultCertificates.getCertificateKey("myalias"), staleAnswer,
            loadedKey -> Assertions.assertSame(freshKey, loadedKey));

        verify(keyVaultClient, times(2)).resolveCertificateVersion("myalias");
        verify(keyVaultClient, times(1)).getKeyForVersion(certificateVersion, null);
        verify(keyVaultClient, times(1)).getKeyForVersion(freshVersion, null);
    }

    @Test
    public void testClientReplacementDiscardsInFlightCertificate() throws Exception {
        BlockingAnswer<Certificate> staleAnswer = new BlockingAnswer<>(certificate);
        when(keyVaultClient.getCertificateForVersion(certificateVersion)).thenAnswer(staleAnswer);
        List<Certificate> loadedValues = Collections.synchronizedList(new ArrayList<>());
        Thread reader = new Thread(() -> loadedValues.add(keyVaultCertificates.getCertificate("myalias")));
        reader.start();

        staleAnswer.awaitStarted();
        keyVaultCertificates.updateKeyVaultClient(null, null, null, null, null, null, false, false);
        staleAnswer.release();
        joinThreads(Collections.singletonList(reader));

        Assertions.assertEquals(1, loadedValues.size());
        Assertions.assertNull(loadedValues.get(0));
        Assertions.assertTrue(keyVaultCertificates.getCertificates().isEmpty());
        verify(keyVaultClient, times(1)).getCertificateForVersion(certificateVersion);
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

    private <T> void assertConcurrentLoadsShareSingleRequest(Supplier<T> load, BlockingAnswer<T> blockingAnswer,
        Consumer<T> assertLoaded) throws Exception {

        CountDownLatch readersReady = new CountDownLatch(CONCURRENT_READERS);
        CountDownLatch readersMayStart = new CountDownLatch(1);
        List<T> loadedValues = Collections.synchronizedList(new ArrayList<>());
        List<Thread> readers = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_READERS; i++) {
            Thread reader = new Thread(() -> {
                readersReady.countDown();
                try {
                    awaitLatch(readersMayStart);
                    loadedValues.add(load.get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
            readers.add(reader);
            reader.start();
        }

        awaitLatch(readersReady);
        readersMayStart.countDown();
        blockingAnswer.awaitStarted();
        awaitThreadsWaiting(readers);
        blockingAnswer.release();
        joinThreads(readers);

        Assertions.assertEquals(CONCURRENT_READERS, loadedValues.size());
        loadedValues.forEach(assertLoaded);
    }

    private <T> void assertRefreshDiscardsStaleLoad(Supplier<T> load, BlockingAnswer<T> staleAnswer,
        Consumer<T> assertFresh) throws Exception {

        List<T> loadedValues = Collections.synchronizedList(new ArrayList<>());
        Thread reader = new Thread(() -> loadedValues.add(load.get()));
        reader.start();

        staleAnswer.awaitStarted();
        keyVaultCertificates.refreshCertificates();
        staleAnswer.release();
        joinThreads(Collections.singletonList(reader));

        Assertions.assertEquals(1, loadedValues.size());
        assertFresh.accept(loadedValues.get(0));
        assertFresh.accept(load.get());
    }

    private void assertConcurrentFailureIsShared(Runnable load, BlockingFailureAnswer<?> blockingFailure,
        RuntimeException expectedFailure, String waiterCallerMethod) throws Exception {
        List<RuntimeException> failures = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch readersReady = new CountDownLatch(CONCURRENT_READERS);
        CountDownLatch readersMayStart = new CountDownLatch(1);
        List<Thread> readers = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_READERS; i++) {
            Thread reader = new Thread(() -> {
                readersReady.countDown();
                try {
                    awaitLatch(readersMayStart);
                    load.run();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } catch (RuntimeException exception) {
                    failures.add(exception);
                }
            });
            readers.add(reader);
            reader.start();
        }

        awaitLatch(readersReady);
        readersMayStart.countDown();
        blockingFailure.awaitStarted();
        awaitSingleFlightWaiters(readers, waiterCallerMethod);
        blockingFailure.release();
        joinThreads(readers);

        Assertions.assertEquals(CONCURRENT_READERS, failures.size());
        failures.forEach(failure -> Assertions.assertSame(expectedFailure, failure));
    }

    private static void joinThreads(List<Thread> threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.join(TIMEOUT_MILLIS);
            Assertions.assertFalse(thread.isAlive(), "Timed out waiting for test thread to finish.");
        }
    }

    private static Thread newMaterialReader(CountDownLatch readersReady, CountDownLatch readersMayStart,
        Runnable loadMaterial) {
        return new Thread(() -> {
            readersReady.countDown();
            try {
                awaitLatch(readersMayStart);
                loadMaterial.run();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });
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

    private static void awaitThreadsWaiting(List<Thread> threads) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TIMEOUT_MILLIS);

        while (System.nanoTime() < deadline) {
            boolean waiting = threads.stream()
                .map(Thread::getState)
                .allMatch(state -> state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING);

            if (waiting) {
                return;
            }

            Thread.sleep(10);
        }

        throw new IllegalStateException("Timed out waiting for the threads to wait.");
    }

    private static void awaitSingleFlightWaiters(List<Thread> readers, String waiterCallerMethod)
        throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TIMEOUT_MILLIS);

        while (System.nanoTime() < deadline) {
            long ownerCount = readers.stream()
                .filter(reader -> hasStackFrame(reader, BlockingFailureAnswer.class, "answer"))
                .count();
            long waiterCount = readers.stream()
                .filter(reader -> hasStackFrameCalledBy(reader, KeyVaultCertificates.class, "awaitInFlightOperation",
                    waiterCallerMethod))
                .count();

            if (ownerCount == 1 && waiterCount == readers.size() - 1) {
                return;
            }

            Thread.sleep(10);
        }

        throw new IllegalStateException("Timed out waiting for all readers to join the single-flight operation.");
    }

    private static boolean hasStackFrameCalledBy(Thread thread, Class<?> declaringClass, String methodName,
        String callerMethodName) {
        StackTraceElement[] stackTrace = thread.getStackTrace();
        for (int index = 0; index < stackTrace.length - 1; index++) {
            StackTraceElement frame = stackTrace[index];
            StackTraceElement caller = stackTrace[index + 1];
            if (frame.getClassName().equals(declaringClass.getName())
                && frame.getMethodName().equals(methodName)
                && caller.getClassName().equals(declaringClass.getName())
                && caller.getMethodName().equals(callerMethodName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasStackFrame(Thread thread, Class<?> declaringClass, String methodName) {
        return Arrays.stream(thread.getStackTrace())
            .anyMatch(frame -> frame.getClassName().equals(declaringClass.getName())
                && frame.getMethodName().equals(methodName));
    }

    private static final class BlockingAnswer<T> implements Answer<T> {

        private final CountDownLatch started = new CountDownLatch(1);

        private final CountDownLatch mayFinish = new CountDownLatch(1);

        private final T result;

        private BlockingAnswer(T result) {
            this.result = result;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws InterruptedException {
            started.countDown();
            awaitLatch(mayFinish);
            return result;
        }

        private void awaitStarted() throws InterruptedException {
            awaitLatch(started);
        }

        private void release() {
            mayFinish.countDown();
        }
    }

    private static final class BlockingFailureAnswer<T> implements Answer<T> {

        private final CountDownLatch started = new CountDownLatch(1);

        private final CountDownLatch mayFail = new CountDownLatch(1);

        private final RuntimeException failure;

        private BlockingFailureAnswer(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws InterruptedException {
            started.countDown();
            awaitLatch(mayFail);
            throw failure;
        }

        private void awaitStarted() throws InterruptedException {
            awaitLatch(started);
        }

        private void release() {
            mayFail.countDown();
        }
    }

}
