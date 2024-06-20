// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.security.KeyStore;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the KeyVaultProvider class.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultJcaProviderTest {
    private static final Logger LOGGER = Logger.getLogger(KeyVaultJcaProviderTest.class.getName());

    /**
     * Test getting a certificate using the Provider.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testGetCertificate() throws Exception {
        LOGGER.entering("KeyVaultJcaProviderTest", "testGetCertificate");

        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        PropertyConvertorUtils.addKeyVaultJcaProvider();
        KeyStore keystore = PropertyConvertorUtils.getKeyVaultKeyStore();
        assertNotNull(keystore.getCertificate(
            PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CERTIFICATE_NAME")));

        LOGGER.exiting("KeyVaultJcaProviderTest", "testGetCertificate");
    }
}
