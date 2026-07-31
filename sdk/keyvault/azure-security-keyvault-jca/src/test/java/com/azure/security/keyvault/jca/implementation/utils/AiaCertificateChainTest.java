// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * <p>Tests must run sequentially because they share JVM-global state (system properties and
 * Mockito static mocks). Parallel execution would cause property-pollution flakiness.
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
        System.clearProperty(CertificateUtil.DISABLE_AIA_DOWNLOAD_PROPERTY);
    }

    @AfterEach
    void cleanup() {
        // Clear the property after each test to prevent interference with subsequent tests
        System.clearProperty(CertificateUtil.DISABLE_AIA_DOWNLOAD_PROPERTY);
    }

    // -----------------------------------------------------------------------
    // completeChainViaAia tests
    // -----------------------------------------------------------------------

    @Test
    void completeChainViaAiaLeafOnlyDownloadsIntermediateAndRoot() throws Exception {
        // Simulate AKV returning only the leaf cert (non-exportable, leaf-only secret)
        Certificate[] leafOnly = new Certificate[] { leafCert };

        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_INTERMEDIATE_URL)).thenReturn(intermediateCert.getEncoded());
            httpMock.when(() -> HttpUtil.getBytes(AIA_ROOT_URL)).thenReturn(rootCert.getEncoded());

            Certificate[] completed = CertificateUtil.completeChainViaAia(leafOnly);

            assertEquals(3, completed.length, "Chain should contain leaf + intermediate + root");
            assertEquals(leafCert, completed[0], "First cert should be the leaf");
            assertEquals(intermediateCert, completed[1], "Second cert should be the intermediate CA");
            assertEquals(rootCert, completed[2], "Third cert should be the root CA");
        }
    }

    @Test
    void completeChainViaAiaLeafAndIntermediateDownloadsRootOnly() throws Exception {
        // Chain already has leaf + intermediate; only root is missing
        Certificate[] partial = new Certificate[] { leafCert, intermediateCert };

        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_ROOT_URL)).thenReturn(rootCert.getEncoded());

            Certificate[] completed = CertificateUtil.completeChainViaAia(partial);

            assertEquals(3, completed.length, "Chain should contain leaf + intermediate + root");
            assertEquals(rootCert, completed[2]);
        }
    }

    @Test
    void completeChainViaAiaFullChainNoDownloadNeeded() throws Exception {
        // Already complete: root is self-signed, no AIA download should happen
        Certificate[] full = new Certificate[] { leafCert, intermediateCert, rootCert };

        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            Certificate[] result = CertificateUtil.completeChainViaAia(full);

            assertEquals(3, result.length);
            httpMock.verifyNoInteractions();
        }
    }

    @Test
    void completeChainViaAiaDownloadFailsReturnsOriginal() throws Exception {
        Certificate[] leafOnly = new Certificate[] { leafCert };

        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_INTERMEDIATE_URL)).thenReturn(null);

            Certificate[] result = CertificateUtil.completeChainViaAia(leafOnly);

            assertEquals(1, result.length, "Should return original chain when download fails");
        }
    }

    @Test
    void completeChainViaAiaNullInputReturnsNull() {
        assertNull(CertificateUtil.completeChainViaAia(null));
    }

    @Test
    void completeChainViaAiaEmptyInputReturnsEmpty() {
        Certificate[] result = CertificateUtil.completeChainViaAia(new Certificate[0]);
        assertEquals(0, result.length);
    }

    // -----------------------------------------------------------------------
    // downloadIssuerCertificateFromAia tests
    // -----------------------------------------------------------------------

    @Test
    void downloadIssuerCertificateFromAiaReturnsDerEncodedCert() throws Exception {
        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_INTERMEDIATE_URL)).thenReturn(intermediateCert.getEncoded());

            X509Certificate result = CertificateUtil.downloadIssuerCertificateFromAia(leafCert);

            assertNotNull(result);
            assertEquals(intermediateCert, result);
        }
    }

    @Test
    void downloadIssuerCertificateFromAiaNoCertWithoutAiaReturnsNull() throws Exception {
        // Root cert has no AIA extension
        X509Certificate result = CertificateUtil.downloadIssuerCertificateFromAia(rootCert);
        assertNull(result);
    }

    @Test
    void downloadIssuerCertificateFromAiaPemBundleSelectsMatchingIssuer() throws Exception {
        String pemBundle = toPem(rootCert) + toPem(intermediateCert);

        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_INTERMEDIATE_URL))
                .thenReturn(pemBundle.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            X509Certificate result = CertificateUtil.downloadIssuerCertificateFromAia(leafCert);

            assertNotNull(result);
            assertEquals(intermediateCert, result,
                "Should select the matching issuer from PEM bundle, not the first certificate");
        }
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

        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_BAD_ISSUER_URL)).thenReturn(badIssuerCert.getEncoded());

            Certificate[] result = CertificateUtil.completeChainViaAia(new Certificate[] { leafWithBadIssuerAia });

            assertEquals(1, result.length,
                "Issuer without keyCertSign should be rejected even if basicConstraints indicates CA");
            assertEquals(leafWithBadIssuerAia, result[0]);
        }
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

        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_INTERMEDIATE_URL)).thenReturn(intermediateCert.getEncoded());
            httpMock.when(() -> HttpUtil.getBytes(AIA_ROOT_URL)).thenReturn(rootCert.getEncoded());
            completedChain = CertificateUtil.completeChainViaAia(leafOnly);
        }

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

    /**
     * Verifies that AIA chain completion can be disabled via system property.
     *
     * <p>When the system property {@code azure.keyvault.jca.disable-aia-download} is set to {@code true},
     * the AIA chain completion is skipped and the original chain is returned unchanged.
     */
    @Test
    void aiaDownloadDisabledBySystemProperty() throws Exception {
        // Set the disable system property
        String originalValue = System.getProperty(CertificateUtil.DISABLE_AIA_DOWNLOAD_PROPERTY);
        System.setProperty(CertificateUtil.DISABLE_AIA_DOWNLOAD_PROPERTY, "true");

        try {
            // Simulate AKV returning only the leaf cert
            Certificate[] leafOnly = new Certificate[] { leafCert };

            // Mock HttpUtil BEFORE calling completeChainViaAia to ensure property check
            // doesn't trigger real network I/O if it regresses
            try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
                // Call completeChainViaAia with the property set to true
                // It should return the same array without downloading anything
                Certificate[] result = CertificateUtil.completeChainViaAia(leafOnly);

                // Verify the chain was NOT extended (still only 1 certificate)
                assertEquals(1, result.length, "Chain should remain unchanged when AIA download is disabled");
                assertEquals(leafCert, result[0], "The returned certificate should be the leaf certificate");

                // Verify that no HTTP calls were made (HttpUtil.getBytes should not be called)
                httpMock.verify(() -> HttpUtil.getBytes(Mockito.anyString()), Mockito.never());
            }
        } finally {
            // Clean up: restore the original property value
            if (originalValue != null) {
                System.setProperty(CertificateUtil.DISABLE_AIA_DOWNLOAD_PROPERTY, originalValue);
            } else {
                System.clearProperty(CertificateUtil.DISABLE_AIA_DOWNLOAD_PROPERTY);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Chain-completion gating tests
    //
    // Loading a certificate must only reach out to the network when the chain
    // cannot be walked from the leaf upwards. A contiguous chain already
    // satisfies jarsigner and PKIX path building, so loading it has to stay a
    // fully offline operation.
    // -----------------------------------------------------------------------

    @Test
    void loadCertificatesCompletesLeafOnlyChain() throws Exception {
        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_INTERMEDIATE_URL)).thenReturn(intermediateCert.getEncoded());
            httpMock.when(() -> HttpUtil.getBytes(AIA_ROOT_URL)).thenReturn(rootCert.getEncoded());

            Certificate[] result = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert));

            assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, result,
                "A leaf-only bundle must be completed up to the root CA");
        }
    }

    @Test
    void loadCertificatesCompletesChainWithMissingIntermediate() throws Exception {
        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            httpMock.when(() -> HttpUtil.getBytes(AIA_INTERMEDIATE_URL)).thenReturn(intermediateCert.getEncoded());

            Certificate[] result
                = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert) + toPem(rootCert));

            assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, result,
                "An intermediate missing in the middle of the chain must still be downloaded");
            httpMock.verify(() -> HttpUtil.getBytes(AIA_ROOT_URL), Mockito.never());
        }
    }

    @Test
    void loadCertificatesSkipsAiaForChainWithoutRoot() throws Exception {
        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            Certificate[] result
                = CertificateUtil.loadCertificatesFromSecretBundleValue(toPem(leafCert) + toPem(intermediateCert));

            // The root CA is a trust anchor, so a contiguous leaf -> intermediate chain needs no download.
            assertArrayEquals(new Certificate[] { leafCert, intermediateCert }, result);
            httpMock.verifyNoInteractions();
        }
    }

    @Test
    void loadCertificatesSkipsAiaForCompleteChain() throws Exception {
        try (MockedStatic<HttpUtil> httpMock = Mockito.mockStatic(HttpUtil.class)) {
            Certificate[] result = CertificateUtil
                .loadCertificatesFromSecretBundleValue(toPem(leafCert) + toPem(intermediateCert) + toPem(rootCert));

            assertArrayEquals(new Certificate[] { leafCert, intermediateCert, rootCert }, result);
            httpMock.verifyNoInteractions();
        }
    }

    // -----------------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------------

    private static X509Certificate buildCertificate(java.security.PublicKey subjectPublicKey, String subjectDn,
        String issuerDn, PrivateKey signingKey, boolean isCa, String aiaUrl) throws Exception {
        return buildCertificate(subjectPublicKey, subjectDn, issuerDn, signingKey, isCa, aiaUrl, null);
    }

    private static X509Certificate buildCertificate(java.security.PublicKey subjectPublicKey, String subjectDn,
        String issuerDn, PrivateKey signingKey, boolean isCa, String aiaUrl, Integer keyUsageFlags) throws Exception {

        X500Name subject = new X500Name(subjectDn);
        X500Name issuer = new X500Name(issuerDn);
        Date notBefore = new Date(System.currentTimeMillis() - 86_400_000L);
        Date notAfter = new Date(System.currentTimeMillis() + 86_400_000L * 365);
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
}
