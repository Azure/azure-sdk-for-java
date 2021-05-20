// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultClientTest {
    private static KeyVaultClient keyVaultClient;
    private static String certificateName;

    @BeforeAll
    public static void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            System.getenv("AZURE_KEYVAULT_URI"),
            System.getenv("AZURE_KEYVAULT_TENANT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_ID"),
            System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        certificateName = System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testGetAliases() {
        assertTrue(keyVaultClient.getAliases().contains(certificateName));
    }

    @Test
    public void testGetCertificate() {
        List<String> aliases = keyVaultClient.getAliases();
        aliases.stream().forEach(a -> System.out.println(a.charAt(0) + "--" + a.substring(1)));
        assertNotNull(aliases);
        System.out.println("getCertificateName" + certificateName.charAt(0) + "--" + certificateName.substring(1));
        assertNotNull(keyVaultClient.getCertificate(certificateName));
    }

    @Test
    public void testGetKey() {
        assertNotNull(keyVaultClient.getKey(certificateName, null));
    }
}
