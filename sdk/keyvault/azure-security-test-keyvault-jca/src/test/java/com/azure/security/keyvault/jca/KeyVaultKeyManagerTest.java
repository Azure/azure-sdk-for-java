// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = ".*")
public class KeyVaultKeyManagerTest {
    private static final Logger LOGGER = Logger.getLogger(KeyVaultKeyManagerTest.class.getName());

    private KeyVaultKeyManager manager;
    private String certificateName;

    public static void putEnvironmentPropertyToSystemProperty(String key) {
        Optional.of(key)
                .map(System::getenv)
                .filter(StringUtils::hasText)
                .ifPresent(value -> {
                    System.out.println("*****************************logStart**************************");
                    System.out.println("Original: " + key + " = " + value);
                    String preValue = value.toLowerCase().substring(0, value.length() / 2);
                    System.out.println("preValue" + key + " = " + preValue);
                    String postValue = value.toLowerCase().substring(value.length() / 2, value.length() - 1);
                    System.out.println("postValue" + key + " = " + postValue);
                    System.out.println(key + "‘s length = " + value.length());

                    if (key.equals("AZURE_KEYVAULT_URI")) {
                        System.getProperties().put(
                            key.toLowerCase().replaceAll("_", "."), value);
                    } else {
                        int index = key.lastIndexOf("_");
                        StringBuilder sb = new StringBuilder(key.toLowerCase().replaceAll("_", "."));
                        System.getProperties().put(sb.replace(index, index + 1, "-").toString(), value);
                    }
                    if (System.getProperty("azure.keyvault.client-id") != null) {
                        String property = System.getProperty("azure.keyvault.client-id");
                        System.out.println("Original property : " + key + " = " + property);
                        String propertyPreValue = property.toLowerCase().substring(0, property.length() / 2);
                        System.out.println("property preValue" + key + " = " + propertyPreValue);
                        String propertyPostValue = property.toLowerCase().substring(property.length() / 2,
                            property.length() - 1);
                        System.out.println("property postValue" + key + " = " + propertyPostValue);
                    }

                    System.out.println("*****************************logEnd**************************");
                });
    }

    @BeforeEach
    public void setEnvironmentProperty() throws KeyStoreException, NoSuchAlgorithmException, IOException,
        CertificateException {
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_URI");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.aad-authentication-url");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_TENANT_ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT_ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT_SECRET");
        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        keyStore.load(parameter);
        manager = new KeyVaultKeyManager(keyStore, null);
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testPrivateKey() {
        assertNotNull(manager.getPrivateKey(certificateName));
    }


    @Test
    public void testGetCertificateChain() {
        assertNotNull(manager.getCertificateChain(certificateName));
    }
}
