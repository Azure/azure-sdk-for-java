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
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = ".*")
public class KeyVaultJcaProviderTest {

    public static void putEnvironmentPropertyToSystemProperty(String key) {
        Optional.of(key)
                .map(System::getenv)
                .filter(StringUtils::hasText)
                .ifPresent(value -> {
                    if (key.equals("AZURE_KEYVAULT_URI")) {
                        System.getProperties().put(
                            key.toLowerCase().replaceAll("_", "."), value);
                    } else {
                        int index = key.lastIndexOf("_");
                        StringBuilder sb = new StringBuilder(key.toLowerCase().replaceAll("_", "."));
                        System.getProperties().put(sb.replace(index, index + 1, "-").toString(), value);
                    }
                });
    }

    @BeforeEach
    public void setEnvironmentProperty() {
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_URI");
        putEnvironmentPropertyToSystemProperty("azure.keyvault.aad-authentication-url");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_TENANT_ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT_ID");
        putEnvironmentPropertyToSystemProperty("AZURE_KEYVAULT_CLIENT_SECRET");
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
            System.getenv("AZURE_KEYVAULT_TENANT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        keystore.load(parameter);
        assertNotNull(keystore.getCertificate(System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME")));
    }
}
