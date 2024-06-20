// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.ProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class SpecificPathCertificatesTest {
    private static final Logger LOGGER = Logger.getLogger(SpecificPathCertificatesTest.class.getName());

    @BeforeAll
    public static void setEnvironmentProperty() {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        PropertyConvertorUtils.addKeyVaultJcaProvider();
    }

    public static String getFilePath(String packageName) {
        String filepath = "\\src\\test\\resources\\" + packageName;
        return System.getProperty("user.dir") + filepath.replace("\\", System.getProperty("file.separator"));
    }

    @Test
    public void testGetSpecificPathCertificate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        LOGGER.entering("SpecificPathCertificatesTest", "testGetSpecificPathCertificate");

        System.setProperty("azure.cert-path.custom", getFilePath("custom"));
        KeyStore keyStore = PropertyConvertorUtils.getKeyVaultKeyStore();
        Assertions.assertNotNull(keyStore.getCertificate("sideload"));

        LOGGER.exiting("SpecificPathCertificatesTest", "testGetSpecificPathCertificate");
    }

    @Test
    public void testCertificatePriority1() {
        LOGGER.entering("SpecificPathCertificatesTest", "testCertificatePriority1");

        System.setProperty("azure.cert-path.well-known", getFilePath("well-known\\"));
        System.setProperty("azure.cert-path.custom", getFilePath("custom\\"));
        KeyVaultKeyStore ks = new KeyVaultKeyStore();
        ks.engineLoad(null);
        X509Certificate customCertificate = getCertificateByFile(new File(getFilePath("custom\\sideload.x509")));
        X509Certificate wellKnownCertificate = getCertificateByFile(new File(getFilePath("well-known\\sideload.pem")));
        assertEquals(wellKnownCertificate, ks.engineGetCertificate("sideload"));
        assertNotEquals(customCertificate, ks.engineGetCertificate("sideload"));

        LOGGER.exiting("SpecificPathCertificatesTest", "testCertificatePriority1");
    }

    @Test
    public void testCertificatePriority2() {
        LOGGER.entering("SpecificPathCertificatesTest", "testCertificatePriority2");

        System.setProperty("azure.cert-path.custom", getFilePath("custom\\"));
        KeyVaultKeyStore ks = new KeyVaultKeyStore();
        ks.engineLoad(null);
        X509Certificate specificPathCertificate = getCertificateByFile(new File(getFilePath("custom\\sideload2.pem")));
        X509Certificate classPathCertificate = getCertificateByFile(new File(getFilePath("keyvault\\sideload2.pem")));
        assertEquals(specificPathCertificate, ks.engineGetCertificate("sideload2"));
        assertNotEquals(classPathCertificate, ks.engineGetCertificate("sideload2"));

        LOGGER.exiting("SpecificPathCertificatesTest", "testCertificatePriority2");
    }

    private X509Certificate getCertificateByFile(File file) {
        X509Certificate certificate;
        try (InputStream inputStream = new FileInputStream(file);
             BufferedInputStream bytes = new BufferedInputStream(inputStream)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(bytes);
        } catch (Exception e) {
            throw new ProviderException(e);
        }
        return certificate;
    }

}
