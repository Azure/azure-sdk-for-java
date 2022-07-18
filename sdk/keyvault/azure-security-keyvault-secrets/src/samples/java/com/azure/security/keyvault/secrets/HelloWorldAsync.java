// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously set, get, update and delete a secret.
 */
public class HelloWorldAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously set, get, update and delete a secret in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {
        /* Instantiate a SecretAsyncClient that will be used to call the service. Notice that the client is using
        default Azure credentials. To make default credentials work, ensure that the environment variable
        'AZURE_CLIENT_ID' is set with the principal ID of a managed identity that has been given access to your vault.

        To get started, you'll need a URL to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/README.md)
        for links and instructions. */
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create a secret holding bank account credentials valid for 1 year. If the secret already exists in the
        // key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new KeyVaultSecret("BankAccountPassword", "f4G34fMh8v")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))))
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n", secretResponse.getName(), secretResponse.getValue()));

        Thread.sleep(2000);

        // Let's get the bank secret from the key vault.
        secretAsyncClient.getSecret("BankAccountPassword").subscribe(secretResponse ->
                System.out.printf("Secret returned with name %s , value %s %n", secretResponse.getName(), secretResponse.getValue()));

        Thread.sleep(2000);

        // After one year, the bank account is still active, we need to update the expiry time of the secret.
        // The update method can be used to update the expiry attribute of the secret. It cannot be used to update the
        // value of the secret.
        secretAsyncClient.getSecret("BankAccountPassword")
            .subscribe(secretResponse -> {
                KeyVaultSecret secret = secretResponse;

                //Update the expiry time of the secret.
                secret.getProperties()
                    .setExpiresOn(OffsetDateTime.now().plusYears(1));
                secretAsyncClient.updateSecretProperties(secret.getProperties())
                    .subscribe(updatedSecretResponse ->
                        System.out.printf("Secret's updated expiry time %s %n",
                            updatedSecretResponse.getExpiresOn().toString()));
        });

        Thread.sleep(2000);

        // Bank forced a password update for security purposes. Let's change the value of the secret in the key vault.
        // To achieve this, we need to create a new version of the secret in the key vault. The update operation cannot
        // change the value of the secret.
        secretAsyncClient.setSecret("BankAccountPassword", "bhjd4DDgsa")
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n", secretResponse.getName(),
                    secretResponse.getValue()));

        Thread.sleep(2000);

        // The bank account was closed, need to delete its credentials from the key vault.
        secretAsyncClient.beginDeleteSecret("BankAccountPassword")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Deleted Secret Name: " + pollResponse.getValue().getName());
                System.out.println("Deleted Secret Value: " + pollResponse.getValue().getValue());
            });

        // To ensure the secret is deleted server-side.
        Thread.sleep(30000);

        // If the key vault is soft-delete enabled, then for permanent deletion deleted secrets need to be purged.
        secretAsyncClient.purgeDeletedSecretWithResponse("BankAccountPassword")
            .subscribe(purgeResponse ->
                System.out.printf("Bank account secret purge status response %d %n", purgeResponse.getStatusCode()));

        // To ensure the secret is purged server-side.
        Thread.sleep(15000);
    }
}
