// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;

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

        // Instantiate an async secret client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create a secret holding bank account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new Secret("BankAccountPassword", "f4G34fMh8v")
            .expires(OffsetDateTime.now().plusYears(1))).subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n", secretResponse.name(), secretResponse.value()));

        Thread.sleep(2000);

        // Let's Get the bank secret from the key vault.
        secretAsyncClient.getSecret("BankAccountPassword").subscribe(secretResponse ->
                System.out.printf("Secret returned with name %s , value %s \n", secretResponse.name(), secretResponse.value()));

        Thread.sleep(2000);

        // After one year, the bank account is still active, we need to update the expiry time of the secret.
        // The update method can be used to update the expiry attribute of the secret. It cannot be used to update
        // the value of the secret.
        secretAsyncClient.getSecret("BankAccountPassword").subscribe(secretResponse -> {
            Secret secret = secretResponse;
            //Update the expiry time of the secret.
            secret.expires(secret.expires().plusYears(1));
            secretAsyncClient.updateSecret(secret).subscribe(updatedSecretResponse ->
                System.out.printf("Secret's updated expiry time %s \n", updatedSecretResponse.expires().toString()));
        });

        Thread.sleep(2000);

        // Bank forced a password update for security purposes. Let's change the value of the secret in the key vault.
        // To achieve this, we need to create a new version of the secret in the key vault. The update operation cannot
        // change the value of the secret.
        secretAsyncClient.setSecret("BankAccountPassword", "bhjd4DDgsa").subscribe(secretResponse ->
            System.out.printf("Secret is created with name %s and value %s \n", secretResponse.name(), secretResponse.value()));

        Thread.sleep(2000);

        // The bank account was closed, need to delete its credentials from the key vault.
        secretAsyncClient.deleteSecret("BankAccountPassword").subscribe(deletedSecretResponse ->
            System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.recoveryId()));

        //To ensure secret is deleted on server side.
        Thread.sleep(30000);

        // If the key vault is soft-delete enabled, then for permanent deletion deleted secrets need to be purged.
        secretAsyncClient.purgeDeletedSecret("BankAccountPassword").subscribe(purgeResponse ->
            System.out.printf("Bank account secret purge status response %d \n", purgeResponse.statusCode()));

        //To ensure secret is purged on server side.
        Thread.sleep(15000);
    }
}
