// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list secrets and versions of a given secret in the key vault.
 */
public class ListOperations {
    /**
     * Authenticates with the key vault and shows how to list secrets and list versions of a specific secret in the key
     * vault.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        /* Instantiate a SecretClient that will be used to call the service. Notice that the client is using default
        Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets/README.md)
        for links and instructions. */
        SecretClient client = new SecretClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create secrets holding storage and bank accounts credentials valid for 1 year. If the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new KeyVaultSecret("StorageAccountPassword", "fakePasswordPlaceholder")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        client.setSecret(new KeyVaultSecret("BankAccountPassword", "fakePasswordPlaceholder")
            .setProperties(new SecretProperties()
                .setExpiresOn(OffsetDateTime.now().plusYears(1))));

        // You need to check if any of the secrets are sharing same values. Let's list the secrets and print their values.
        // List operations don't return the secrets with value information. So, for each returned secret we call getSecret to get the secret with its value information.
        for (SecretProperties secret : client.listPropertiesOfSecrets()) {
            if (!secret.isEnabled()) {
                continue;
            }
            KeyVaultSecret secretWithValue = client.getSecret(secret.getName(), secret.getVersion());
            System.out.printf("Received secret with name %s and value %s \n", secretWithValue.getName(), secretWithValue.getValue());
        }

        // The bank account password got updated, so you want to update the secret in key vault to ensure it reflects the new password.
        // Calling setSecret on an existing secret creates a new version of the secret in the key vault with the new value.
        client.setSecret("BankAccountPassword", "fakePasswordPlaceholder");

        // You need to check all the different values your bank account password secret had previously. Lets print all the versions of this secret.
        for (SecretProperties secret : client.listPropertiesOfSecretVersions("BankAccountPassword")) {
            KeyVaultSecret secretWithValue = client.getSecret(secret.getName(), secret.getVersion());

            System.out.printf("Received secret's version with name %s and value %s", secretWithValue.getName(),
                secretWithValue.getValue());
        }
    }
}
