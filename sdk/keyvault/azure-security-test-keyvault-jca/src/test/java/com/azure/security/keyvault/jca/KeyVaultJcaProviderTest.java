// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.util.StringUtils;

import java.security.KeyStore;
import java.security.Security;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the KeyVaultProvider class.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = ".*")
public class KeyVaultJcaProviderTest {

    private static final Logger LOGGER = Logger.getLogger(KeyVaultJcaProviderTest.class.getName());

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
    public void setEnvironmentProperty() {
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_URI");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.aad-authentication-url");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_TENANT-ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT-ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT-SECRET");
    }

    /**
     * Test the constructor.
     */
    @Test
    public void testConstructor() {
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        assertNotNull(provider);
    }

    /**
     * Test getting a certificate using the Provider.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testGetCertificate() throws Exception {
        System.out.println(System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME"));
        System.out.println(System.getenv("KEYVAULT_SUBSCRIPTION_ID"));
        Security.addProvider(new KeyVaultJcaProvider());
        KeyStore keystore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-SECRET"));
        keystore.load(parameter);
        assertNotNull(keystore.getCertificate("myalias"));
    }
}
