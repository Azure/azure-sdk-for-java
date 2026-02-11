// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.bouncycastle.pkcs.PKCSException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CertificateOrderTest {

    /**
     * Test to verify the certificate chain order from PEM files.
     * The expected order is: end-entity (leaf) cert, intermediate CA(s), root CA.
     */
    @Test
    public void testPemCertificateChainOrder() throws CertificateException, IOException, KeyStoreException,
        NoSuchAlgorithmException, NoSuchProviderException, PKCSException {

        String pemString = new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/SecretBundle.value/3-certificates-in-chain.pem")),
            StandardCharsets.UTF_8);

        Certificate[] certs = CertificateUtil.loadCertificatesFromSecretBundleValue(pemString);

        assertEquals(3, certs.length, "Should have 3 certificates in chain");

        X509Certificate cert0 = (X509Certificate) certs[0];
        X509Certificate cert1 = (X509Certificate) certs[1];
        X509Certificate cert2 = (X509Certificate) certs[2];

        // Certificate 0 should be the end-entity (leaf) certificate with CN=signer
        assertTrue(cert0.getSubjectX500Principal().getName().contains("CN=signer"),
            "First certificate should be the end-entity certificate");

        // Certificate 1 should be the intermediate CA
        assertTrue(cert1.getSubjectX500Principal().getName().contains("CN=Intermediate CA"),
            "Second certificate should be the intermediate CA");

        // Certificate 2 should be the root CA
        assertTrue(cert2.getSubjectX500Principal().getName().contains("CN=Root CA"),
            "Third certificate should be the root CA");

        // Verify the chain: cert0 should be issued by cert1
        assertEquals(cert0.getIssuerX500Principal(), cert1.getSubjectX500Principal(),
            "End-entity cert should be issued by intermediate CA");

        // Verify the chain: cert1 should be issued by cert2
        assertEquals(cert1.getIssuerX500Principal(), cert2.getSubjectX500Principal(),
            "Intermediate CA should be issued by root CA");
    }

    /**
     * Test to verify the certificate chain order from PKCS12 files.
     * The expected order is: end-entity (leaf) cert, intermediate CA(s), root CA.
     */
    @Test
    public void testPkcs12CertificateChainOrder() throws CertificateException, IOException, KeyStoreException,
        NoSuchAlgorithmException, NoSuchProviderException, PKCSException {

        String pfxString = new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/SecretBundle.value/3-certificates-in-chain.pfx")),
            StandardCharsets.UTF_8);

        Certificate[] certs = CertificateUtil.loadCertificatesFromSecretBundleValue(pfxString);

        assertEquals(3, certs.length, "Should have 3 certificates in chain");

        X509Certificate cert0 = (X509Certificate) certs[0];
        X509Certificate cert1 = (X509Certificate) certs[1];
        X509Certificate cert2 = (X509Certificate) certs[2];

        // Certificate 0 should be the end-entity (leaf) certificate
        assertTrue(cert0.getSubjectX500Principal().getName().contains("CN=signer"),
            "First certificate should be the end-entity certificate");

        // Certificate 1 should be the intermediate CA
        assertTrue(cert1.getSubjectX500Principal().getName().contains("CN=Intermediate CA"),
            "Second certificate should be the intermediate CA");

        // Certificate 2 should be the root CA
        assertTrue(cert2.getSubjectX500Principal().getName().contains("CN=Root CA"),
            "Third certificate should be the root CA");

        // Verify the chain: cert0 should be issued by cert1
        assertEquals(cert0.getIssuerX500Principal(), cert1.getSubjectX500Principal(),
            "End-entity cert should be issued by intermediate CA");

        // Verify the chain: cert1 should be issued by cert2
        assertEquals(cert1.getIssuerX500Principal(), cert2.getSubjectX500Principal(),
            "Intermediate CA should be issued by root CA");
    }

    /**
     * Test to verify that the orderCertificateChain method correctly orders
     * a reversed certificate chain (root CA, intermediate, leaf).
     */
    @Test
    public void testOrderCertificateChainReversed() throws CertificateException, IOException, KeyStoreException,
        NoSuchAlgorithmException, NoSuchProviderException, PKCSException {

        String pemString = new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/SecretBundle.value/3-certificates-in-chain.pem")),
            StandardCharsets.UTF_8);

        Certificate[] certs = CertificateUtil.loadCertificatesFromSecretBundleValue(pemString);

        // Reverse the certificate order to simulate the issue
        Certificate[] reversedCerts = new Certificate[certs.length];
        for (int i = 0; i < certs.length; i++) {
            reversedCerts[i] = certs[certs.length - 1 - i];
        }

        // Now order the reversed chain
        Certificate[] orderedCerts = CertificateUtil.orderCertificateChain(reversedCerts);

        assertEquals(3, orderedCerts.length, "Should have 3 certificates in chain");

        X509Certificate cert0 = (X509Certificate) orderedCerts[0];
        X509Certificate cert1 = (X509Certificate) orderedCerts[1];
        X509Certificate cert2 = (X509Certificate) orderedCerts[2];

        // After ordering, certificate 0 should be the end-entity (leaf) certificate
        assertTrue(cert0.getSubjectX500Principal().getName().contains("CN=signer"),
            "First certificate should be the end-entity certificate after ordering");

        // Certificate 1 should be the intermediate CA
        assertTrue(cert1.getSubjectX500Principal().getName().contains("CN=Intermediate CA"),
            "Second certificate should be the intermediate CA after ordering");

        // Certificate 2 should be the root CA
        assertTrue(cert2.getSubjectX500Principal().getName().contains("CN=Root CA"),
            "Third certificate should be the root CA after ordering");
    }

    /**
     * Test to verify that orderCertificateChain handles null and empty arrays correctly.
     */
    @Test
    public void testOrderCertificateChainEdgeCases() {
        // Test null array
        Certificate[] result = CertificateUtil.orderCertificateChain(null);
        assertEquals(null, result, "Should return null for null input");

        // Test empty array
        result = CertificateUtil.orderCertificateChain(new Certificate[0]);
        assertEquals(0, result.length, "Should return empty array for empty input");

        // Test single certificate
        Certificate[] singleCert = new Certificate[1];
        result = CertificateUtil.orderCertificateChain(singleCert);
        assertEquals(1, result.length, "Should return single certificate unchanged");
    }
}
