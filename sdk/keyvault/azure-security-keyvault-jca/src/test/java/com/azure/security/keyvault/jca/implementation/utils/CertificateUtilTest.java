// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.bouncycastle.pkcs.PKCSException;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import static com.azure.security.keyvault.jca.implementation.utils.CertificateUtil.loadX509CertificateFromFile;
import static com.azure.security.keyvault.jca.implementation.utils.CertificateUtil.loadX509CertificatesFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CertificateUtilTest {

    @Test
    public void loadCertificateChainFromSecretBundleValueTest()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, PKCSException {
        assertCertNumberInCertChain("src/test/resources/certificate-util/SecretBundle.value/pem-exportable-key.pem", 1);
        assertCertNumberInCertChain("src/test/resources/certificate-util/SecretBundle.value/pem-non-exportable-key.pem", 1);
        assertCertNumberInCertChain("src/test/resources/certificate-util/SecretBundle.value/pkcs12-exportable-key.pfx.test", 1);
        assertCertNumberInCertChain("src/test/resources/certificate-util/SecretBundle.value/pkcs12-non-exportable-key.pfx.test", 1);
        assertCertNumberInCertChain("src/test/resources/certificate-util/SecretBundle.value/3-certificates-in-chain.pem", 3);
        assertCertNumberInCertChain("src/test/resources/certificate-util/SecretBundle.value/3-certificates-in-chain.pfx.test", 3);
    }

    private void assertCertNumberInCertChain(String pemFile, int expectedNumber)
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, PKCSException {
        String pemString = new String(Files.readAllBytes(Paths.get(pemFile)), StandardCharsets.UTF_8);
        assertEquals(expectedNumber, CertificateUtil.loadCertificatesFromSecretBundleValue(pemString).length);
    }

    @Test
    public void loadX509CertificateFromFileTest() throws IOException, CertificateException {
        // CertificateUtil#loadX509CertificateFromFile is used in SpecificPathCertificates and ClassPathCertificates.
        // Not it not support these type of files:
        //   1. pem file with private key.
        //   2. pfx files.
        // It's a new feature to support these files, not implement now.
        // Now just keep the test files and unit test codes, they may be used when implement the feature.
        // certificateCanBeLoadFromFile("src/test/resources/certificate-util/downloaded-from-portal/pem-exportable-key.pem");
        certificateCanBeLoadFromFile("src/test/resources/certificate-util/downloaded-from-portal/pem-non-exportable-key.pem");
        // certificateCanBeLoadFromFile("src/test/resources/certificate-util/downloaded-from-portal/pkcs12-exportable-key.pfx.test");
        // certificateCanBeLoadFromFile("src/test/resources/certificate-util/downloaded-from-portal/pkcs12-non-exportable-key.pfx.test");
        certificateCanBeLoadFromFile("src/test/resources/custom/sideload.x509");
        certificateCanBeLoadFromFile("src/test/resources/custom/sideload2.pem");
        certificateCanBeLoadFromFile("src/test/resources/keyvault/sideload2.pem");
        certificateCanBeLoadFromFile("src/test/resources/well-known/sideload.pem");
    }

    private void certificateCanBeLoadFromFile(String file) throws IOException, CertificateException {
        InputStream inputStream = new FileInputStream(file);
        Certificate certificate = loadX509CertificateFromFile(inputStream);
        assertNotNull(certificate);
    }

    @Test
    public void loadX509CertificatesFromFileTest() throws IOException, CertificateException {
        assertCertificateNumberInInFile("src/test/resources/custom/sideload.x509", 1);
        assertCertificateNumberInInFile("src/test/resources/custom/sideload2.pem", 1);
        assertCertificateNumberInInFile("src/test/resources/keyvault/sideload2.pem", 1);
        assertCertificateNumberInInFile("src/test/resources/well-known/sideload.pem", 1);
    }

    private void assertCertificateNumberInInFile(String file, int expectedCertificateNumber) throws IOException, CertificateException {
        InputStream inputStream = new FileInputStream(file);
        Certificate[] certificates = loadX509CertificatesFromFile(inputStream);
        assertEquals(expectedCertificateNumber, certificates.length);
    }
}
