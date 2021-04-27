// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The JUnit tests for the KeyVaultLoadStoreParameter class.
 */
@EnabledIfEnvironmentVariable(named = "azure.keyvault.certificate-name", matches = ".*")
public class KeyVaultLoadStoreParameterTest {

    /**
     * Test getProtectionParameter method.
     */
    @Test
    public void testGetProtectionParameter() {
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getenv("azure.keyvault.uri"),
            System.getenv("azure.keyvault.tenant-id"),
            System.getenv("azure.keyvault.client-id"),
            System.getenv("azure.keyvault.client-secret")
        );
        assertNull(parameter.getProtectionParameter());
    }
}
