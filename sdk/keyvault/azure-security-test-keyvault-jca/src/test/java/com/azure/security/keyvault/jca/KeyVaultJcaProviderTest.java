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

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the KeyVaultProvider class.
 */
@EnabledIfEnvironmentVariable(named = "azure.keyvault.certificate-name", matches = ".*")
public class KeyVaultJcaProviderTest {

    public static void putEnvironmentPropertyToSystemProperty(String key) {
        Optional.of(key)
                .map(System::getenv)
                .filter(StringUtils::hasText)
                .ifPresent(value -> System.getProperties().put(key, value));
    }

    @BeforeEach
    public void setEnvironmentProperty() {
        putEnvironmentPropertyToSystemProperty("azure.keyvault.uri");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.aad-authentication-url");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.tenant-id");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.client-id");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.client-secret");
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
        Security.addProvider(new KeyVaultJcaProvider());
        KeyStore keystore = KeyStore.getInstance("AzureKeyVault");
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("azure.keyvault.uri"),
            System.getenv("azure.keyvault.tenant-id"),
            System.getenv("azure.keyvault.client-id"),
            System.getenv("azure.keyvault.client-secret"));
        keystore.load(parameter);
        assertNotNull(keystore.getCertificate(System.getenv("azure.keyvault.certificate-name")));
    }
}
