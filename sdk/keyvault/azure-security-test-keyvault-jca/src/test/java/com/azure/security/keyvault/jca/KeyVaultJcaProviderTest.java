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

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the KeyVaultProvider class.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = ".*")
public class KeyVaultJcaProviderTest {

    private static final Logger LOGGER = Logger.getLogger(KeyVaultJcaProviderTest.class.getName());

    String getEnvValue(String key){
        String value = System.getenv(key);
        System.out.println("*****************************logStart");
        System.out.println("Original: " + key + " = " + value);
        String lowerCaseValue = value.toLowerCase();
        System.out.println("lowerCaseValue" + key + " = " + lowerCaseValue);
        String upperCaseValue = value.toLowerCase();
        System.out.println("upperCaseValue" + key + " = " + upperCaseValue);
        String halfValue = value.toLowerCase().substring(value.length() - 1);
        System.out.println("halfValue" + key + " = " + halfValue);
        System.out.println("*****************************logEnd");
        return value;
    }


    public static void putEnvironmentPropertyToSystemProperty(String key) {
        Optional.of(key)
                .map(System::getenv)
                .filter(StringUtils::hasText)
                .ifPresent(value -> {
                    System.out.println("*****************************logStart**************************");
                    System.out.println("Original: " + key + " = " + value);
                    String lowerCaseValue = value.toLowerCase();
                    System.out.println("lowerCaseValue" + key + " = " + lowerCaseValue);
                    String upperCaseValue = value.toUpperCase();
                    System.out.println("upperCaseValue" + key + " = " + upperCaseValue);
                    String halfValue = value.toLowerCase().substring(value.length() - 1);
                    System.out.println("halfValue" + key + " = " + halfValue);
                    System.getProperties().put(
                        key.toLowerCase().replaceAll("_", "."), value);
                    String propertyValue = System.getProperty(key.toLowerCase().replaceAll("_", "."));
                    System.out.println("Original property: " + key + " = " + propertyValue);
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
