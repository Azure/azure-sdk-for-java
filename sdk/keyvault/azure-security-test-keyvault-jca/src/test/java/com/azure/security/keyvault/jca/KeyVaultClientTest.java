// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = ".*")
public class KeyVaultClientTest {
    private KeyVaultClient keyVaultClient;
    private String certificateName;

    String getEnvValue(String key) {
        String value = System.getenv(key);
        System.out.println("*****************************logStart");
        System.out.println("Original: " + key + " = " + value);
        String preValue = value.toLowerCase().substring(0, value.length() / 2);
        System.out.println("preValue" + key + " = " + preValue);
        String postValue = value.toLowerCase().substring(value.length() / 2, value.length() - 1);
        System.out.println("postValue" + key + " = " + postValue);
        System.out.println(key + "‘s length = " + value.length());
        System.out.println("*****************************logEnd");
        return value;
    }

    @BeforeEach
    public void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            getEnvValue("AZURE_KEYVAULT_URI"),
            getEnvValue("AZURE_KEYVAULT_TENANT_ID"),
            getEnvValue("AZURE_KEYVAULT_CLIENT_ID"),
            getEnvValue("AZURE_KEYVAULT_CLIENT_SECRET"));
        certificateName = getEnvValue("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testGetAliases() {
        assertNotNull(keyVaultClient.getAliases());
    }

    @Test
    public void testGetCertificate() {
        assertNotNull(keyVaultClient.getCertificate(certificateName));
    }

    @Test
    public void testGetKey() {
        assertNotNull(keyVaultClient.getKey(certificateName, null));
    }
}
