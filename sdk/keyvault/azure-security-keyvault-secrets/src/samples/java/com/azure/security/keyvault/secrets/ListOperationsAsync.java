// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously list secrets and versions of a given secret in the key vault.
 */
public class ListOperationsAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously list secrets and list versions of a specific secret in the key vault.
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
                .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new KeyVaultSecret("BankAccountPassword", "f4G34fMh8v")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))))
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n", secretResponse.getName(), secretResponse.getValue()));

        Thread.sleep(2000);

        secretAsyncClient.setSecret(new KeyVaultSecret("StorageAccountPassword", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))))
            .subscribe(secretResponse ->
                    System.out.printf("Secret is created with name %s and value %s \n", secretResponse.getName(), secretResponse.getValue()));

        Thread.sleep(2000);

        // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
        // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
        secretAsyncClient.listPropertiesOfSecrets()
            .subscribe(secret ->
                secretAsyncClient.getSecret(secret.getName(), secret.getVersion()).subscribe(secretResponse ->
                    System.out.printf("Received secret with name %s and value %s \n", secretResponse.getName(), secretResponse.getValue())));

        Thread.sleep(15000);

        // The bank account password got updated, so you want to update the secret in key vault to ensure it reflects the new password.
        // Calling setSecret on an existing secret creates a new version of the secret in the key vault with the new value.
        secretAsyncClient.setSecret(new KeyVaultSecret("BankAccountPassword", "sskdjfsdasdjsd")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1)))).subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n", secretResponse.getName(), secretResponse.getValue()));

        Thread.sleep(2000);

        // You need to check all the different values your bank account password secret had previously. Lets print all the versions of this secret.
        secretAsyncClient.listPropertiesOfSecretVersions("BankAccountPassword").subscribe(secret ->
            secretAsyncClient.getSecret(secret.getName(), secret.getVersion()).subscribe(secretResponse ->
                System.out.printf("Received secret's version with name %s and value %s \n", secretResponse.getName(), secretResponse.getValue())));

        Thread.sleep(15000);
    }
}
