package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Arrays;

public class FileSystemCertificatesTest {

    private static String certificateName;

    @BeforeAll
    public static void setEnvironmentProperty() {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemProperty(
            Arrays.asList("AZURE_KEYVAULT_URI",
                "AZURE_KEYVAULT_TENANT_ID",
                "AZURE_KEYVAULT_CLIENT_ID",
                "AZURE_KEYVAULT_CLIENT_SECRET")
        );
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    public static String getFilePath() {
        String filepath = "\\src\\test\\java\\com\\azure\\security\\keyvault\\jca\\certificate\\";
        return System.getProperty("user.dir") + filepath.replace("\\", System.getProperty("file.separator"));
    }

    @Test
    public void testGetFileSystemCertificate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        System.setProperty("azure.jca.path-of-custom-certs", getFilePath());
        KeyStore keyStore = PropertyConvertorUtils.getKeyVaultKeyStore();
        Assertions.assertNotNull(keyStore.getCertificate("sideload"));
    }

}
