// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list secrets and versions of a given secret in the key vault.
 */
public class ListOperations {
    /**
     * Authenticates with the key vault and shows how to list secrets and list versions of a specific secret in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException {

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = new SecretClientBuilder()
                .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new Secret("StorageAccountPassword", "f4G34fMh8v-fdsgjsk2323=-asdsdfsdf")
                .expires(OffsetDateTime.now().plusYears(1)));

        client.setSecret(new Secret("BankAccountPassword", "f4G34fMh8v")
                .expires(OffsetDateTime.now().plusYears(1)));

        // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
        // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
        for (SecretBase secret : client.listSecrets()) {
            Secret secretWithValue  = client.getSecret(secret);
            System.out.printf("Received secret with name %s and value %s \n", secretWithValue.name(), secretWithValue.value());
        }

        // The bank account password got updated, so you want to update the secret in key vault to ensure it reflects the new password.
        // Calling setSecret on an existing secret creates a new version of the secret in the key vault with the new value.
        client.setSecret("BankAccountPassword", "sskdjfsdasdjsd");

        // You need to check all the different values your bank account password secret had previously. Lets print all the versions of this secret.
        for (SecretBase secret : client.listSecretVersions("BankAccountPassword")) {
            Secret secretWithValue  = client.getSecret(secret);
            System.out.printf("Received secret's version with name %s and value %s", secretWithValue.name(), secretWithValue.value());
        }
    }
}
