// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.ProviderException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FileSystemCertificatesTest {

    FileSystemCertificates fileSystemCertificates;

    public static String getFilePath(String packageName) {
        String filepath = "\\src\\test\\resources\\" + packageName;
        return System.getProperty("user.dir") + filepath.replace("\\", System.getProperty("file.separator"));
    }

    @Test
    public void testSetCertificateEntry() {
        fileSystemCertificates = FileSystemCertificates.FileSystemCertificatesFactory.getCustomFileSystemCertificates(getFilePath("custom\\"));
        fileSystemCertificates.loadCertificatesFromFileSystem();
        Assertions.assertTrue(fileSystemCertificates.getAliases().contains("sideload"));
    }

    @Test
    public void testFileSystemCertificatePath() {
        // Test them together cause System.setProperty will interdependent
        System.setProperty("azure.keyvault.uri", "https://fake-vaule.vault.azure.net/");
        System.setProperty("azure.cert-path.well-known", getFilePath("well-known\\"));
        System.setProperty("azure.cert-path.custom", getFilePath("custom\\"));
        KeyVaultKeyStore ks = new KeyVaultKeyStore();
        ks.engineLoad(null);
        X509Certificate customCertificate = getCertificateByFile(new File(getFilePath("custom\\sideload.x509")));
        X509Certificate wellKnownCertificate = getCertificateByFile(new File(getFilePath("well-known\\sideload.pem")));
        assertEquals(wellKnownCertificate, ks.engineGetCertificate("sideload"));
        assertNotEquals(customCertificate, ks.engineGetCertificate("sideload"));

    }


    public X509Certificate getCertificateByFile(File file) {
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
