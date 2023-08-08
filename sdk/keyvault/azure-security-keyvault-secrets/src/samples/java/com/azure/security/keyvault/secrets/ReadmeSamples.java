// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

@SuppressWarnings("unused")
public class ReadmeSamples {
    private final SecretClient secretClient = new SecretClientBuilder()
        .vaultUrl("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
    private final SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
        .vaultUrl("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

    public void createSecretClient() {
        // BEGIN: readme-sample-createSecretClient
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createSecretClient
    }

    public void createSecret() {
        // BEGIN: readme-sample-createSecret
        KeyVaultSecret secret = secretClient.setSecret("<secret-name>", "<secret-value>");
        System.out.printf("Secret created with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue());
        // END: readme-sample-createSecret
    }

    public void retrieveSecret() {
        // BEGIN: readme-sample-retrieveSecret
        KeyVaultSecret secret = secretClient.getSecret("<secret-name>");
        System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secret.getName(), secret.getValue());
        // END: readme-sample-retrieveSecret
    }

    public void updateSecret() {
        // BEGIN: readme-sample-updateSecret
        // Get the secret to update.
        KeyVaultSecret secret = secretClient.getSecret("<secret-name>");
        // Update the expiry time of the secret.
        secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
        SecretProperties updatedSecretProperties = secretClient.updateSecretProperties(secret.getProperties());
        System.out.printf("Secret's updated expiry time: %s%n", updatedSecretProperties.getExpiresOn());
        // END: readme-sample-updateSecret
    }

    public void deleteSecret() {
        // BEGIN: readme-sample-deleteSecret
        SyncPoller<DeletedSecret, Void> deletedSecretPoller = secretClient.beginDeleteSecret("<secret-name>");

        // Deleted secret is accessible as soon as polling begins.
        PollResponse<DeletedSecret> deletedSecretPollResponse = deletedSecretPoller.poll();

        // Deletion date only works for a SoftDelete-enabled Key Vault.
        System.out.printf("Deletion date: %s%n", deletedSecretPollResponse.getValue().getDeletedOn());

        // Secret is being deleted on server.
        deletedSecretPoller.waitForCompletion();
        // END: readme-sample-deleteSecret
    }

    public void listSecrets() {
        // BEGIN: readme-sample-listSecrets
        // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to
        // get the secret with its value information.
        for (SecretProperties secretProperties : secretClient.listPropertiesOfSecrets()) {
            KeyVaultSecret secretWithValue = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());
            System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secretWithValue.getName(),
                secretWithValue.getValue());
        }
        // END: readme-sample-listSecrets
    }

    public void createSecretAsync() {
        // BEGIN: readme-sample-createSecretAsync
        secretAsyncClient.setSecret("<secret-name>", "<secret-value>")
            .subscribe(secret -> System.out.printf("Created secret with name \"%s\" and value \"%s\"%n",
                secret.getName(), secret.getValue()));
        // END: readme-sample-createSecretAsync
    }

    public void retrieveSecretAsync() {
        // BEGIN: readme-sample-retrieveSecretAsync
        secretAsyncClient.getSecret("<secret-name>")
            .subscribe(secret -> System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n",
                secret.getName(), secret.getValue()));
        // END: readme-sample-retrieveSecretAsync
    }

    public void updateSecretAsync() {
        // BEGIN: readme-sample-updateSecretAsync
        secretAsyncClient.getSecret("<secret-name>")
            .flatMap(secret -> {
                // Update the expiry time of the secret.
                secret.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
                return secretAsyncClient.updateSecretProperties(secret.getProperties());
            }).subscribe(updatedSecretProperties ->
                System.out.printf("Secret's updated expiry time: %s%n", updatedSecretProperties.getExpiresOn()));
        // END: readme-sample-updateSecretAsync
    }

    public void deleteSecretAsync() {
        // BEGIN: readme-sample-deleteSecretAsync
        secretAsyncClient.beginDeleteSecret("<secret-name>")
            .subscribe(pollResponse -> {
                System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
                System.out.printf("Deleted secret name: %s%n", pollResponse.getValue().getName());
                System.out.printf("Deleted secret value: %s%n", pollResponse.getValue().getValue());
            });
        // END: readme-sample-deleteSecretAsync
    }

    public void listSecretsAsync() {
        // BEGIN: readme-sample-listSecretsAsync
        // The List secrets operation returns secrets without their value, so for each secret returned we call `getSecret`
        // to get its value as well.
        secretAsyncClient.listPropertiesOfSecrets()
            .flatMap(secretProperties ->
                secretAsyncClient.getSecret(secretProperties.getName(), secretProperties.getVersion()))
            .subscribe(secretResponse ->
                System.out.printf("Retrieved secret with name \"%s\" and value \"%s\"%n", secretResponse.getName(),
                    secretResponse.getValue()));
        // END: readme-sample-listSecretsAsync
    }

    public void troubleshooting() {
        // BEGIN: readme-sample-troubleshooting
        try {
            secretClient.getSecret("<deleted-secret-name>");
        } catch (ResourceNotFoundException e) {
            System.out.println(e.getMessage());
        }
        // END: readme-sample-troubleshooting
    }
}
