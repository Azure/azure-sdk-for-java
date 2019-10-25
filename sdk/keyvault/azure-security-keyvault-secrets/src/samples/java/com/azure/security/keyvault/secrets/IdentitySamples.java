// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.ChainedTokenCredential;
import com.azure.identity.ChainedTokenCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.DeviceCodeCredential;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;

/**
 * Samples for azure-identity readme.
 */
public class IdentitySamples {
    /**
     * A sample for authenticating a key vault secret client with a default credential.
     */
    public void authenticateWithDefaultAzureCredential() {
        DefaultAzureCredential defaultCredential = new DefaultAzureCredentialBuilder().build();

        // Azure SDK client builders accept the credential as a parameter

        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(defaultCredential)
            .buildClient();

        KeyVaultSecret secret = client.getSecret("{SECRET_NAME}");
        System.out.println(secret.getValue());
    }

    /**
     * A sample for authenticating a key vault secret client with a client secret credential.
     */
    public void authenticateWithClientSecretCredential() {
        // authenticate with client secret,
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .clientSecret("<YOUR_CLIENT_SECRET>")
            .tenantId("<YOUR_TENANT_ID>")
            .build();

        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(clientSecretCredential)
            .buildClient();

        KeyVaultSecret secret = client.getSecret("{SECRET_NAME}");
        System.out.println(secret.getValue());
    }

    /**
     * A sample for authenticating a key vault secret client with a device code credential.
     */
    public void authenticateWithDeviceCodeCredential() {
        // authenticate with client secret,
        DeviceCodeCredential deviceCodeCredential = new DeviceCodeCredentialBuilder()
                .challengeConsumer(challenge -> {
                    // lets user know of the challenge, e.g., display the message on an IoT device
                    displayMessage(challenge.getMessage());
                })
                .build();

        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(deviceCodeCredential)
            .buildClient();

        KeyVaultSecret secret = client.getSecret("{SECRET_NAME}");
        System.out.println(secret.getValue());
    }

    /**
     * A sample for authenticating a key vault secret client with a username password credential.
     */
    public void authenticateWithUsernamePasswordCredential() {
        // authenticate with client secret,
        UsernamePasswordCredential usernamePasswordCredential = new UsernamePasswordCredentialBuilder()
                .clientId("<YOUR_CLIENT_ID>")
                .username("<YOUR_USERNAME>")
                .password("<YOUR_PASSWORD>")
                .build();

        SecretClient client = new SecretClientBuilder()
                .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(usernamePasswordCredential)
                .buildClient();

        KeyVaultSecret secret = client.getSecret("{SECRET_NAME}");
        System.out.println(secret.getValue());
    }

    /**
     * A sample for authenticating a key vault secret client with a chained credential.
     */
    public void authenticateWithChainedCredential() {
        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .build();

        ClientSecretCredential secondServicePrincipal = new ClientSecretCredentialBuilder()
            .clientId("<YOUR_CLIENT_ID>")
            .clientSecret("<YOUR_CLIENT_SECRET>")
            .tenantId("<YOUR_TENANT_ID>")
            .build();

        // when an access token is requested, the chain will try each
        // credential in order, stopping when one provides a token

        ChainedTokenCredential credentialChain = new ChainedTokenCredentialBuilder()
            .addLast(managedIdentityCredential)
            .addLast(secondServicePrincipal)
            .build();

        // the chain can be used anywhere a credential is required
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(credentialChain)
            .buildClient();

        KeyVaultSecret secret = client.getSecret("{SECRET_NAME}");
        System.out.println(secret.getValue());
    }

    private void displayMessage(String message) {
        System.out.println(message);
    }
}
