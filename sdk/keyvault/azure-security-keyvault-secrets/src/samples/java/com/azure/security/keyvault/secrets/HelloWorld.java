// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to set, get, update and delete a secret.
 */
public class HelloWorld {

    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a secret in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException {

        // Instantiate a secret client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient secretClient = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create a secret holding bank account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretClient.setSecret(new KeyVaultSecret("BankAccountPassword", "f4G34fMh8v")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        // Let's Get the bank secret from the key vault.
        KeyVaultSecret bankSecret = secretClient.getSecret("BankAccountPassword");
        System.out.printf("Secret is returned with name %s and value %s \n", bankSecret.getName(), bankSecret.getValue());

        // After one year, the bank account is still active, we need to update the expiry time of the secret.
        // The update method can be used to update the expiry attribute of the secret. It cannot be used to update
        // the value of the secret.
        bankSecret.getProperties()
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        SecretProperties updatedSecret = secretClient.updateSecretProperties(bankSecret.getProperties());
        System.out.printf("Secret's updated expiry time %s \n", updatedSecret.getExpiresOn());

        // Bank forced a password update for security purposes. Let's change the value of the secret in the key vault.
        // To achieve this, we need to create a new version of the secret in the key vault. The update operation cannot
        // change the value of the secret.
        secretClient.setSecret(new KeyVaultSecret("BankAccountPassword", "bhjd4DDgsa")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        // The bank account was closed, need to delete its credentials from the key vault.
        SyncPoller<DeletedSecret, Void> deletedBankSecretPoller
                = secretClient.beginDeleteSecret("BankAccountPassword");

        PollResponse<DeletedSecret> deletedBankSecretPollResponse = deletedBankSecretPoller.poll();

        System.out.println("Deleted Date %s" + deletedBankSecretPollResponse.getValue().getDeletedOn().toString());
        System.out.printf("Deleted Secret's Recovery Id %s", deletedBankSecretPollResponse.getValue().getRecoveryId());

        // Key is being deleted on server.
        deletedBankSecretPoller.waitForCompletion();

        // If the key vault is soft-delete enabled, then for permanent deletion  deleted secrets need to be purged.
        secretClient.purgeDeletedSecret("BankAccountPassword");
    }
}
