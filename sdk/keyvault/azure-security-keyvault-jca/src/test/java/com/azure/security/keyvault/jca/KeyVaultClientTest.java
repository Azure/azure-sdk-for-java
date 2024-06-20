// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultClientTest {
    private static final Logger LOGGER = Logger.getLogger(KeyVaultClientTest.class.getName());

    private static KeyVaultClient keyVaultClient;
    private static String certificateName;

    @BeforeAll
    public static void setEnvironmentProperty() {
        keyVaultClient = new KeyVaultClient(
            PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT"),
            PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_TENANT_ID"),
            PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_ID"),
            PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_SECRET"));
        certificateName = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CERTIFICATE_NAME");
    }

    @Test
    public void testGetAliases() {
        LOGGER.entering("KeyVaultClientTest", "testGetAliases");

        assertTrue(keyVaultClient.getAliases().contains(certificateName));

        LOGGER.exiting("KeyVaultClientTest", "testGetAliases");
    }

    @Test
    public void testGetCertificate() {
        LOGGER.entering("KeyVaultClientTest", "testGetCertificate");

        assertNotNull(keyVaultClient.getCertificate(certificateName));

        LOGGER.exiting("KeyVaultClientTest", "testGetCertificate");
    }

    @Test
    public void testGetKey() {
        LOGGER.entering("KeyVaultClientTest", "testGetKey");

        assertNotNull(keyVaultClient.getKey(certificateName, null));

        LOGGER.exiting("KeyVaultClientTest", "testGetKey");
    }
}
