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

    @BeforeEach
    public void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT-SECRET"));
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testGetAliases() {
        System.out.println("*****************************logStart**************************");
        keyVaultClient.getAliases().forEach(System.out::println);
        System.out.println(keyVaultClient.getAliases().size());
        System.out.println("*****************************logEnd**************************");
        assertNotNull(keyVaultClient.getAliases());
    }

    @Test
    public void testGetCertificate() {
        System.out.println("*****************************logStart**************************");
        String certificate_name = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
        System.out.println(System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME").length());
        System.out.println(certificate_name.substring(0, certificate_name.length() - 1));
        System.out.println("*****************************logEnd**************************");
        assertNotNull(keyVaultClient.getCertificate("myalias"));
    }

    @Test
    public void testGetKey() {
        assertNotNull(keyVaultClient.getKey("myalias", null));
    }
}
