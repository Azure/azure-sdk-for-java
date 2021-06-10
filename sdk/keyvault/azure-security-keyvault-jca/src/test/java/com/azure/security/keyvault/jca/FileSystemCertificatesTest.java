// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FileSystemCertificatesTest {

    FileSystemCertificates fileSystemCertificates;

    public static String getFilePath() {
        String filepath = "\\src\\test\\resources\\custom\\";
        return System.getProperty("user.dir") + filepath.replace("\\", System.getProperty("file.separator"));
    }

    @Test
    public void testSetCertificateEntry() {
        fileSystemCertificates = new FileSystemCertificates(getFilePath());
        fileSystemCertificates.loadCertificatesFromFileSystem();
        Assertions.assertTrue(fileSystemCertificates.getAliases().contains("sideload"));
    }

    @Test
    public void testFileSystemCertificatePath() {
        // Test them together cause System.setProperty will interdependent
        System.setProperty("azure.keyvault.uri", "https://fake-vaule.vault.azure.net/");
        System.out.println(System.getProperty("azure.cert-path.custom") + "---------1212");
        KeyVaultKeyStore ks = new KeyVaultKeyStore();
        Assertions.assertEquals(ks.customPath, "/etc/certs/custom/");
        Assertions.assertEquals(ks.wellKnowPath, "/etc/certs/well-known/");
        System.setProperty("azure.keyvault.uri", "https://fake-vaule.vault.azure.net/");
        System.setProperty("azure.cert-path.well-known", "well-known-path");
        System.setProperty("azure.cert-path.custom", "custom-path");
        ks = new KeyVaultKeyStore();
        Assertions.assertEquals(ks.customPath, "custom-path");
        Assertions.assertEquals(ks.wellKnowPath, "well-known-path");
    }

}
