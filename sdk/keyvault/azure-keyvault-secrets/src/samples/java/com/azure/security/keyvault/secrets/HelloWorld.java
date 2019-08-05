// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;

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
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create a secret holding bank account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretClient.setSecret(new Secret("BankAccountPassword", "f4G34fMh8v")
            .expires(OffsetDateTime.now().plusYears(1)));

        // Let's Get the bank secret from the key vault.
        Secret bankSecret = secretClient.getSecret("BankAccountPassword");
        System.out.printf("Secret is returned with name %s and value %s \n", bankSecret.name(), bankSecret.value());

        // After one year, the bank account is still active, we need to update the expiry time of the secret.
        // The update method can be used to update the expiry attribute of the secret. It cannot be used to update
        // the value of the secret.
        bankSecret.expires(bankSecret.expires().plusYears(1));
        SecretBase updatedSecret = secretClient.updateSecret(bankSecret);
        System.out.printf("Secret's updated expiry time %s \n", updatedSecret.expires());

        // Bank forced a password update for security purposes. Let's change the value of the secret in the key vault.
        // To achieve this, we need to create a new version of the secret in the key vault. The update operation cannot
        // change the value of the secret.
        secretClient.setSecret(new Secret("BankAccountPassword", "bhjd4DDgsa")
            .expires(OffsetDateTime.now().plusYears(1)));

        // The bank account was closed, need to delete its credentials from the key vault.
        secretClient.deleteSecret("BankAccountPassword");

        // To ensure secret is deleted on server side.
        Thread.sleep(30000);

        // If the key vault is soft-delete enabled, then for permanent deletion  deleted secrets need to be purged.
        secretClient.purgeDeletedSecret("BankAccountPassword");
    }
}
