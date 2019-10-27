// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.SharedTokenCacheCredential;
import com.azure.identity.SharedTokenCacheCredentialBuilder;

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
        SharedTokenCacheCredential defaultCredential = new SharedTokenCacheCredentialBuilder()
            .clientId("04b07795-8ddb-461a-bbee-02f9e1bf7b46")
            .build();

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
