// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import io.clientcore.core.util.CoreUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultAdministrationUserAgentPropertiesTest {

    @Test
    public void testAzureConfiguration() {
        Map<String, String> properties = CoreUtils.getProperties("azure-security-keyvault-administration.properties");
        assertTrue(properties.get("name").matches("azure-security-keyvault-administration"));
        assertTrue(properties.get("version").matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*"));
    }
}
