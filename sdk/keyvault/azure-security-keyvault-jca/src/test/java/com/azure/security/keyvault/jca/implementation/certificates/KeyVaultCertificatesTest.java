// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import com.azure.security.keyvault.jca.KeyVaultLoadStoreParameter;
import com.azure.security.keyvault.jca.implementation.CertificateVersion;
import com.azure.security.keyvault.jca.implementation.TestCertificateVersions;
import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import com.azure.security.keyvault.jca.implementation.mocking.MockCertificate;
import com.azure.security.keyvault.jca.implementation.mocking.MockKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KeyVaultCertificatesTest {

    private static final int CONCURRENT_READERS = 4;

    private static final long TIMEOUT_MILLIS = 10_000;

    private final FakeKeyVaultClient keyVaultClient = new FakeKeyVaultClient();

    private final CertificateVersion certificateVersion = TestCertificateVersions.create("myalias");

    private final Key key = new MockKey();

    private final Certificate certificate = new MockCertificate();

    private final Certificate[] certificateChain = new Certificate[] { certificate };

    private KeyVaultCertificates keyVaultCertificates;

    private KeyVaultCertificates createKeyVaultCertificates(KeyVaultClient client) {
        return createKeyVaultCertificates(client, Collections.emptySet());
    }

    private KeyVaultCertificates createKeyVaultCertificates(KeyVaultClient client, Set<String> filterPatterns) {
        KeyVaultLoadStoreParameter parameter
            = new KeyVaultLoadStoreParameter(null).setCertificatesRefreshIntervalInMs(60_000)
                .setCertificateAliasFilterPatterns(filterPatterns);
        return new KeyVaultCertificates(parameter, client);
    }

    /**
     * Reinjects a {@link KeyVaultClient} after {@link KeyVaultCertificates#updateKeyVaultClient} has nulled it out
     * (which happens whenever the supplied {@link KeyVaultLoadStoreParameter} has no URI). There is no public API
     * for this, so reflection is used purely to keep testing against the fake client afterward.
     */
    private void setKeyVaultClient(KeyVaultCertificates certificates, KeyVaultClient client) {
        try {
            Field keyVaultClientField = KeyVaultCertificates.class.getDeclaredField("keyVaultClient");
            keyVaultClientField.setAccessible(true);
            keyVaultClientField.set(certificates, client);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to inject the test KeyVaultClient.", exception);
        }
    }

    @BeforeEach
    public void beforeEach() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        keyVaultClient.stubAliases(aliases);
        keyVaultClient.stubResolveCertificateVersion("myalias", certificateVersion);
        keyVaultClient.stubKeyForVersion(certificateVersion, key);
        keyVaultClient.stubCertificateForVersion(certificateVersion, certificate);
        keyVaultClient.stubCertificateChainForVersion(certificateVersion, certificateChain);
        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient);
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
        keyVaultClient.stubAliases(null);
        Assertions.assertNotEquals(keyVaultCertificates.refreshAndGetAliasByCertificate(certificate), "myalias");
        Assertions.assertNull(keyVaultCertificates.getCertificates().get("myalias"));
    }

    @Test
    public void testRefreshAndGetAliasByCertificateReturnsMatchBeforeLaterRefreshInvalidatesIt() {
        String matchingAlias = "first-alias";
        String otherAlias = "second-alias";
        CertificateVersion matchingVersion = TestCertificateVersions.create(matchingAlias);
        CertificateVersion otherVersion = TestCertificateVersions.create(otherAlias);
        Certificate otherCertificate = new MockCertificate() {
            @Override
            public byte[] getEncoded() {
                return new byte[] { 1 };
            }
        };

        keyVaultClient.stubAliases(Arrays.asList(matchingAlias, otherAlias));
        keyVaultClient.stubResolveCertificateVersion(matchingAlias, matchingVersion);
        keyVaultClient.stubResolveCertificateVersion(otherAlias, otherVersion);
        keyVaultClient.stubCertificateForVersionAnswer(matchingVersion, () -> {
            sleepUnchecked(50);
            return certificate;
        });
        keyVaultClient.stubCertificateForVersion(otherVersion, otherCertificate);
        KeyVaultLoadStoreParameter parameter
            = new KeyVaultLoadStoreParameter(null).setCertificatesRefreshIntervalInMs(1);
        keyVaultCertificates = new KeyVaultCertificates(parameter, keyVaultClient);

        Assertions.assertEquals(matchingAlias, keyVaultCertificates.refreshAndGetAliasByCertificate(certificate));
        Assertions.assertEquals(0, keyVaultClient.resolveCertificateVersionCallCount(otherAlias));
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

        Assertions.assertEquals(0, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testLoadCertificateDetailsForRequestedAliasOnly() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        aliases.add("otheralias");

        keyVaultClient.stubAliases(aliases);

        keyVaultCertificates.getCertificate("myalias");

        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.resolveCertificateVersionCallCount("otheralias"));
    }

    @Test
    public void testGetKeyLoadsOnlyKeyForRequestedAlias() {
        keyVaultCertificates.getCertificateKey("myalias");

        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testGetCertificateChainLoadsOnlyChainForRequestedAlias() {
        keyVaultCertificates.getCertificateChain("myalias");

        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateForVersionCallCount(certificateVersion));
    }

    @Test
    public void testCertificateChainAndKeyUseSameResolvedVersionUntilRefresh() {
        CertificateVersion version1 = TestCertificateVersions.create("myalias");
        CertificateVersion version2 = TestCertificateVersions.create("myalias");
        Certificate version1Certificate = new MockCertificate();
        Certificate[] version1Chain = new Certificate[] { version1Certificate };
        Key version1Key = new MockKey();
        Key version2Key = new MockKey();

        // These stubs reproduce the pre-fix behavior when material was fetched independently by alias.
        keyVaultClient.stubLegacyCertificateChain("myalias", version1Chain);
        keyVaultClient.stubLegacyKey("myalias", version2Key);

        keyVaultClient.stubResolveCertificateVersion("myalias", version1, version2);
        keyVaultClient.stubCertificateForVersion(version1, version1Certificate);
        keyVaultClient.stubCertificateChainForVersion(version1, version1Chain);
        keyVaultClient.stubKeyForVersion(version1, version1Key);
        keyVaultClient.stubKeyForVersion(version2, version2Key);

        Assertions.assertArrayEquals(version1Chain, keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertSame(version1Certificate, keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertSame(version1Key, keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(version2));

        keyVaultCertificates.refreshCertificates();

        Assertions.assertSame(version2Key, keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(2, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.keyForVersionCallCount(version2));
    }

    @Test
    public void testConcurrentMaterialLoadsShareCertificateVersionResolution() throws Exception {
        BlockingAnswer<CertificateVersion> blockingAnswer = new BlockingAnswer<>(certificateVersion);
        keyVaultClient.stubResolveCertificateVersionAnswer("myalias", blockingAnswer);
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

        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
        Assertions.assertEquals(1, keyVaultClient.keyForVersionCallCount(certificateVersion));
    }

    @Test
    public void testConfiguredAliasesFilter() {
        List<String> aliases = new ArrayList<>();
        aliases.add("myalias");
        aliases.add("otheralias");
        keyVaultClient.stubAliases(aliases);

        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, Collections.singleton("myalias"));

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
        keyVaultClient.stubAliases(aliases);

        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, Collections.singleton("^prod-.*"));

        Assertions.assertEquals(Collections.singletonList("prod-cert"), keyVaultCertificates.getAliases());
        Assertions.assertEquals(1, keyVaultClient.aliasesCallCount());
    }

    @Test
    public void testFilterPatternsExcludeRegex() {
        List<String> aliases = new ArrayList<>();
        aliases.add("prod-active");
        aliases.add("prod-deprecated");
        keyVaultClient.stubAliases(aliases);

        Set<String> filterPatterns = new HashSet<>(Arrays.asList("^prod-.*", "!^prod-deprecated$"));
        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, filterPatterns);

        Assertions.assertEquals(Collections.singletonList("prod-active"), keyVaultCertificates.getAliases());
        Assertions.assertEquals(1, keyVaultClient.aliasesCallCount());
    }

    @Test
    public void testConfiguredAliasesFilterAfterRefresh() {
        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, Collections.singleton("myalias"));

        Assertions.assertEquals(Collections.singletonList("myalias"), keyVaultCertificates.getAliases());

        keyVaultCertificates.refreshCertificates();

        List<String> refreshedAliases = keyVaultCertificates.getAliases();
        Assertions.assertEquals(1, refreshedAliases.size());
        Assertions.assertTrue(refreshedAliases.contains("myalias"));
        Assertions.assertFalse(refreshedAliases.contains("otheralias"));
        Assertions.assertFalse(refreshedAliases.contains("new"));
        Assertions.assertEquals(2, keyVaultClient.aliasesCallCount());
    }

    @Test
    public void testConfiguredAliasesFilterUsesListApi() {
        keyVaultClient.stubAliases(Arrays.asList("configured-alias", "other-alias"));

        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, Collections.singleton("configured-alias"));

        Assertions.assertEquals(Collections.singletonList("configured-alias"), keyVaultCertificates.getAliases());
        Assertions.assertEquals(1, keyVaultClient.aliasesCallCount());
    }

    @Test
    public void testConfiguredAliasesIgnoreNullEntries() {
        Set<String> configuredAliases = new HashSet<>(Arrays.asList("myalias", null));
        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, configuredAliases);

        Assertions.assertEquals(Collections.singletonList("myalias"), keyVaultCertificates.getAliases());
        Assertions.assertEquals(1, keyVaultClient.aliasesCallCount());
    }

    @Test
    public void testInvalidFilterPatternThrows() {
        Set<String> filterPatterns = new HashSet<>(Collections.singletonList("[invalid"));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> createKeyVaultCertificates(keyVaultClient, filterPatterns));
    }

    @Test
    public void testFilterPatternWithBoundedQuantifier() {
        keyVaultClient.stubAliases(Arrays.asList("cert-42", "cert-1234567", "cert-abc"));

        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, Collections.singleton("^cert-\\d{1,5}$"));

        Assertions.assertEquals(Collections.singletonList("cert-42"), keyVaultCertificates.getAliases());
    }

    @Test
    public void testGetCertificateWithUnconfiguredAliasDoesNotFetchDetails() {
        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, Collections.singleton("myalias"));

        Assertions.assertNull(keyVaultCertificates.getCertificate("otheralias"));

        Assertions.assertEquals(0, keyVaultClient.resolveCertificateVersionCallCount("otheralias"));
    }

    @Test
    public void testAliasCertificateLoadFailureIsRetriedOnNextAccess() {
        RuntimeException loadFailure = new RuntimeException("transient certificate error");
        keyVaultClient.stubCertificateForVersionThrowThenReturn(certificateVersion, loadFailure, certificate);

        RuntimeException thrown
            = Assertions.assertThrows(RuntimeException.class, () -> keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertSame(loadFailure, thrown);
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(2, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testAliasCertificateNullLoadIsRetriedOnNextAccess() {
        keyVaultClient.stubCertificateForVersion(certificateVersion, null, certificate);

        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(2, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testAliasKeyLoadFailureIsRetriedOnNextAccess() {
        RuntimeException loadFailure = new RuntimeException("transient key error");
        keyVaultClient.stubKeyForVersionThrowThenReturn(certificateVersion, loadFailure, key);

        RuntimeException thrown
            = Assertions.assertThrows(RuntimeException.class, () -> keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertSame(loadFailure, thrown);
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(2, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testAliasKeyNullLoadIsRetriedOnNextAccess() {
        keyVaultClient.stubKeyForVersion(certificateVersion, null, key);

        Assertions.assertNull(keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(key, keyVaultCertificates.getCertificateKey("myalias"));
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(2, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testAliasChainLoadFailureIsRetriedOnNextAccess() {
        RuntimeException loadFailure = new RuntimeException("Key Vault returned HTTP 403");
        keyVaultClient.stubCertificateChainForVersionThrowThenReturn(certificateVersion, loadFailure, certificateChain);

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
            () -> keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertSame(loadFailure, thrown);
        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(2, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(certificateVersion));
    }

    @Test
    public void testAliasChainEmptyLoadIsCached() {
        keyVaultClient.stubCertificateChainForVersion(certificateVersion, new Certificate[0], certificateChain);

        Assertions.assertArrayEquals(new Certificate[0], keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertArrayEquals(new Certificate[0], keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(0, keyVaultClient.keyForVersionCallCount(certificateVersion));
    }

    @Test
    public void testCertificateVersionResolutionFailureIsRetriedOnNextAccess() {
        RuntimeException resolutionFailure = new RuntimeException("Key Vault returned HTTP 403");
        keyVaultClient.stubResolveCertificateVersionThrowThenReturn("myalias", resolutionFailure, certificateVersion);

        RuntimeException thrown
            = Assertions.assertThrows(RuntimeException.class, () -> keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertSame(resolutionFailure, thrown);
        Assertions.assertSame(certificate, keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(2, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(certificateVersion));
    }

    @Test
    public void testConcurrentChainLoadFailureIsSharedAndRetried() throws Exception {
        RuntimeException loadFailure = new RuntimeException("Key Vault returned HTTP 429");
        BlockingFailureAnswer<Certificate[]> blockingFailure = new BlockingFailureAnswer<>(loadFailure);
        keyVaultClient.stubCertificateChainForVersionAnswerThenReturn(certificateVersion, blockingFailure,
            certificateChain);

        assertConcurrentFailureIsShared(() -> keyVaultCertificates.getCertificateChain("myalias"), blockingFailure,
            loadFailure, "loadMaterialIfNeeded");
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));

        Assertions.assertArrayEquals(certificateChain, keyVaultCertificates.getCertificateChain("myalias"));
        Assertions.assertEquals(2, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testConcurrentCertificateVersionResolutionFailureIsSharedAndRetried() throws Exception {
        RuntimeException resolutionFailure = new RuntimeException("Key Vault returned HTTP 403");
        BlockingFailureAnswer<CertificateVersion> blockingFailure = new BlockingFailureAnswer<>(resolutionFailure);
        keyVaultClient.stubResolveCertificateVersionAnswerThenReturn("myalias", blockingFailure, certificateVersion);

        assertConcurrentFailureIsShared(() -> keyVaultCertificates.getCertificate("myalias"), blockingFailure,
            resolutionFailure, "resolveCertificateVersionIfNeeded");
        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));

        Assertions.assertSame(certificate, keyVaultCertificates.getCertificate("myalias"));
        Assertions.assertEquals(2, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(certificateVersion));
    }

    @Test
    public void testRefreshDiscardsInFlightFailureFromPreviousGeneration() throws Exception {
        RuntimeException staleFailure = new RuntimeException("stale generation failure");
        CertificateVersion freshVersion = TestCertificateVersions.create("myalias");
        Certificate[] freshChain = new Certificate[] { new MockCertificate() };
        BlockingFailureAnswer<Certificate[]> blockingFailure = new BlockingFailureAnswer<>(staleFailure);
        keyVaultClient.stubResolveCertificateVersion("myalias", certificateVersion, freshVersion);
        keyVaultClient.stubCertificateChainForVersionAnswer(certificateVersion, blockingFailure);
        keyVaultClient.stubCertificateChainForVersion(freshVersion, freshChain);
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
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(freshVersion));
    }

    @Test
    public void testUpdateKeyVaultClientClearsCachedState() {
        Assertions.assertTrue(keyVaultCertificates.getAliases().contains("myalias"));
        Assertions.assertEquals(certificate, keyVaultCertificates.getCertificate("myalias"));

        keyVaultCertificates.updateKeyVaultClient(new KeyVaultLoadStoreParameter(null));

        Assertions.assertTrue(keyVaultCertificates.getAliases().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificates().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificateChains().isEmpty());
        Assertions.assertTrue(keyVaultCertificates.getCertificateKeys().isEmpty());
        Assertions.assertNull(keyVaultCertificates.getCertificate("myalias"));
    }

    @Test
    public void testUpdateKeyVaultClientAppliesAliasFilterPatterns() {
        keyVaultClient.stubAliases(Arrays.asList("prod-cert", "dev-cert"));
        KeyVaultLoadStoreParameter parameter
            = new KeyVaultLoadStoreParameter(null).setCertificateAliasFilterPatterns(Collections.singleton("^prod-.*"));

        keyVaultCertificates.updateKeyVaultClient(parameter);
        setKeyVaultClient(keyVaultCertificates, keyVaultClient);

        Assertions.assertEquals(Collections.singletonList("prod-cert"), keyVaultCertificates.getAliases());
    }

    @Test
    public void testInvalidClientUpdatePreservesExistingAliasFilters() {
        keyVaultClient.stubAliases(Arrays.asList("myalias", "otheralias"));
        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient, Collections.singleton("myalias"));
        Assertions.assertEquals(Collections.singletonList("myalias"), keyVaultCertificates.getAliases());

        Set<String> invalidFilterPatterns = new HashSet<>(Arrays.asList("otheralias", "![invalid"));
        KeyVaultLoadStoreParameter parameter
            = new KeyVaultLoadStoreParameter(null).setCertificateAliasFilterPatterns(invalidFilterPatterns);

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> keyVaultCertificates.updateKeyVaultClient(parameter));
        keyVaultCertificates.refreshCertificates();

        Assertions.assertEquals(Collections.singletonList("myalias"), keyVaultCertificates.getAliases());
    }

    @Test
    public void testConcurrentCertificateLoadsShareSingleRequest() throws Exception {
        BlockingAnswer<Certificate> blockingAnswer = new BlockingAnswer<>(certificate);
        keyVaultClient.stubCertificateForVersionAnswer(certificateVersion, blockingAnswer);

        assertConcurrentLoadsShareSingleRequest(() -> keyVaultCertificates.getCertificate("myalias"), blockingAnswer,
            loadedCertificate -> Assertions.assertSame(certificate, loadedCertificate));

        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(certificateVersion));
    }

    @Test
    public void testConcurrentCertificateChainLoadsShareSingleRequest() throws Exception {
        BlockingAnswer<Certificate[]> blockingAnswer = new BlockingAnswer<>(certificateChain);
        keyVaultClient.stubCertificateChainForVersionAnswer(certificateVersion, blockingAnswer);

        assertConcurrentLoadsShareSingleRequest(() -> keyVaultCertificates.getCertificateChain("myalias"),
            blockingAnswer, loadedChain -> Assertions.assertArrayEquals(certificateChain, loadedChain));

        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
    }

    @Test
    public void testConcurrentKeyLoadsShareSingleRequest() throws Exception {
        BlockingAnswer<Key> blockingAnswer = new BlockingAnswer<>(key);
        keyVaultClient.stubKeyForVersionAnswer(certificateVersion, blockingAnswer);

        assertConcurrentLoadsShareSingleRequest(() -> keyVaultCertificates.getCertificateKey("myalias"), blockingAnswer,
            loadedKey -> Assertions.assertSame(key, loadedKey));

        Assertions.assertEquals(1, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.keyForVersionCallCount(certificateVersion));
    }

    @Test
    public void testRefreshDiscardsInFlightCertificateFromPreviousGeneration() throws Exception {
        Certificate freshCertificate = new MockCertificate();
        CertificateVersion freshVersion = TestCertificateVersions.create("myalias");
        BlockingAnswer<Certificate> staleAnswer = new BlockingAnswer<>(certificate);
        keyVaultClient.stubResolveCertificateVersion("myalias", certificateVersion, freshVersion);
        keyVaultClient.stubCertificateForVersionAnswer(certificateVersion, staleAnswer);
        keyVaultClient.stubCertificateForVersion(freshVersion, freshCertificate);

        assertRefreshDiscardsStaleLoad(() -> keyVaultCertificates.getCertificate("myalias"), staleAnswer,
            loadedCertificate -> Assertions.assertSame(freshCertificate, loadedCertificate));

        Assertions.assertEquals(2, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(certificateVersion));
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(freshVersion));
    }

    @Test
    public void testRefreshDiscardsInFlightCertificateChainFromPreviousGeneration() throws Exception {
        Certificate[] freshChain = new Certificate[] { new MockCertificate() };
        CertificateVersion freshVersion = TestCertificateVersions.create("myalias");
        BlockingAnswer<Certificate[]> staleAnswer = new BlockingAnswer<>(certificateChain);
        keyVaultClient.stubResolveCertificateVersion("myalias", certificateVersion, freshVersion);
        keyVaultClient.stubCertificateChainForVersionAnswer(certificateVersion, staleAnswer);
        keyVaultClient.stubCertificateChainForVersion(freshVersion, freshChain);

        assertRefreshDiscardsStaleLoad(() -> keyVaultCertificates.getCertificateChain("myalias"), staleAnswer,
            loadedChain -> Assertions.assertArrayEquals(freshChain, loadedChain));

        Assertions.assertEquals(2, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(certificateVersion));
        Assertions.assertEquals(1, keyVaultClient.certificateChainForVersionCallCount(freshVersion));
    }

    @Test
    public void testRefreshDiscardsInFlightKeyFromPreviousGeneration() throws Exception {
        Key freshKey = new MockKey();
        CertificateVersion freshVersion = TestCertificateVersions.create("myalias");
        BlockingAnswer<Key> staleAnswer = new BlockingAnswer<>(key);
        keyVaultClient.stubResolveCertificateVersion("myalias", certificateVersion, freshVersion);
        keyVaultClient.stubKeyForVersionAnswer(certificateVersion, staleAnswer);
        keyVaultClient.stubKeyForVersion(freshVersion, freshKey);

        assertRefreshDiscardsStaleLoad(() -> keyVaultCertificates.getCertificateKey("myalias"), staleAnswer,
            loadedKey -> Assertions.assertSame(freshKey, loadedKey));

        Assertions.assertEquals(2, keyVaultClient.resolveCertificateVersionCallCount("myalias"));
        Assertions.assertEquals(1, keyVaultClient.keyForVersionCallCount(certificateVersion));
        Assertions.assertEquals(1, keyVaultClient.keyForVersionCallCount(freshVersion));
    }

    @Test
    public void testClientReplacementDiscardsInFlightCertificate() throws Exception {
        BlockingAnswer<Certificate> staleAnswer = new BlockingAnswer<>(certificate);
        keyVaultClient.stubCertificateForVersionAnswer(certificateVersion, staleAnswer);
        List<Certificate> loadedValues = Collections.synchronizedList(new ArrayList<>());
        Thread reader = new Thread(() -> loadedValues.add(keyVaultCertificates.getCertificate("myalias")));
        reader.start();

        staleAnswer.awaitStarted();
        keyVaultCertificates.updateKeyVaultClient(new KeyVaultLoadStoreParameter(null));
        staleAnswer.release();
        joinThreads(Collections.singletonList(reader));

        Assertions.assertEquals(1, loadedValues.size());
        Assertions.assertNull(loadedValues.get(0));
        Assertions.assertTrue(keyVaultCertificates.getCertificates().isEmpty());
        Assertions.assertEquals(1, keyVaultClient.certificateForVersionCallCount(certificateVersion));
    }

    @Test
    public void testConcurrentForceRefreshAppliesLatestAliases() throws Exception {
        CountDownLatch firstListCallStarted = new CountDownLatch(1);
        CountDownLatch firstListCallMayFinish = new CountDownLatch(1);
        AtomicInteger listCallCount = new AtomicInteger();

        keyVaultClient.stubAliasesAnswer(() -> {
            if (listCallCount.getAndIncrement() == 0) {
                firstListCallStarted.countDown();
                awaitLatchUnchecked(firstListCallMayFinish);
                return Collections.singletonList("stale-alias");
            }
            return Collections.singletonList("fresh-alias");
        });

        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient);

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

        keyVaultClient.stubAliasesAnswer(() -> {
            listCallStarted.countDown();
            awaitLatchUnchecked(listCallMayFinish);
            return Collections.singletonList("myalias");
        });

        keyVaultCertificates = createKeyVaultCertificates(keyVaultClient);

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

        Assertions.assertEquals(1, keyVaultClient.aliasesCallCount());
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
     * Same as {@link #awaitLatch(CountDownLatch)}, but usable from {@link Supplier#get()} lambdas, which cannot
     * declare a checked {@link InterruptedException}.
     */
    private static void awaitLatchUnchecked(CountDownLatch latch) {
        try {
            awaitLatch(latch);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the test latch.", exception);
        }
    }

    private static void sleepUnchecked(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the refresh interval to expire.", exception);
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
            long ownerCount
                = readers.stream().filter(reader -> hasStackFrame(reader, BlockingFailureAnswer.class, "get")).count();
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

    /**
     * Scripted response that blocks until released, so tests can deterministically observe an in-flight Key Vault
     * call before letting it complete.
     */
    private static final class BlockingAnswer<T> implements Supplier<T> {

        private final CountDownLatch started = new CountDownLatch(1);

        private final CountDownLatch mayFinish = new CountDownLatch(1);

        private final T result;

        private BlockingAnswer(T result) {
            this.result = result;
        }

        @Override
        public T get() {
            started.countDown();
            awaitLatchUnchecked(mayFinish);
            return result;
        }

        private void awaitStarted() throws InterruptedException {
            awaitLatch(started);
        }

        private void release() {
            mayFinish.countDown();
        }
    }

    /**
     * Same as {@link BlockingAnswer}, but throws a scripted failure instead of returning a value once released.
     */
    private static final class BlockingFailureAnswer<T> implements Supplier<T> {

        private final CountDownLatch started = new CountDownLatch(1);

        private final CountDownLatch mayFail = new CountDownLatch(1);

        private final RuntimeException failure;

        private BlockingFailureAnswer(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public T get() {
            started.countDown();
            awaitLatchUnchecked(mayFail);
            throw failure;
        }

        private void awaitStarted() throws InterruptedException {
            awaitLatch(started);
        }

        private void release() {
            mayFail.countDown();
        }
    }

    /**
     * Handwritten {@link KeyVaultClient} test double. Every remote call is scripted through a
     * {@link ScriptedResponses} instance keyed by whatever uniquely identifies the call (nothing for
     * {@link #getAliases()}, the alias for {@link #resolveCertificateVersion(String)}, and the resolved
     * {@link CertificateVersion} identity for the per-version material getters), which mirrors how the production
     * {@code KeyVaultCertificates} class keys its own caches.
     */
    private static final class FakeKeyVaultClient extends KeyVaultClient {

        private final ScriptedResponses<Void, List<String>> aliasesScript = new ScriptedResponses<>();

        private final ScriptedResponses<String, CertificateVersion> resolveCertificateVersionScript
            = new ScriptedResponses<>();

        private final ScriptedResponses<CertificateVersion, Certificate> certificateForVersionScript
            = new ScriptedResponses<>();

        private final ScriptedResponses<CertificateVersion, Certificate[]> certificateChainForVersionScript
            = new ScriptedResponses<>();

        private final ScriptedResponses<CertificateVersion, Key> keyForVersionScript = new ScriptedResponses<>();

        private final Map<String, Certificate[]> legacyCertificateChains = new HashMap<>();

        private final Map<String, Key> legacyKeys = new HashMap<>();

        private FakeKeyVaultClient() {
            super("https://accountname.vault.azure.net", "tenant-id", "client-id", "client-secret");
        }

        private void stubAliases(List<String> aliases) {
            aliasesScript.returnValues(null, Collections.singletonList(aliases));
        }

        private void stubAliasesAnswer(Supplier<List<String>> supplier) {
            aliasesScript.answer(null, supplier);
        }

        private int aliasesCallCount() {
            return aliasesScript.callCount(null);
        }

        @Override
        public List<String> getAliases() {
            return aliasesScript.invoke(null);
        }

        private void stubResolveCertificateVersion(String alias, CertificateVersion... versions) {
            resolveCertificateVersionScript.returnValues(alias, Arrays.asList(versions));
        }

        private void stubResolveCertificateVersionThrowThenReturn(String alias, RuntimeException exception,
            CertificateVersion version) {
            resolveCertificateVersionScript.throwThenReturn(alias, exception, version);
        }

        private void stubResolveCertificateVersionAnswer(String alias, Supplier<CertificateVersion> supplier) {
            resolveCertificateVersionScript.answer(alias, supplier);
        }

        private void stubResolveCertificateVersionAnswerThenReturn(String alias, Supplier<CertificateVersion> supplier,
            CertificateVersion version) {
            resolveCertificateVersionScript.answerThenReturn(alias, supplier, version);
        }

        private int resolveCertificateVersionCallCount(String alias) {
            return resolveCertificateVersionScript.callCount(alias);
        }

        @Override
        public CertificateVersion resolveCertificateVersion(String alias) {
            return resolveCertificateVersionScript.invoke(alias);
        }

        private void stubCertificateForVersion(CertificateVersion version, Certificate... certificates) {
            certificateForVersionScript.returnValues(version, Arrays.asList(certificates));
        }

        private void stubCertificateForVersionThrowThenReturn(CertificateVersion version, RuntimeException exception,
            Certificate value) {
            certificateForVersionScript.throwThenReturn(version, exception, value);
        }

        private void stubCertificateForVersionAnswer(CertificateVersion version, Supplier<Certificate> supplier) {
            certificateForVersionScript.answer(version, supplier);
        }

        private int certificateForVersionCallCount(CertificateVersion version) {
            return certificateForVersionScript.callCount(version);
        }

        @Override
        public Certificate getCertificateForVersion(CertificateVersion version) {
            return certificateForVersionScript.invoke(version);
        }

        private void stubCertificateChainForVersion(CertificateVersion version, Certificate[]... chains) {
            certificateChainForVersionScript.returnValues(version, Arrays.asList(chains));
        }

        private void stubCertificateChainForVersionThrowThenReturn(CertificateVersion version,
            RuntimeException exception, Certificate[] value) {
            certificateChainForVersionScript.throwThenReturn(version, exception, value);
        }

        private void stubCertificateChainForVersionAnswer(CertificateVersion version,
            Supplier<Certificate[]> supplier) {
            certificateChainForVersionScript.answer(version, supplier);
        }

        private void stubCertificateChainForVersionAnswerThenReturn(CertificateVersion version,
            Supplier<Certificate[]> supplier, Certificate[] value) {
            certificateChainForVersionScript.answerThenReturn(version, supplier, value);
        }

        private int certificateChainForVersionCallCount(CertificateVersion version) {
            return certificateChainForVersionScript.callCount(version);
        }

        @Override
        public Certificate[] getCertificateChainForVersion(CertificateVersion version) {
            return certificateChainForVersionScript.invoke(version);
        }

        private void stubKeyForVersion(CertificateVersion version, Key... keys) {
            keyForVersionScript.returnValues(version, Arrays.asList(keys));
        }

        private void stubKeyForVersionThrowThenReturn(CertificateVersion version, RuntimeException exception,
            Key value) {
            keyForVersionScript.throwThenReturn(version, exception, value);
        }

        private void stubKeyForVersionAnswer(CertificateVersion version, Supplier<Key> supplier) {
            keyForVersionScript.answer(version, supplier);
        }

        private int keyForVersionCallCount(CertificateVersion version) {
            return keyForVersionScript.callCount(version);
        }

        @Override
        public Key getKeyForVersion(CertificateVersion version, char[] password) {
            return keyForVersionScript.invoke(version);
        }

        // Legacy, alias-keyed accessors. Only used to reproduce pre-refactor stubs in one test; never verified.
        private void stubLegacyCertificateChain(String alias, Certificate[] chain) {
            legacyCertificateChains.put(alias, chain);
        }

        private void stubLegacyKey(String alias, Key key) {
            legacyKeys.put(alias, key);
        }

        @Override
        public Certificate[] getCertificateChain(String alias) {
            return legacyCertificateChains.get(alias);
        }

        @Override
        public Key getKey(String alias, char[] password) {
            return legacyKeys.get(alias);
        }
    }

    /**
     * Scripted return values, failures, responses, and call counts keyed by whatever value identifies a given
     * response (for example a {@code null} constant, an alias, or a {@link CertificateVersion}).
     * <p>
     * Each key is scripted with a queue of {@link Supplier}s. Calling {@link #invoke(Object)} consumes queued
     * suppliers one at a time until only one remains, at which point that last supplier is replayed for every
     * subsequent call.
     *
     * @param <K> The type of the key used to identify a stub (for example a {@link String} alias).
     * @param <V> The type of value returned by a stub.
     */
    private static final class ScriptedResponses<K, V> {

        private final Map<K, Deque<Supplier<V>>> queuedSuppliers = new HashMap<>();

        private final Map<K, AtomicInteger> callCounts = new HashMap<>();

        private synchronized void sequence(K key, List<Supplier<V>> suppliers) {
            queuedSuppliers.put(key, new ArrayDeque<>(suppliers));
            callCounts.put(key, new AtomicInteger());
        }

        private void returnValues(K key, List<V> values) {
            List<Supplier<V>> suppliers = new ArrayList<>();

            for (V value : values) {
                suppliers.add(() -> value);
            }

            sequence(key, suppliers);
        }

        private void throwThenReturn(K key, RuntimeException exception, V value) {
            List<Supplier<V>> suppliers = new ArrayList<>();

            suppliers.add(() -> {
                throw exception;
            });
            suppliers.add(() -> value);
            sequence(key, suppliers);
        }

        private void answer(K key, Supplier<V> supplier) {
            sequence(key, Collections.singletonList(supplier));
        }

        private void answerThenReturn(K key, Supplier<V> supplier, V value) {
            List<Supplier<V>> suppliers = new ArrayList<>();

            suppliers.add(supplier);
            suppliers.add(() -> value);
            sequence(key, suppliers);
        }

        private int callCount(K key) {
            AtomicInteger counter;

            synchronized (this) {
                counter = callCounts.get(key);
            }

            return counter == null ? 0 : counter.get();
        }

        private V invoke(K key) {
            Supplier<V> supplier;

            synchronized (this) {
                AtomicInteger counter = callCounts.computeIfAbsent(key, unused -> new AtomicInteger());

                counter.incrementAndGet();

                Deque<Supplier<V>> suppliers = queuedSuppliers.get(key);

                if (suppliers == null || suppliers.isEmpty()) {
                    return null;
                }

                supplier = suppliers.size() > 1 ? suppliers.poll() : suppliers.peek();
            }

            // The actual supplier invocation happens outside the synchronized block so blocking suppliers used by
            // concurrency tests don't serialize unrelated calls against this same script.
            return supplier.get();
        }
    }
}
