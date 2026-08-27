// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import com.azure.security.keyvault.jca.KeyVaultJcaPropertyNames;
import com.azure.security.keyvault.jca.implementation.CertificateVersion;
import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import com.azure.security.keyvault.jca.implementation.TestCertificateVersions;
import com.azure.security.keyvault.jca.implementation.TestKeyVaultClient;
import com.azure.security.keyvault.jca.implementation.model.SecretBundle;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for AIA-based certificate chain completion in {@link CertificateUtil}.
 *
 * <p>Covers the scenario where a non-exportable certificate stored in Azure Key Vault has
 * only its leaf certificate in the secret bundle. The missing intermediate CA certificates
 * must be downloaded via the CA Issuers URL in the AIA extension of each certificate.
 *
 * <p>Tests must run sequentially because they share JVM-global state (system properties, the AIA response cache,
 * and the AIA response loader). Parallel execution would cause property-pollution flakiness.
 */
@Execution(ExecutionMode.SAME_THREAD)
public class AiaCertificateChainTest {

    private static final String AIA_INTERMEDIATE_URL = "http://aia.example.com/intermediate.crt";
    private static final String AIA_ROOT_URL = "http://aia.example.com/root.crt";
    private static final String AIA_BAD_ISSUER_URL = "http://aia.example.com/bad-issuer.crt";
    // Monotonic counter avoids duplicate serial numbers when certificates are created back-to-back
    private static final AtomicLong SERIAL_COUNTER = new AtomicLong(1);

    private static X509Certificate rootCert;
    private static X509Certificate intermediateCert;
    private static X509Certificate leafCert;
    private TestAiaResponseLoader responseLoader;

    @BeforeAll
    static void generateTestChain() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        // Root CA (self-signed, no AIA needed)
        KeyPair rootKeyPair = keyGen.generateKeyPair();
        rootCert = buildCertificate(rootKeyPair.getPublic(), "CN=Test Root CA", "CN=Test Root CA",
            rootKeyPair.getPrivate(), true, null);

        // Intermediate CA (signed by root, AIA points to root cert)
        KeyPair intermediateKeyPair = keyGen.generateKeyPair();
        intermediateCert = buildCertificate(intermediateKeyPair.getPublic(), "CN=Test Intermediate CA",
            "CN=Test Root CA", rootKeyPair.getPrivate(), true, AIA_ROOT_URL);

        // Leaf certificate (signed by intermediate, AIA points to intermediate cert)
        KeyPair leafKeyPair = keyGen.generateKeyPair();
        leafCert = buildCertificate(leafKeyPair.getPublic(), "CN=Test Leaf", "CN=Test Intermediate CA",
            intermediateKeyPair.getPrivate(), false, AIA_INTERMEDIATE_URL);
    }

    @BeforeEach
    void setupClean() {
        // Ensure each test starts with a clean state - clear the disable property
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD);
        AiaCertificateChainUtil.clearAiaCache();
        responseLoader = new TestAiaResponseLoader();
        AiaCertificateChainUtil.setResponseLoader(responseLoader);
    }

    @AfterEach
    void cleanup() {
        // Clear the property after each test to prevent interference with subsequent tests
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD);
        AiaCertificateChainUtil.clearAiaCache();
        AiaCertificateChainUtil.resetResponseLoader();
    }

    // -----------------------------------------------------------------------
    // Chain-completion gating tests
    // -----------------------------------------------------------------------

    @Test
    void shouldNotCompleteNullOrEmptyChainViaAia() {
        assertFalse(AiaCertificateChainUtil.shouldCompleteChainViaAia(null));
        assertFalse(AiaCertificateChainUtil.shouldCompleteChainViaAia(new Certificate[0]));
    }

    @Test
    void shouldNotCompleteChainEndingInSelfSignedRootViaAia() {
        assertFalse(AiaCertificateChainUtil.shouldCompleteChainViaAia(new Certificate[] { rootCert }));
        assertFalse(AiaCertificateChainUtil
            .shouldCompleteChainViaAia(new Certificate[] { leafCert, intermediateCert, rootCert }));
    }

    @Test
    void shouldCompleteChainEndingInNonSelfSignedCertificateViaAia() {
        assertTrue(AiaCertificateChainUtil.shouldCompleteChainViaAia(new Certificate[] { leafCert }));
        assertTrue(AiaCertificateChainUtil.shouldCompleteChainViaAia(new Certificate[] { leafCert, intermediateCert }));
    }

    @Test
    void shouldCompleteChainWithMissingIntermediateViaAia() {
        assertTrue(AiaCertificateChainUtil.shouldCompleteChainViaAia(new Certificate[] { leafCert, rootCert }));
    }

    // -----------------------------------------------------------------------
    // completeChainViaAia tests
    // -----------------------------------------------------------------------

    @Test
    void completeChainViaAiaLeafOnlyDownloadsIntermediateAndRoot() throws Exception {
        // Simulate AKV returning only the leaf cert (non-exportable, leaf-only secret)
        Certificate[] leafOnly = new Certificate[] { leafCert };

        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());
        addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());

        Certificate[] completed = AiaCertificateChainUtil.completeChainViaAia(leafOnly, false);

        assertEquals(3, completed.length, "Chain should contain leaf + intermediate + root");
        assertEquals(leafCert, completed[0], "First cert should be the leaf");
        assertEquals(intermediateCert, completed[1], "Second cert should be the intermediate CA");
        assertEquals(rootCert, completed[2], "Third cert should be the root CA");
    }

    @Test
    void completeChainViaAiaLeafAndIntermediateDownloadsRootOnly() throws Exception {
        // Chain already has leaf + intermediate; only root is missing
        Certificate[] partial = new Certificate[] { leafCert, intermediateCert };

        addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());

        Certificate[] completed = AiaCertificateChainUtil.completeChainViaAia(partial, false);

        assertEquals(3, completed.length, "Chain should contain leaf + intermediate + root");
        assertEquals(rootCert, completed[2]);
    }

    @Test
    void completeChainViaAiaFullChainNoDownloadNeeded() throws Exception {
        // Already complete: root is self-signed, no AIA download should happen
        Certificate[] full = new Certificate[] { leafCert, intermediateCert, rootCert };

        Certificate[] result = AiaCertificateChainUtil.completeChainViaAia(full, false);

        assertEquals(3, result.length);
        assertEquals(0, responseLoader.getTotalCallCount());
    }

    @Test
    void completeChainViaAiaDownloadFailsReturnsOriginal() throws Exception {
        Certificate[] leafOnly = new Certificate[] { leafCert };

        addAiaResponse(AIA_INTERMEDIATE_URL, null);

        Certificate[] result = AiaCertificateChainUtil.completeChainViaAia(leafOnly, false);

        assertEquals(1, result.length, "Should return original chain when download fails");
    }

    @Test
    void completeChainViaAiaNullInputReturnsNull() {
        assertNull(AiaCertificateChainUtil.completeChainViaAia(null, false));
    }

    @Test
    void completeChainViaAiaEmptyInputReturnsEmpty() {
        Certificate[] result = AiaCertificateChainUtil.completeChainViaAia(new Certificate[0], false);
        assertEquals(0, result.length);
    }

    // -----------------------------------------------------------------------
    // downloadIssuerCertificateFromAia tests
    // -----------------------------------------------------------------------

    @Test
    void downloadIssuerCertificateFromAiaReturnsDerEncodedCert() throws Exception {
        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());

        X509Certificate result = AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert);

        assertNotNull(result);
        assertEquals(intermediateCert, result);
    }

    @Test
    void downloadIssuerCertificateFromAiaNoCertWithoutAiaReturnsNull() throws Exception {
        // Root cert has no AIA extension
        X509Certificate result = AiaCertificateChainUtil.downloadIssuerCertificateFromAia(rootCert);
        assertNull(result);
    }

    @Test
    void downloadIssuerCertificateFromAiaPemBundleSelectsMatchingIssuer() throws Exception {
        String pemBundle = toPem(rootCert) + toPem(intermediateCert);

        addAiaResponse(AIA_INTERMEDIATE_URL, pemBundle.getBytes(StandardCharsets.UTF_8));

        X509Certificate result = AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert);

        assertNotNull(result);
        assertEquals(intermediateCert, result,
            "Should select the matching issuer from PEM bundle, not the first certificate");
    }

    @Test
    void completeChainViaAiaRejectsIssuerWithoutKeyCertSign() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        KeyPair badIssuerKeyPair = keyGen.generateKeyPair();
        X509Certificate badIssuerCert = buildCertificate(badIssuerKeyPair.getPublic(), "CN=Bad Issuer", "CN=Bad Issuer",
            badIssuerKeyPair.getPrivate(), true, null, KeyUsage.digitalSignature);

        KeyPair leafKeyPair = keyGen.generateKeyPair();
        X509Certificate leafWithBadIssuerAia = buildCertificate(leafKeyPair.getPublic(), "CN=Leaf With Bad Issuer",
            "CN=Bad Issuer", badIssuerKeyPair.getPrivate(), false, AIA_BAD_ISSUER_URL);

        addAiaResponse(AIA_BAD_ISSUER_URL, badIssuerCert.getEncoded());

        Certificate[] result
            = AiaCertificateChainUtil.completeChainViaAia(new Certificate[] { leafWithBadIssuerAia }, false);

        assertEquals(1, result.length,
            "Issuer without keyCertSign should be rejected even if basicConstraints indicates CA");
        assertEquals(leafWithBadIssuerAia, result[0]);
    }

    @Test
    void completeChainViaAiaRejectsExpiredIssuer() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        // Build an issuer that is a valid CA in every respect EXCEPT that it has already expired.
        // Chain completion must reject it so it is never inserted into the chain.
        Date expiredNotBefore = new Date(System.currentTimeMillis() - 86_400_000L * 30);
        Date expiredNotAfter = new Date(System.currentTimeMillis() - 86_400_000L);
        KeyPair expiredIssuerKeyPair = keyGen.generateKeyPair();
        X509Certificate expiredIssuerCert
            = buildCertificate(expiredIssuerKeyPair.getPublic(), "CN=Expired Issuer", "CN=Expired Issuer",
                expiredIssuerKeyPair.getPrivate(), true, null, KeyUsage.keyCertSign, expiredNotBefore, expiredNotAfter);

        KeyPair leafKeyPair = keyGen.generateKeyPair();
        X509Certificate leafWithExpiredAia = buildCertificate(leafKeyPair.getPublic(), "CN=Leaf", "CN=Expired Issuer",
            expiredIssuerKeyPair.getPrivate(), false, AIA_BAD_ISSUER_URL);

        addAiaResponse(AIA_BAD_ISSUER_URL, expiredIssuerCert.getEncoded());

        Certificate[] result
            = AiaCertificateChainUtil.completeChainViaAia(new Certificate[] { leafWithExpiredAia }, false);

        assertEquals(1, result.length,
            "An expired issuer certificate must be rejected and not inserted into the chain");
        assertEquals(leafWithExpiredAia, result[0]);
    }

    // -----------------------------------------------------------------------
    // PKIX path-building tests – reproduce and verify the reported bug
    //
    // The issue reporter sees:
    //   "PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
    //    unable to find valid certification path to requested target"
    //
    // These tests confirm that:
    //  (a) the error is reproducible WITHOUT our fix (leaf-only chain), and
    //  (b) it is resolved WITH our fix (chain completed via AIA download).
    // -----------------------------------------------------------------------

    /**
     * Reproduces the exact error from the issue without our fix.
     *
     * <p>When Azure Key Vault returns only the leaf certificate (non-exportable key, leaf-only
     * secret bundle), the PKIX path builder cannot trace a path to the trusted root CA because
     * the intermediate CA certificate is absent. This is the root cause of the reported warning:
     * <pre>
     *   PKIX path building failed: unable to find valid certification path to requested target
     * </pre>
     */
    @Test
    void pkixPathBuildingWithoutFixFailsWithReportedError() throws Exception {
        // Trust store contains only the root CA – mirrors the system JRE cacerts behaviour
        Set<TrustAnchor> trustAnchors = Collections.singleton(new TrustAnchor(rootCert, null));

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(leafCert);

        PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, selector);
        params.setRevocationEnabled(false);

        // Only the leaf cert is available – this is what AKV returns without our fix
        params.addCertStore(
            CertStore.getInstance("Collection", new CollectionCertStoreParameters(Collections.singleton(leafCert))));

        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");

        CertPathBuilderException exception = assertThrows(CertPathBuilderException.class, () -> builder.build(params),
            "PKIX path building must fail when the intermediate CA is missing");

        // The CertPathBuilderException carries the inner error message directly.
        // jarsigner then surfaces the full warning as:
        //   "PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
        //    unable to find valid certification path to requested target"
        // Verify the root message matches what the issue reporter sees.
        // Use String.valueOf() to guard against null message across different JDKs/providers
        String message = String.valueOf(exception.getMessage());
        assertTrue(message.contains("unable to find valid certification path to requested target"),
            "Exception message should match the reported error. Actual: " + message);
    }

    /**
     * Verifies that our AIA-based chain-completion fix resolves the reported PKIX error.
     *
     * <p>After {@link CertificateUtil#completeChainViaAia} downloads the missing intermediate CA,
     * the full chain (leaf → intermediate → root) is present and PKIX path building succeeds.
     */
    @Test
    void pkixPathBuildingWithFixSucceeds() throws Exception {
        // Simulate AKV returning only the leaf cert – the broken starting state
        Certificate[] leafOnly = new Certificate[] { leafCert };
        Certificate[] completedChain;

        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());
        addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());
        completedChain = AiaCertificateChainUtil.completeChainViaAia(leafOnly, false);

        assertEquals(3, completedChain.length, "Chain should be leaf + intermediate + root after fix");

        // Now try PKIX path building with the completed chain – this is what jarsigner does
        Set<TrustAnchor> trustAnchors = Collections.singleton(new TrustAnchor(rootCert, null));

        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(leafCert);

        PKIXBuilderParameters params = new PKIXBuilderParameters(trustAnchors, selector);
        params.setRevocationEnabled(false);

        List<Certificate> certList = Arrays.asList(completedChain);
        params.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList)));

        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");

        // Should NOT throw – the full chain enables successful path validation
        CertPathBuilderResult result = builder.build(params);
        assertNotNull(result, "PKIX path building must succeed with the completed chain");
        assertEquals(2, result.getCertPath().getCertificates().size(),
            "Path should contain leaf + intermediate (root is the trust anchor, not in path)");
    }

    @Test
    void aiaDownloadCanBeDisabled() throws Exception {
        Certificate[] result = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert), true);

        assertEquals(1, result.length, "Chain should remain unchanged when AIA download is disabled");
        assertEquals(leafCert, result[0], "The returned certificate should be the leaf certificate");
        assertEquals(0, responseLoader.getTotalCallCount());
    }

    @Test
    void keyVaultClientKeepsAiaDownloadSettingFromConstruction() throws Exception {
        String secretId = "https://fake.vault.azure.net/secrets/aia-test/version";
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue(toPem(leafCert));
        CertificateVersion certificateVersion
            = TestCertificateVersions.create("aia-test", null, null, secretId, false, null);
        KeyVaultClient keyVaultClient = new TestKeyVaultClient("test-token", true, (uri, headers) -> {
            assertEquals(secretId + HttpUtil.API_VERSION_POSTFIX, uri);
            return JsonConverterUtil.toJson(secretBundle);
        });

        // Simulate another SSL bundle replacing the JVM-global value before this client lazily loads its chain.
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD, "false");

        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());
        addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());

        Certificate[] result = keyVaultClient.getCertificateChainForVersion(certificateVersion);

        assertArrayEquals(new Certificate[] { leafCert }, result,
            "The client must keep the AIA setting captured when it was constructed");
        assertEquals(0, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
        assertEquals(0, responseLoader.getCallCount(AIA_ROOT_URL));
    }

    @Test
    void keyVaultClientKeepsAiaDownloadEnabledFromConstruction() throws Exception {
        String secretId = "https://fake.vault.azure.net/secrets/aia-enabled/version";
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue(toPem(leafCert));
        CertificateVersion certificateVersion
            = TestCertificateVersions.create("aia-enabled", null, null, secretId, false, null);
        KeyVaultClient keyVaultClient = new TestKeyVaultClient("test-token", false, (uri, headers) -> {
            assertEquals(secretId + HttpUtil.API_VERSION_POSTFIX, uri);
            return JsonConverterUtil.toJson(secretBundle);
        });

        // Simulate another SSL bundle replacing the JVM-global value before this client lazily loads its chain.
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD, "true");

        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());
        addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());

        Certificate[] result = keyVaultClient.getCertificateChainForVersion(certificateVersion);

        assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, result,
            "The client must keep the AIA setting captured when it was constructed");
        assertEquals(1, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
        assertEquals(1, responseLoader.getCallCount(AIA_ROOT_URL));
    }

    // -----------------------------------------------------------------------
    // Certificate-loading integration tests
    // -----------------------------------------------------------------------

    @Test
    void loadCertificatesCompletesLeafOnlyChain() throws Exception {
        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());
        addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());

        Certificate[] result = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert), false);

        assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, result,
            "A leaf-only bundle must be completed up to the root CA");
    }

    @Test
    void loadCertificatesCompletesChainWithMissingIntermediate() throws Exception {
        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());

        Certificate[] result
            = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert) + toPem(rootCert), false);

        assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, result,
            "An intermediate missing in the middle of the chain must still be downloaded");
        assertEquals(0, responseLoader.getCallCount(AIA_ROOT_URL));
    }

    @Test
    void loadCertificatesCompletesChainWithoutRootAndCachesIssuer() throws Exception {
        addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());

        Certificate[] firstResult
            = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert) + toPem(intermediateCert), false);
        Certificate[] secondResult
            = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert) + toPem(intermediateCert), false);

        assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, firstResult,
            "A contiguous chain must still be completed when its terminal certificate is not self-signed");
        assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, secondResult,
            "A subsequent load must reuse the cached root certificate");
        assertEquals(1, responseLoader.getCallCount(AIA_ROOT_URL));
        assertEquals(0, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void loadCertificatesSkipsAiaForCompleteChain() throws Exception {
        Certificate[] result = CertificateUtil
            .loadCertificatesFromSecretBundleValue(toPem(leafCert) + toPem(intermediateCert) + toPem(rootCert), false);

        assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, result);
        assertEquals(0, responseLoader.getTotalCallCount());
    }

    @Test
    void loadCertificatesKeepsChainWithExpiredIssuerUntouched() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        // An expired CA that still is the issuer of the leaf. Expiry is only rejected for certificates downloaded
        // via AIA, so a chain returned by Key Vault must keep its order and must not trigger a download.
        Date expiredNotBefore = new Date(System.currentTimeMillis() - 86_400_000L * 30);
        Date expiredNotAfter = new Date(System.currentTimeMillis() - 86_400_000L);
        KeyPair expiredCaKeyPair = keyGen.generateKeyPair();
        X509Certificate expiredCaCert = buildCertificate(expiredCaKeyPair.getPublic(), "CN=Expired CA", "CN=Expired CA",
            expiredCaKeyPair.getPrivate(), true, null, KeyUsage.keyCertSign, expiredNotBefore, expiredNotAfter);

        KeyPair leafKeyPair = keyGen.generateKeyPair();
        X509Certificate leafOfExpiredCa = buildCertificate(leafKeyPair.getPublic(), "CN=Leaf Of Expired CA",
            "CN=Expired CA", expiredCaKeyPair.getPrivate(), false, AIA_INTERMEDIATE_URL);

        Certificate[] result = CertificateUtil
            .loadCertificatesFromSecretBundleValue(toPem(leafOfExpiredCa) + toPem(expiredCaCert), false);

        assertArrayEquals(new Certificate[] { leafOfExpiredCa, expiredCaCert }, result,
            "An expired certificate already in the chain must not change how the chain is ordered");
        assertEquals(0, responseLoader.getTotalCallCount());
    }

    // -----------------------------------------------------------------------
    // AIA response cache tests
    //
    // Issuer certificates are immutable, so the response of each CA Issuers URL
    // is cached to avoid repeated round trips to public CA endpoints. Caching
    // must never shortcut issuer validation.
    // -----------------------------------------------------------------------

    @Test
    void aiaResponseIsCachedAcrossDownloads() throws Exception {
        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());

        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));
        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));

        assertEquals(1, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void cachedAiaResponseIsValidatedBeforeAndAfterForcedRefresh() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        // A certificate claiming the cached issuer's DN but signed by a different key. Neither the cached response
        // nor the forced refresh may skip signature verification.
        KeyPair impostorKeyPair = keyGen.generateKeyPair();
        KeyPair subjectKeyPair = keyGen.generateKeyPair();
        X509Certificate certSignedByAnotherKey = buildCertificate(subjectKeyPair.getPublic(), "CN=Other Leaf",
            "CN=Test Intermediate CA", impostorKeyPair.getPrivate(), false, AIA_INTERMEDIATE_URL);

        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());

        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));
        assertNull(AiaCertificateChainUtil.downloadIssuerCertificateFromAia(certSignedByAnotherKey),
            "A cache hit and its forced refresh must both reject a signature mismatch");

        assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void expiredExtraCertificateDoesNotPreventCachingValidIssuer() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        Date expiredNotBefore = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30));
        Date expiredNotAfter = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        KeyPair expiredExtraKeyPair = keyGen.generateKeyPair();
        X509Certificate expiredExtra
            = buildCertificate(expiredExtraKeyPair.getPublic(), "CN=Expired Extra CA", "CN=Expired Extra CA",
                expiredExtraKeyPair.getPrivate(), true, null, KeyUsage.keyCertSign, expiredNotBefore, expiredNotAfter);
        String pemBundle = toPem(expiredExtra) + toPem(intermediateCert);

        addAiaResponse(AIA_INTERMEDIATE_URL, pemBundle.getBytes(StandardCharsets.UTF_8));

        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));
        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));

        assertEquals(1, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void refreshesCachedResponseWhenIssuerRotates() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        KeyPair rotatedIssuerKeyPair = keyGen.generateKeyPair();
        X509Certificate rotatedIssuer = buildCertificate(rotatedIssuerKeyPair.getPublic(), "CN=Test Intermediate CA",
            "CN=Test Intermediate CA", rotatedIssuerKeyPair.getPrivate(), true, null);
        KeyPair rotatedLeafKeyPair = keyGen.generateKeyPair();
        X509Certificate rotatedLeaf = buildCertificate(rotatedLeafKeyPair.getPublic(), "CN=Rotated Leaf",
            "CN=Test Intermediate CA", rotatedIssuerKeyPair.getPrivate(), false, AIA_INTERMEDIATE_URL);

        responseLoader.addResponses(AIA_INTERMEDIATE_URL, binaryResponse(intermediateCert.getEncoded()),
            binaryResponse(rotatedIssuer.getEncoded()));

        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));
        assertEquals(rotatedIssuer, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(rotatedLeaf));

        assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void suppressesRepeatedMissForSameTarget() throws Exception {
        X509Certificate rotatedLeaf = buildRotatedLeafWithoutMatchingIssuer("CN=Suppressed Leaf");
        List<String> messages = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                messages.add(logRecord.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Logger logger = Logger.getLogger(AiaCertificateChainUtil.class.getName());
        Level originalLevel = logger.getLevel();
        boolean originalUseParentHandlers = logger.getUseParentHandlers();
        logger.addHandler(collector);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);

        try {
            addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());

            assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));
            assertNull(AiaCertificateChainUtil.downloadIssuerCertificateFromAia(rotatedLeaf));
            assertNull(AiaCertificateChainUtil.downloadIssuerCertificateFromAia(rotatedLeaf));

            assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
        } finally {
            logger.removeHandler(collector);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalUseParentHandlers);
        }

        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Cached AIA response for URL")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Starting forced AIA refresh for URL")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Forced AIA refresh for URL")));
        assertTrue(messages.stream().anyMatch(message -> message.startsWith("Skipping forced AIA refresh for URL")));
    }

    @Test
    void doesNotSuppressDifferentTarget() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        X509Certificate firstRotatedLeaf = buildRotatedLeafWithoutMatchingIssuer("CN=First Rotated Leaf");

        KeyPair secondIssuerKeyPair = keyGen.generateKeyPair();
        X509Certificate secondIssuer = buildCertificate(secondIssuerKeyPair.getPublic(), "CN=Test Intermediate CA",
            "CN=Test Intermediate CA", secondIssuerKeyPair.getPrivate(), true, null);
        KeyPair secondLeafKeyPair = keyGen.generateKeyPair();
        X509Certificate secondRotatedLeaf = buildCertificate(secondLeafKeyPair.getPublic(), "CN=Second Rotated Leaf",
            "CN=Test Intermediate CA", secondIssuerKeyPair.getPrivate(), false, AIA_INTERMEDIATE_URL);

        responseLoader.addResponses(AIA_INTERMEDIATE_URL, binaryResponse(intermediateCert.getEncoded()),
            binaryResponse(intermediateCert.getEncoded()), binaryResponse(secondIssuer.getEncoded()));

        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));
        assertNull(AiaCertificateChainUtil.downloadIssuerCertificateFromAia(firstRotatedLeaf));
        assertEquals(secondIssuer, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(secondRotatedLeaf));

        assertEquals(3, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void failedRefreshDoesNotReplaceUsefulPositiveEntry() throws Exception {
        X509Certificate rotatedLeaf = buildRotatedLeafWithoutMatchingIssuer("CN=Failed Refresh Leaf");

        responseLoader.addResponses(AIA_INTERMEDIATE_URL, binaryResponse(intermediateCert.getEncoded()),
            binaryResponse(null));

        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));
        assertNull(AiaCertificateChainUtil.downloadIssuerCertificateFromAia(rotatedLeaf));
        assertEquals(intermediateCert, AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert));

        assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void clearAiaCacheForcesNewDownload() throws Exception {
        addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());

        AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert);
        AiaCertificateChainUtil.clearAiaCache();
        AiaCertificateChainUtil.downloadIssuerCertificateFromAia(leafCert);

        assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void failedAiaResponseIsNegativelyCached() {
        addAiaResponse(AIA_INTERMEDIATE_URL, null);

        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());
        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());

        assertEquals(1, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void emptyAiaResponseIsNegativelyCached() {
        addAiaResponse(AIA_INTERMEDIATE_URL, new byte[0]);

        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());
        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());

        assertEquals(1, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void unparseableAiaResponseIsNegativelyCached() {
        addAiaResponse(AIA_INTERMEDIATE_URL, "not a certificate".getBytes(StandardCharsets.UTF_8));

        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());
        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());

        assertEquals(1, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void noStoreAiaResponseIsNotCached() throws Exception {
        responseLoader.addResponses(AIA_INTERMEDIATE_URL, binaryResponse(intermediateCert.getEncoded(), "no-store"));

        assertEquals(intermediateCert,
            AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).get(0));
        assertEquals(intermediateCert,
            AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).get(0));

        assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void noStoreFailedAiaResponseIsNotCached() {
        responseLoader.addResponses(AIA_INTERMEDIATE_URL, binaryResponse(null, "no-store"));

        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());
        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());

        assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void noCacheUnparseableAiaResponseIsNotCached() {
        responseLoader.addResponses(AIA_INTERMEDIATE_URL,
            binaryResponse("not a certificate".getBytes(StandardCharsets.UTF_8), "no-cache"));

        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());
        assertTrue(AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(AIA_INTERMEDIATE_URL).isEmpty());

        assertEquals(2, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
    }

    @Test
    void successfulResponseUsesFallbackTtlWithoutHeaders() {
        long now = 1_000L;

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(binaryResponse(new byte[] { 1 }), now);

        assertEquals(now + TimeUnit.HOURS.toMillis(24), expiresAt);
    }

    @Test
    void successfulResponseHonorsMaxAgeAndAgeHeaders() {
        long now = 1_000L;
        HttpUtil.BinaryHttpResponse response
            = new HttpUtil.BinaryHttpResponse(new byte[] { 1 }, "max-age=300", null, "30", null);

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(response, now);

        assertEquals(now + TimeUnit.SECONDS.toMillis(270), expiresAt);
    }

    @Test
    void successfulResponseAccountsForApparentAgeFromDateHeader() {
        long now = ZonedDateTime.parse("Wed, 5 Aug 2026 12:00:00 GMT", DateTimeFormatter.RFC_1123_DATE_TIME)
            .toInstant()
            .toEpochMilli();
        HttpUtil.BinaryHttpResponse response = new HttpUtil.BinaryHttpResponse(new byte[] { 1 }, "max-age=3600",
            "Wed, 5 Aug 2026 10:00:00 GMT", "0", null);

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(response, now);

        assertEquals(now, expiresAt);
    }

    @Test
    void successfulResponseUsesGreaterOfAgeAndApparentAge() {
        long now = ZonedDateTime.parse("Wed, 5 Aug 2026 10:01:00 GMT", DateTimeFormatter.RFC_1123_DATE_TIME)
            .toInstant()
            .toEpochMilli();
        HttpUtil.BinaryHttpResponse response = new HttpUtil.BinaryHttpResponse(new byte[] { 1 }, "max-age=300",
            "Wed, 5 Aug 2026 10:00:00 GMT", "120", null);

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(response, now);

        assertEquals(now + TimeUnit.SECONDS.toMillis(180), expiresAt);
    }

    @Test
    void successfulResponseWithNoStoreExpiresImmediately() {
        long now = 1_000L;

        long expiresAt
            = AiaCertificateChainUtil.calculateResponseExpiration(binaryResponse(new byte[] { 1 }, "no-store"), now);

        assertEquals(now, expiresAt);
    }

    @Test
    void successfulResponseWithNoCacheExpiresImmediately() {
        long now = 1_000L;

        long expiresAt
            = AiaCertificateChainUtil.calculateResponseExpiration(binaryResponse(new byte[] { 1 }, "no-cache"), now);

        assertEquals(now, expiresAt);
    }

    @Test
    void successfulResponseHonorsExpiresDateAndAgeHeaders() {
        long now = 1_000L;
        HttpUtil.BinaryHttpResponse response = new HttpUtil.BinaryHttpResponse(new byte[] { 1 }, null,
            "Wed, 5 Aug 2026 10:00:00 GMT", "30", "Wed, 5 Aug 2026 10:05:00 GMT");

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(response, now);

        assertEquals(now + TimeUnit.SECONDS.toMillis(270), expiresAt);
    }

    @Test
    void successfulResponseDoesNotReuseExpiredExpiresHeader() {
        long now = ZonedDateTime.parse("Wed, 5 Aug 2026 12:00:00 GMT", DateTimeFormatter.RFC_1123_DATE_TIME)
            .toInstant()
            .toEpochMilli();
        HttpUtil.BinaryHttpResponse response = new HttpUtil.BinaryHttpResponse(new byte[] { 1 }, null,
            "Wed, 5 Aug 2026 10:00:00 GMT", "0", "Wed, 5 Aug 2026 11:00:00 GMT");

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(response, now);

        assertEquals(now, expiresAt);
    }

    @Test
    void malformedFreshnessHeadersUseFallbackTtl() {
        long now = 1_000L;
        HttpUtil.BinaryHttpResponse response = new HttpUtil.BinaryHttpResponse(new byte[] { 1 }, "max-age=invalid",
            "not-a-date", "invalid", "also-not-a-date");

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(response, now);

        assertEquals(now + TimeUnit.HOURS.toMillis(24), expiresAt);
    }

    @Test
    void overflowingAgeMakesResponseImmediatelyStale() {
        long now = 1_000L;
        HttpUtil.BinaryHttpResponse response = new HttpUtil.BinaryHttpResponse(new byte[] { 1 }, "max-age=300", null,
            "999999999999999999999999999999999999999999", null);

        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(response, now);

        assertEquals(now, expiresAt);
    }

    @Test
    void responseExpirationIsIndependentOfCandidateValidity() {
        long now = 1_000L;
        long expiresAt = AiaCertificateChainUtil.calculateResponseExpiration(binaryResponse(new byte[] { 1 }), now);

        assertEquals(now + TimeUnit.HOURS.toMillis(24), expiresAt);
    }

    @Test
    void aiaCacheEvictsLeastRecentlyUsedEntryWhenFull() throws Exception {
        String firstUrl = "http://aia.example.com/cache-0.crt";
        HttpUtil.BinaryHttpResponse response = binaryResponse(intermediateCert.getEncoded());
        responseLoader.respondToAnyUrl(ignored -> response);

        AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(firstUrl);

        // Fill the cache past its maximum size so its first entry can no longer be retained.
        for (int i = 1; i <= 128; i++) {
            AiaCertificateChainUtil.fetchCertificatesFromAiaUrl("http://aia.example.com/cache-" + i + ".crt");
        }

        AiaCertificateChainUtil.fetchCertificatesFromAiaUrl(firstUrl);

        assertEquals(2, responseLoader.getCallCount(firstUrl));
    }

    @Test
    void cachedIssuerIsNotReportedAsADownload() throws Exception {
        List<String> messages = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                messages.add(logRecord.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        Logger logger = Logger.getLogger(AiaCertificateChainUtil.class.getName());
        Level originalLevel = logger.getLevel();
        boolean originalUseParentHandlers = logger.getUseParentHandlers();

        logger.addHandler(collector);
        logger.setLevel(Level.FINE);
        logger.setUseParentHandlers(false);

        try {
            addAiaResponse(AIA_INTERMEDIATE_URL, intermediateCert.getEncoded());
            addAiaResponse(AIA_ROOT_URL, rootCert.getEncoded());

            // The second run resolves the same two issuers entirely from the cache.
            AiaCertificateChainUtil.completeChainViaAia(new Certificate[] { leafCert }, false);
            AiaCertificateChainUtil.completeChainViaAia(new Certificate[] { leafCert }, false);

            assertEquals(1, responseLoader.getCallCount(AIA_INTERMEDIATE_URL));
            assertEquals(1, responseLoader.getCallCount(AIA_ROOT_URL));
        } finally {
            logger.removeHandler(collector);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalUseParentHandlers);
        }

        assertEquals(4L, messages.stream().filter(m -> m.startsWith("Resolved issuer certificate via AIA")).count(),
            "Both runs must report resolving the intermediate and the root");

        assertEquals(2L,
            messages.stream().filter(m -> m.startsWith("Downloading issuer certificate from AIA URL")).count(),
            "Only the first run performs downloads; a cache hit must not be reported as one");
    }

    // -----------------------------------------------------------------------
    // Loop-termination tests
    // -----------------------------------------------------------------------

    @Test
    @Timeout(30)
    void completeChainViaAiaTerminatesOnCrossSignedIssuers() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        // Two CA certificates issuing each other. Walking the chain upwards never reaches a self-signed root, and
        // the issuer of the chain's top always sits inside the already-valid prefix, so repositioning it would
        // break that prefix and let the loop oscillate between two arrangements.
        KeyPair keyPairA = keyGen.generateKeyPair();
        KeyPair keyPairB = keyGen.generateKeyPair();
        X509Certificate crossSignedA = buildCertificate(keyPairA.getPublic(), "CN=Cross CA A", "CN=Cross CA B",
            keyPairB.getPrivate(), true, null);
        X509Certificate crossSignedB = buildCertificate(keyPairB.getPublic(), "CN=Cross CA B", "CN=Cross CA A",
            keyPairA.getPrivate(), true, null);

        Certificate[] result
            = AiaCertificateChainUtil.completeChainViaAia(new Certificate[] { crossSignedA, crossSignedB }, false);

        assertArrayEquals(new Certificate[] { crossSignedA, crossSignedB }, result,
            "Cross-signed issuers must be left in place instead of being repositioned");
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private void addAiaResponse(String url, byte[] body) {
        responseLoader.addResponses(url, binaryResponse(body));
    }

    private static HttpUtil.BinaryHttpResponse binaryResponse(byte[] body) {
        return new HttpUtil.BinaryHttpResponse(body, null, null, null, null);
    }

    private static HttpUtil.BinaryHttpResponse binaryResponse(byte[] body, String cacheControl) {
        return new HttpUtil.BinaryHttpResponse(body, cacheControl, null, null, null);
    }

    private static X509Certificate buildRotatedLeafWithoutMatchingIssuer(String subjectDn) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair rotatedIssuerKeyPair = keyGen.generateKeyPair();
        KeyPair rotatedLeafKeyPair = keyGen.generateKeyPair();
        return buildCertificate(rotatedLeafKeyPair.getPublic(), subjectDn, "CN=Test Intermediate CA",
            rotatedIssuerKeyPair.getPrivate(), false, AIA_INTERMEDIATE_URL);
    }

    private static X509Certificate buildCertificate(PublicKey subjectPublicKey, String subjectDn, String issuerDn,
        PrivateKey signingKey, boolean isCa, String aiaUrl) throws Exception {
        return buildCertificate(subjectPublicKey, subjectDn, issuerDn, signingKey, isCa, aiaUrl, null);
    }

    private static X509Certificate buildCertificate(PublicKey subjectPublicKey, String subjectDn, String issuerDn,
        PrivateKey signingKey, boolean isCa, String aiaUrl, Integer keyUsageFlags) throws Exception {

        Date notBefore = new Date(System.currentTimeMillis() - 86_400_000L);
        Date notAfter = new Date(System.currentTimeMillis() + 86_400_000L * 365);

        return buildCertificate(subjectPublicKey, subjectDn, issuerDn, signingKey, isCa, aiaUrl, keyUsageFlags,
            notBefore, notAfter);
    }

    private static X509Certificate buildCertificate(PublicKey subjectPublicKey, String subjectDn, String issuerDn,
        PrivateKey signingKey, boolean isCa, String aiaUrl, Integer keyUsageFlags, Date notBefore, Date notAfter)
        throws Exception {

        X500Name subject = new X500Name(subjectDn);
        X500Name issuer = new X500Name(issuerDn);
        BigInteger serial = BigInteger.valueOf(SERIAL_COUNTER.getAndIncrement());

        JcaX509v3CertificateBuilder builder
            = new JcaX509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, subjectPublicKey);

        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(isCa));

        if (keyUsageFlags != null) {
            builder.addExtension(Extension.keyUsage, true, new KeyUsage(keyUsageFlags));
        }

        if (aiaUrl != null) {
            GeneralName accessLocation = new GeneralName(GeneralName.uniformResourceIdentifier, aiaUrl);
            AccessDescription caIssuers = new AccessDescription(X509ObjectIdentifiers.id_ad_caIssuers, accessLocation);
            builder.addExtension(Extension.authorityInfoAccess, false, new AuthorityInformationAccess(caIssuers));
        }

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(signingKey);
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
    }

    private static String toPem(X509Certificate certificate) throws Exception {
        String base64 = Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(certificate.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + base64 + "\n-----END CERTIFICATE-----\n";
    }

    private static final class TestAiaResponseLoader implements AiaCertificateChainUtil.AiaResponseLoader {
        private final Map<String, List<HttpUtil.BinaryHttpResponse>> responses = new HashMap<>();
        private final Map<String, AtomicInteger> callCounts = new HashMap<>();
        private Function<String, HttpUtil.BinaryHttpResponse> fallback;

        private void addResponses(String url, HttpUtil.BinaryHttpResponse... configuredResponses) {
            responses.put(url, new ArrayList<>(Arrays.asList(configuredResponses)));
        }

        private void respondToAnyUrl(Function<String, HttpUtil.BinaryHttpResponse> responder) {
            fallback = responder;
        }

        private int getCallCount(String url) {
            AtomicInteger count = callCounts.get(url);
            return count == null ? 0 : count.get();
        }

        private int getTotalCallCount() {
            return callCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        }

        @Override
        public HttpUtil.BinaryHttpResponse load(String url) {
            int invocation = callCounts.computeIfAbsent(url, ignored -> new AtomicInteger()).getAndIncrement();
            List<HttpUtil.BinaryHttpResponse> configuredResponses = responses.get(url);
            if (configuredResponses != null && !configuredResponses.isEmpty()) {
                return configuredResponses.get(Math.min(invocation, configuredResponses.size() - 1));
            }
            if (fallback != null) {
                return fallback.apply(url);
            }
            throw new AssertionError("Unexpected AIA request: " + url);
        }
    }
}
