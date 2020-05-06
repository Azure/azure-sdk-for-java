// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

public class MultiTenantUserDemo {
    public static void main(String[] args) {
        InteractiveBrowserCredential credential = new InteractiveBrowserCredentialBuilder()
            .tenantId("c32df355-c3c7-49eb-8718-dcf53254b0c8")
            .clientId("95546745-0c7e-4d4f-9b9b-2cb66f3e8e03") // demo115 in jianghaolu.onmicrosoft.com
            .port(8765)
            .build();

        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://persistentcachedemo2.vault.azure.net")
            .credential(credential)
            .buildClient();

        // Try to get a secret! Only works if you are logged in
        System.out.println("\nWhat is the super secret secret?\n\n");
        KeyVaultSecret secret = client.getSecret("the-secret");
        System.out.println("Secret was found: " + secret.getValue());
    }
}
