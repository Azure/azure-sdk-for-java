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

import static java.util.logging.Level.INFO;
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
                    LOGGER.log(INFO, "LOGGER: the first name of sub = ",
                        System.getenv("KEYVAULT_SUBSCRIPTION_ID").toLowerCase().charAt(0));


                    System.out.println("the first name of certificate name = " +
                        System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME").toLowerCase().charAt(0));
                    System.out.println("the first name of sub = " +
                        System.getenv("KEYVAULT_SUBSCRIPTION_ID").toLowerCase().charAt(0));

                    LOGGER.log(INFO, "LOGGER: the first name of certificate name = ",
                        System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME").toLowerCase().charAt(0));

                    System.out.println("azure certificate length = " +
                        System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME").length());


                    System.out.println("azure certificate length / 2 = " +
                        System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME").substring(0, System.getenv(
                            "AZURE_KEYVAULT_CERTIFICATE_NAME").length() / 2));

                    LOGGER.log(INFO, "LOGGER: azure certificate length / 2 = ",
                        System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME").substring(0, System.getenv(
                            "AZURE_KEYVAULT_CERTIFICATE_NAME").length() / 2));

                    System.out.println("AZURE_KEYVAULT_URI" + System.getenv("AZURE_KEYVAULT_URI"));
                    System.out.println("AZURE_KEYVAULT_CERTIFICATE_NAME" +
                        System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME"));
                    System.out.println("KEYVAULT_ sub id = " + System.getenv("KEYVAULT_SUBSCRIPTION_ID"));
                    System.getProperties().put(
                        key.toLowerCase().replaceAll("_", "."), value);

                    System.out.println("azure.keyvault.uri = " + System.getProperty("azure.keyvault.uri"));
                    System.out.println("azure.keyvault.tenant-id = " + System.getProperty("azure.keyvault.tenant-id"));
                    System.out.println("azure.keyvault.client-secret = " +
                        System.getProperty("azure.keyvault.client-secret"));

                    LOGGER.log(INFO, "LOGGER: azure.keyvault.tenant-id = ",
                        System.getProperty("azure.keyvault.tenant-id"));

                    System.out.println("*****************************logEnd**************************");
                });
    }

    @BeforeEach
    public void setEnvironmentProperty() throws KeyStoreException, NoSuchAlgorithmException, IOException,
        CertificateException {
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_URI");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.aad-authentication-url");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_TENANT-ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT-ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT-SECRET");
        Security.insertProviderAt(new KeyVaultJcaProvider(), 1);
        KeyStore keyStore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-SECRET"));
        keyStore.load(parameter);
        manager = new KeyVaultKeyManager(keyStore, null);
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testPrivateKey() {
        assertNotNull(manager.getPrivateKey("myalias"));
    }


    @Test
    public void testGetCertificateChain() {
        assertNotNull(manager.getCertificateChain(certificateName));
    }
}
