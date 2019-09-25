// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.credential.ChainedTokenCredential;
import com.azure.identity.credential.ChainedTokenCredentialBuilder;
import com.azure.identity.credential.ClientSecretCredential;
import com.azure.identity.credential.ClientSecretCredentialBuilder;
import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.identity.credential.DeviceCodeCredential;
import com.azure.identity.credential.DeviceCodeCredentialBuilder;
import com.azure.identity.credential.ManagedIdentityCredential;
import com.azure.identity.credential.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;

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
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(defaultCredential)
            .buildClient();

        Secret secret = client.getSecret("{SECRET_NAME}");
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
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(clientSecretCredential)
            .buildClient();

        Secret secret = client.getSecret("{SECRET_NAME}");
        System.out.println(secret.getValue());
    }

    /**
     * A sample for authenticating a key vault secret client with a device code credential.
     */
    public void authenticateWithDeviceCodeCredential() {
        // authenticate with client secret,
        DeviceCodeCredential deviceCodeCredential = new DeviceCodeCredentialBuilder()
            .deviceCodeChallengeConsumer(challenge -> {
                // lets user know of the challenge, e.g., display the message on an IoT device
                displayMessage(challenge.getMessage());
            })
            .build();

        SecretClient client = new SecretClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(deviceCodeCredential)
            .buildClient();

        Secret secret = client.getSecret("{SECRET_NAME}");
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
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(credentialChain)
            .buildClient();

        Secret secret = client.getSecret("{SECRET_NAME}");
        System.out.println(secret.getValue());
    }

    private void displayMessage(String message) {
        System.out.println(message);
    }
}
