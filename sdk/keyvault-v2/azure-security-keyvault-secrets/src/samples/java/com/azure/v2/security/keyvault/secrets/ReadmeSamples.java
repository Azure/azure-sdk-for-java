// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;
import io.clientcore.core.http.models.HttpResponseException;

import java.time.OffsetDateTime;

@SuppressWarnings("unused")
public class ReadmeSamples {
    private final SecretClient secretClient = new SecretClientBuilder()
        .endpoint("<your-key-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

    public void createSecretClient() {
        // BEGIN: readme-sample-createSecretClient
        SecretClient secretClient = new SecretClientBuilder()
            .endpoint("<your-key-vault-url>")
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
        Poller<DeletedSecret, Void> deletedSecretPoller = null;
            //secretClient.beginDeleteSecret("<secret-name>");

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

    public void troubleshooting() {
        // BEGIN: readme-sample-troubleshooting
        try {
            secretClient.getSecret("<deleted-secret-name>");
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
        // END: readme-sample-troubleshooting
    }
}
