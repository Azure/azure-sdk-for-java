// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The JUnit tests for the KeyVaultLoadStoreParameter class.
 *
 * @author Manfred Riem (manfred.riem@microsoft.com)
 */
public class KeyVaultLoadStoreParameterTest {

    /**
     * Test getProtectionParameter method.
     */
    @Test
    public void testGetProtectionParameter() {
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(
            System.getProperty("azure.keyvault.uri"),
            null,
            null,
            null
        );
        assertNull(parameter.getProtectionParameter());
    }
}
