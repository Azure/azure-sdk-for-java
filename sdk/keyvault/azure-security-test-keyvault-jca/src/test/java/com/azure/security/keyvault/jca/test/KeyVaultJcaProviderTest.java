// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit tests for the KeyVaultProvider class.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class KeyVaultJcaProviderTest {


    /**
     * Test getting a certificate using the Provider.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testGetCertificate() throws Exception {
        PropertyConvertorUtils.putEnvironmentPropertyToSystemPropertyForKeyVaultJca();
        PropertyConvertorUtils.addKeyVaultJcaProvider();
        KeyStore keystore = PropertyConvertorUtils.getKeyVaultKeyStore();
        assertNotNull(keystore.getCertificate(System.getenv("AZURE_KEYVAULT_CERTIFICATE_NAME")));
    }
}
