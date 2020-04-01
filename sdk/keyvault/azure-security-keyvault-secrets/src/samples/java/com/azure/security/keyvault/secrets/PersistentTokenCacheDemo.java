// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import java.time.Duration;

/**
 * Sample showing how to authenticate to key vault with a shared token cache credential.
 */
public class PersistentTokenCacheDemo {

    /**
     * Authenticates from shared token cache and gets a secret.
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {

        // Wrote to AZURE_USERNAME env variable
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder()
                .tokenRefreshOffset(Duration.ofMinutes(60)).build();

        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://persistentcachedemo.vault.azure.net")
            .credential(defaultCredential)
            .buildClient();

        // Try to get a secret! Only works if you are logged in
        System.out.println("\nWhat is the super secret secret?\n\n");
        KeyVaultSecret secret = client.getSecret("the-secret");
        System.out.println("Secret was found: " + secret.getValue());
    }
}
