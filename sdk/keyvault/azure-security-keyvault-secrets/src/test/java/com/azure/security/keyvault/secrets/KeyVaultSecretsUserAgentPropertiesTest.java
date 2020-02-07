// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.util.CoreUtils;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultSecretsUserAgentPropertiesTest {

    @Test
    public void testAzureConfiguration() {
        Map<String, String> properties = CoreUtils.getProperties("azure-key-vault-secrets.properties");
        assertTrue(properties.get("name").matches("azure-security-keyvault-secrets"));
        assertTrue(properties.get("version").matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*"));
    }
}
