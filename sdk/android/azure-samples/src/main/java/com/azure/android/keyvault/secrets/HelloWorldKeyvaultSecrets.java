// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.keyvault.secrets;

import android.util.Log;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to set, get, update and delete a secret.
 */
public class HelloWorldKeyvaultSecrets {
    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a secret in the key vault.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    private static final String TAG = "HelloSecrets";
    public static void main(String endpoint, ClientSecretCredential clientSecretCredential) throws InterruptedException, IllegalArgumentException {
        /* Instantiate a SecretClient that will be used to call the service. Notice that the client is using default
        Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/README.md)
        for links and instructions. */

        SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(endpoint)
                .credential(clientSecretCredential)
                .buildClient();

        // Let's create a secret holding bank account credentials valid for 1 year. If the secret already exists in the
        // key vault, then a new version of the secret is created.
        secretClient.setSecret(new KeyVaultSecret("BankAccountPassword", "f4G34fMh8v")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        // Let's get the bank secret from the key vault.
        KeyVaultSecret bankSecret = secretClient.getSecret("BankAccountPassword");

        Log.i(TAG, String.format("Secret is returned with name %s and value %s \n", bankSecret.getName(), bankSecret.getValue()));

        // After one year, the bank account is still active, we need to update the expiry time of the secret.
        // The update method can be used to update the expiry attribute of the secret. It cannot be used to update the
        // value of the secret.
        bankSecret.getProperties()
            .setExpiresOn(OffsetDateTime.now().plusYears(1));
        SecretProperties updatedSecret = secretClient.updateSecretProperties(bankSecret.getProperties());

        Log.i(TAG, String.format("Secret's updated expiry time %s \n", updatedSecret.getExpiresOn()));

        // Bank forced a password update for security purposes. Let's change the value of the secret in the key vault.
        // To achieve this, we need to create a new version of the secret in the key vault. The update operation cannot
        // change the value of the secret.
        secretClient.setSecret(new KeyVaultSecret("BankAccountPassword", "bhjd4DDgsa")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        // The bank account was closed, need to delete its credentials from the key vault.
        SyncPoller<DeletedSecret, Void> deletedBankSecretPoller =
            secretClient.beginDeleteSecret("BankAccountPassword");
        PollResponse<DeletedSecret> deletedBankSecretPollResponse = deletedBankSecretPoller.poll();

        Log.i(TAG, "Deleted Date " + deletedBankSecretPollResponse.getValue().getDeletedOn().toString());
        Log.i(TAG, "Deleted Secret's Recovery Id " + deletedBankSecretPollResponse.getValue().getRecoveryId());

        // Secret is being deleted on the server.
        deletedBankSecretPoller.waitForCompletion();

        // If the key vault is soft-delete enabled, then deleted secrets need to be purged for permanent deletion.
        secretClient.purgeDeletedSecret("BankAccountPassword");
    }
}
