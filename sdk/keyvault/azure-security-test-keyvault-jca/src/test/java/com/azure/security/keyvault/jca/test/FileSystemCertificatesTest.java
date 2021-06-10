// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class FileSystemCertificatesTest {

    private static String certificateName;

    @BeforeAll
    public static void setEnvironmentProperty() {
        System.setProperty("azure.cert-path.custom", getFilePath());
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(PropertyConvertorUtils.SYSTEM_PROPERTIES);
        PropertyConvertorUtils.addKeyVaultJcaProvider();
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    public static String getFilePath() {
        String filepath = "\\src\\test\\java\\com\\azure\\security\\keyvault\\jca\\certificate\\";
        return System.getProperty("user.dir") + filepath.replace("\\", System.getProperty("file.separator"));
    }

    @Test
    public void testGetFileSystemCertificate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        KeyStore keyStore = PropertyConvertorUtils.getKeyVaultKeyStore();
        Assertions.assertNotNull(keyStore.getCertificate("sideload"));
    }

}
