// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.keyvault.SecretAsyncClient;
import com.azure.keyvault.SecretClient;
import com.azure.keyvault.models.Secret;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to set, get, and delete a secret.
 */
public class HelloWorldAsync {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException {

        // Instantiate an async secret client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        SecretAsyncClient secretAsyncClient = SecretAsyncClient.builder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            //.credentials(AzureCredential.DEFAULT)  TODO: Enable this, once Azure Identity Library merges.
            .build();

        // Let's create a secret holding bank account credentials valid for 1 year. if the secret
        // already exists in the key vault, then a new version of the secret is created.
        secretAsyncClient.setSecret(new Secret("BANK_ACCOUNT_PASSWORD", "f4G34fMh8v")
         .expires(OffsetDateTime.now().plusYears(1))).subscribe(secretResponse ->
             System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        // Let's Get the bank secret from the key vault.
        secretAsyncClient.getSecret("BANK_ACCOUNT_PASSWORD").subscribe(secretResponse ->
                System.out.printf("Secret returned with name %s , value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        // After one year, the bank account is still active, we need to update the expiry time of the secret.
        // The update method can be used to update the expiry attribute of the secret. It cannot be used to update
        // the value of the secret.
        secretAsyncClient.getSecret("BANK_ACCOUNT_PASSWORD").subscribe(secretResponse -> {
          Secret secret = secretResponse.value();
          //Update the expiry time of the secret.
          secret.expires(secret.expires().plusYears(1));
          secretAsyncClient.updateSecret(secret).subscribe(updatedSecretResponse ->
              System.out.printf("Secret's updated expiry time %s \n", updatedSecretResponse.value().expires().toString()));
        });

        // Bank forced a password update for security purposes. Let's change the value of the secret in the key vault.
        // To achieve this, we need to create a new version of the secret in the key vault. The update operation cannot
        // change the value of the secret.
        secretAsyncClient.setSecret("BANK_ACCOUNT_PASSWORD", "bhjd4DDgsa").subscribe(secretResponse ->
          System.out.printf("Secret is created with name %s and value %s \n", secretResponse.value().name(), secretResponse.value().value()));

        // The bank account was closed, need to delete its credentials from the key vault.
        secretAsyncClient.deleteSecret("BANK_ACCOUNT_PASSWORD").subscribe(deletedSecretResponse ->
          System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));

        //To ensure async call completes and secret is deleted on server side.
        Thread.sleep(4000);

        // If the keyvault is soft-delete enabled, then for permanent deletion deleted secret needs to be purged.
        //secretAsyncClient.purgeDeletedSecret("deletedSecretName").subscribe(purgeResponse ->
        //System.out.printf("Purge Status response %d \n", purgeResponse.statusCode()));
    }
}
