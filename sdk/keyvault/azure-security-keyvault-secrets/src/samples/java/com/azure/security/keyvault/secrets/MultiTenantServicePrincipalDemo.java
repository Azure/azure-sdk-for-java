// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

/**
 * Sample showing how to authenticate to key vault with a shared token cache credential.
 */
public class MultiTenantServicePrincipalDemo {

    /**
     * Authenticates from shared token cache and gets a secret.
     * @param args Unused. Arguments to the program.
     */
    public static void main(String[] args) {

        // Wrote to AZURE_USERNAME env variable
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .tenantId("c32df355-c3c7-49eb-8718-dcf53254b0c8")
            .clientId("95546745-0c7e-4d4f-9b9b-2cb66f3e8e03") // demo115 in jianghaolu.onmicrosoft.com
            .clientSecret("Y/mwDDok2M_Hhsj0xAMn_g5dI-pn54HL")
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
