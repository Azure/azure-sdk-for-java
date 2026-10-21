// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.certificates.AzureCertificates;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Isolated("Shares JVM properties with legacy tests that do not use resource locks")
@ResourceLock(Resources.SYSTEM_PROPERTIES)
public class KeyVaultKeyStoreReloadTest {
    private static final String ALIAS = "filesystem-reload";
    private static X509Certificate originalCertificate;
    private static X509Certificate replacementCertificate;

    @TempDir
    Path directory;

    private Path customPath;
    private Path wellKnownPath;
    private final Map<String, String> savedProperties = new HashMap<>();

    @BeforeAll
    static void createCertificates() throws Exception {
        originalCertificate = createCertificate(1);
        replacementCertificate = createCertificate(2);
    }

    private static X509Certificate createCertificate(int serial) throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        X500Name name = new X500Name("CN=filesystem-reload-" + serial);
        Instant now = Instant.now();
        return new JcaX509CertificateConverter()
            .getCertificate(new JcaX509v3CertificateBuilder(name, BigInteger.valueOf(serial),
                Date.from(now.minusSeconds(3600)), Date.from(now.plusSeconds(86400)), name, pair.getPublic())
                    .build(new JcaContentSignerBuilder("SHA256withRSA").build(pair.getPrivate())));
    }

    @BeforeEach
    void configurePaths() throws Exception {
        customPath = Files.createDirectory(directory.resolve("custom"));
        wellKnownPath = Files.createDirectory(directory.resolve("well-known"));
        for (String property : Arrays.asList("azure.keyvault.uri", "azure.cert-path.custom",
            "azure.cert-path.well-known", "azure.keyvault.jca.refresh-certificates-when-have-un-trust-certificate")) {
            savedProperties.put(property, System.getProperty(property));
            System.clearProperty(property);
        }
        System.setProperty("azure.cert-path.custom", customPath.toString());
        System.setProperty("azure.cert-path.well-known", wellKnownPath.toString());
    }

    @AfterEach
    void restoreProperties() {
        savedProperties.forEach((name, value) -> {
            if (value == null) {
                System.clearProperty(name);
            } else {
                System.setProperty(name, value);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("reloadScenarios")
    void reloadReplacesCertificate(boolean wellKnown, LoadMethod method) throws Exception {
        Path file = writeCertificate(wellKnown, originalCertificate);
        FileTime modified = Files.getLastModifiedTime(file);
        KeyStore store = newStore();
        X509TrustManager previousManager = trustManager(store);
        assertTrusted(previousManager, originalCertificate);

        Files.write(file, replacementCertificate.getEncoded());
        Files.setLastModifiedTime(file, modified);
        reload(store, method);

        assertEquals(replacementCertificate, store.getCertificate(ALIAS));
        assertNull(store.getCertificateAlias(originalCertificate));
        assertEquals(ALIAS, store.getCertificateAlias(replacementCertificate));
        assertEquals(1, Collections.frequency(Collections.list(store.aliases()), ALIAS));
        assertTrustUpdated(store);
        assertTrusted(previousManager, originalCertificate);
    }

    @ParameterizedTest
    @MethodSource("reloadScenarios")
    void reloadRemovesDeletedCertificateAndLoadsNewFile(boolean wellKnown, LoadMethod method) throws Exception {
        Path file = writeCertificate(wellKnown, originalCertificate);
        KeyStore store = newStore();
        Files.delete(file);
        Files.write(file.resolveSibling("replacement.cer"), replacementCertificate.getEncoded());

        reload(store, method);

        assertNull(store.getCertificate(ALIAS));
        assertNull(store.getCertificateChain(ALIAS));
        assertFalse(store.containsAlias(ALIAS));
        assertFalse(Collections.list(store.aliases()).contains(ALIAS));
        assertEquals(replacementCertificate, store.getCertificate("replacement"));
        assertTrustUpdated(store);
    }

    @ParameterizedTest
    @MethodSource("reloadScenarios")
    void invalidReplacementDoesNotRetainPreviousCertificate(boolean wellKnown, LoadMethod method) throws Exception {
        Path file = writeCertificate(wellKnown, originalCertificate);
        KeyStore store = newStore();
        Files.write(file, "not a certificate".getBytes(StandardCharsets.UTF_8));

        reload(store, method);

        assertNull(store.getCertificate(ALIAS));
        assertFalse(store.containsAlias(ALIAS));
        assertNotTrusted(trustManager(store), originalCertificate);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void newStoreReadsCurrentFilesWithoutChangingExistingStore(boolean wellKnown) throws Exception {
        Path file = writeCertificate(wellKnown, originalCertificate);
        KeyStore existing = newStore();
        Files.write(file, replacementCertificate.getEncoded());

        assertEquals(replacementCertificate, new KeyVaultKeyStore().engineGetCertificate(ALIAS));
        KeyStore current = newStore();

        assertEquals(originalCertificate, existing.getCertificate(ALIAS));
        assertEquals(replacementCertificate, current.getCertificate(ALIAS));
        assertTrustUpdated(current);
        current.deleteEntry(ALIAS);
        assertEquals(originalCertificate, existing.getCertificate(ALIAS));
        assertEquals(replacementCertificate, newStore().getCertificate(ALIAS));
    }

    @ParameterizedTest
    @MethodSource("reloadScenarios")
    void missingDirectoryCanBeCreatedAndRemoved(boolean wellKnown, LoadMethod method) throws Exception {
        Path path = wellKnown ? wellKnownPath : customPath;
        Files.delete(path);
        KeyStore store = newStore();
        assertFalse(store.containsAlias(ALIAS));
        Files.createDirectory(path);
        Path file = writeCertificate(wellKnown, originalCertificate);
        reload(store, method);
        assertEquals(originalCertificate, store.getCertificate(ALIAS));

        Files.delete(file);
        Files.delete(path);
        reload(store, method);
        assertFalse(store.containsAlias(ALIAS));
        assertNull(store.getCertificateChain(ALIAS));
        assertNotTrusted(trustManager(store), originalCertificate);
    }

    @Test
    void reloadPreservesSourcePriorityAndProgrammaticEntries() throws Exception {
        Path wellKnown = writeCertificate(true, originalCertificate);
        writeCertificate(false, replacementCertificate);
        KeyStore store = newStore();
        store.setCertificateEntry("programmatic", originalCertificate);
        assertEquals(originalCertificate, store.getCertificate(ALIAS));

        Files.delete(wellKnown);
        store.load(null, null);

        assertEquals(replacementCertificate, store.getCertificate(ALIAS));
        assertEquals(originalCertificate, store.getCertificate("programmatic"));
        assertEquals(1, Collections.frequency(Collections.list(store.aliases()), ALIAS));
    }

    @ParameterizedTest
    @MethodSource("reloadScenarios")
    void reloadRetainsPathsCapturedAtConstruction(boolean wellKnown, LoadMethod method) throws Exception {
        Path file = writeCertificate(wellKnown, originalCertificate);
        KeyStore store = newStore();
        Files.write(file, replacementCertificate.getEncoded());
        Path redirectedPath = Files.createDirectory(directory.resolve("redirected"));
        Files.write(redirectedPath.resolve(ALIAS + ".cer"), originalCertificate.getEncoded());
        System.setProperty(wellKnown ? "azure.cert-path.well-known" : "azure.cert-path.custom",
            redirectedPath.toString());

        reload(store, method);

        assertEquals(replacementCertificate, store.getCertificate(ALIAS));
        assertTrustUpdated(store);
    }

    @Test
    void concurrentReloadDoesNotFailCertificateAliasLookup() throws Exception {
        Path file = writeCertificate(false, originalCertificate);
        KeyVaultKeyStore store = new KeyVaultKeyStore();
        Field sourcesField = KeyVaultKeyStore.class.getDeclaredField("allCertificates");
        sourcesField.setAccessible(true);
        List<?> sources = (List<?>) sourcesField.get(store);
        CountDownLatch aliasesBeingEnumerated = new CountDownLatch(1);
        CountDownLatch reloaded = new CountDownLatch(1);
        sourcesField.set(store, new AbstractList<AzureCertificates>() {
            @Override
            public AzureCertificates get(int index) {
                if (index == sources.size() - 1) {
                    // Finish enumerating the old snapshot only after the reload removes its certificate.
                    aliasesBeingEnumerated.countDown();
                    try {
                        assertTrue(reloaded.await(10, TimeUnit.SECONDS), "Reload did not complete");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError("Alias lookup interrupted", e);
                    }
                }
                return (AzureCertificates) sources.get(index);
            }

            @Override
            public int size() {
                return sources.size();
            }
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> alias = executor.submit(() -> store.engineGetCertificateAlias(originalCertificate));
            assertTrue(aliasesBeingEnumerated.await(10, TimeUnit.SECONDS), "Alias enumeration did not start");
            Files.delete(file);
            store.engineLoad(null, null);
            reloaded.countDown();

            assertNull(alias.get(10, TimeUnit.SECONDS));
            assertNull(store.engineGetCertificateAlias(originalCertificate));
        } finally {
            reloaded.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Alias lookup did not stop");
        }
    }

    private Path writeCertificate(boolean wellKnown, X509Certificate certificate) throws Exception {
        return Files.write((wellKnown ? wellKnownPath : customPath).resolve(ALIAS + ".cer"), certificate.getEncoded());
    }

    private static KeyStore newStore() throws Exception {
        KeyStore store = KeyStore.getInstance(KeyVaultKeyStore.KEY_STORE_TYPE, new KeyVaultJcaProvider());
        store.load(null, null);
        return store;
    }

    private void reload(KeyStore store, LoadMethod method) throws Exception {
        switch (method) {
            case STREAM:
                store.load(null, null);
                break;

            case NULL_PARAMETER:
                store.load((KeyStore.LoadStoreParameter) null);
                break;

            case OTHER_PARAMETER:
                store.load(() -> null);
                break;

            case EXPLICIT_PARAMETER:
                store.load(new KeyVaultLoadStoreParameter(null));
                break;

            default:
                throw new IllegalArgumentException("Unknown load method: " + method);
        }
    }

    private static X509TrustManager trustManager(KeyStore store) throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance("PKIX");
        factory.init(store);
        return (X509TrustManager) factory.getTrustManagers()[0];
    }

    private static void assertTrustUpdated(KeyStore store) throws Exception {
        for (X509TrustManager manager : Arrays.asList(trustManager(store), new KeyVaultTrustManager(store))) {
            assertTrusted(manager, replacementCertificate);
            assertNotTrusted(manager, originalCertificate);
        }
    }

    private static void assertTrusted(X509TrustManager manager, X509Certificate certificate) throws Exception {
        manager.checkClientTrusted(new X509Certificate[] { certificate }, "RSA");
        manager.checkServerTrusted(new X509Certificate[] { certificate }, "RSA");
    }

    private static void assertNotTrusted(X509TrustManager manager, X509Certificate certificate) {
        assertThrows(CertificateException.class,
            () -> manager.checkClientTrusted(new X509Certificate[] { certificate }, "RSA"));
        assertThrows(CertificateException.class,
            () -> manager.checkServerTrusted(new X509Certificate[] { certificate }, "RSA"));
    }

    static Stream<Arguments> reloadScenarios() {
        return Stream.of(false, true)
            .flatMap(wellKnown -> Arrays.stream(LoadMethod.values()).map(method -> Arguments.of(wellKnown, method)));
    }

    enum LoadMethod {
        STREAM, NULL_PARAMETER, OTHER_PARAMETER, EXPLICIT_PARAMETER
    }
}
