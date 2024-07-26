// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.utils;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import static com.azure.security.keyvault.jca.implementation.utils.CertificateUtil.loadX509CertificateFromFile;
import static com.azure.security.keyvault.jca.implementation.utils.CertificateUtil.loadX509CertificatesFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CertificateUtilTest {

    @Test
    public void loadCertificatesFromSecretBundleValueFromPemTest()
        throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        assertNumberInCertFile("src/test/resources/certificate/download-from-keyvault.pem", 3);
        assertNumberInCertFile("src/test/resources/certificate/download-from-keyvault.pfx.txt", 3);
        assertNumberInCertFile("src/test/resources/custom/sideload2.pem", 1);
        assertNumberInCertFile("src/test/resources/keyvault/sideload2.pem", 1);
        assertNumberInCertFile("src/test/resources/well-known/sideload.pem", 1);
    }

    private void assertNumberInCertFile(String pemFile, int expectedNumber) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException {
        String pemString = new String(Files.readAllBytes(Paths.get(pemFile)), StandardCharsets.UTF_8);
        assertEquals(expectedNumber, CertificateUtil.loadCertificatesFromSecretBundleValue(pemString).length);
    }

    @Test
    public void loadX509CertificateFromFileTest() throws IOException, CertificateException {
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
