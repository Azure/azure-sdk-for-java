// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.keyvault.SecretAsyncClient;
import com.azure.keyvault.SecretClient;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to set, get, and delete a secret.
 */
public class HelloWorld {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretClient client = SecretClient.builder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            //.credentials(AzureCredential.DEFAULT)  TODO: Enable this, once Azure Identity Library merges.
            .build();

        // Let's create a secret holding bank account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        client.setSecret(new Secret("BANK_ACCOUNT_PASSWORD", "f4G34fMh8v")
            .expires(OffsetDateTime.now().plusYears(1)));

        // Let's Get the bank secret from the key vault.
        Secret bankSecret = client.getSecret("BANK_ACCOUNT_PASSWORD").value();

        // After one year, the bank account is still active, we need to update the expiry time of the secret.
        // The update method can be used to update the expiry attribute of the secret. It cannot be used to update
        // the value of the secret.
        bankSecret.expires(bankSecret.expires().plusYears(1));
        client.updateSecret(bankSecret);

        // Bank forced a password update for security purposes. Let's change the value of the secret in the key vault.
        // To achieve this, we need to create a new version of the secret in the key vault. The update operation cannot
        // change the value of the secret.
        client.setSecret(new Secret("BANK_ACCOUNT_PASSWORD", "bhjd4DDgsa")
            .expires(OffsetDateTime.now().plusYears(1)));

        // The bank account was closed, need to delete its credentials from the key vault.
        client.deleteSecret("BANK_ACCOUNT_PASSWORD");


    }
}
