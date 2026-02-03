// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SecretClientTest {
    /**
     * Tests that a secret can be created in the key vault.
     */
    @Test
    @Disabled
    public void setSecret() {
        SecretClient secretClient = new SecretClientBuilder().endpoint(System.getenv("AZURE_KEYVAULT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build())
            .httpInstrumentationOptions(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS))
            .buildClient();

        KeyVaultSecret keyVaultSecret = secretClient.setSecret("manualTestSecret1", "manualTestValue1");

        System.out.println("Secret created: " + keyVaultSecret.getName() + " with value: " + keyVaultSecret.getValue());
    }
}
