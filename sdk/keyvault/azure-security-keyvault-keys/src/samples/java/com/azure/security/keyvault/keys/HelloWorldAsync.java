// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.http.rest.Response;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously set, get, update and delete a key.
 */
public class HelloWorldAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously set, get, update and delete a key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {

        // Instantiate an async key client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create Cloud Rsa key valid for 1 year. if the key
        // already exists in the key vault, then a new version of the key is created.
        Response<KeyVaultKey> createKeyResponse = keyAsyncClient.createRsaKeyWithResponse(new CreateRsaKeyOptions("CloudRsaKey")
                                                                                                .setExpiresOn(OffsetDateTime.now().plusYears(1))
                                                                                                .setKeySize(2048)).block();

        // Let's validate create key operation succeeded using the status code information in the response.
        System.out.printf("Create Key operation succeeded with status code %s \n", createKeyResponse.getStatusCode());
        System.out.printf("Key is created with name %s and type %s \n", createKeyResponse.getValue().getName(), createKeyResponse.getValue().getKeyType());

        Thread.sleep(2000);

        // Let's Get the Cloud Rsa Key from the key vault.
        keyAsyncClient.getKey("CloudRsaKey").subscribe(keyResponse ->
                System.out.printf("Key returned with name %s and type %s \n", keyResponse.getName(), keyResponse.getKeyType()));

        Thread.sleep(2000);


        // After one year, the Cloud Rsa Key is still required, we need to update the expiry time of the key.
        // The update method can be used to update the expiry attribute of the key.
        keyAsyncClient.getKey("CloudRsaKey").subscribe(keyResponse -> {
            KeyVaultKey key = keyResponse;
            //Update the expiry time of the key.
            key.getProperties().setExpiresOn(key.getProperties().getExpiresOn().plusYears(1));
            keyAsyncClient.updateKeyProperties(key.getProperties()).subscribe(updatedKeyResponse ->
                System.out.printf("Key's updated expiry time %s \n", updatedKeyResponse.getProperties().getExpiresOn().toString()));
        });

        Thread.sleep(2000);

        // We need the Cloud Rsa key with bigger key size, so you want to update the key in key vault to ensure it has the required size.
        // Calling createRsaKey on an existing key creates a new version of the key in the key vault with the new specified size.
        keyAsyncClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1))
                .setKeySize(4096))
                .subscribe(keyResponse ->
                        System.out.printf("Key is created with name %s and type %s \n", keyResponse.getName(), keyResponse.getKeyType()));

        Thread.sleep(2000);

        // The Cloud Rsa Key is no longer needed, need to delete it from the key vault.
        keyAsyncClient.beginDeleteKey("CloudRsaKey")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Key Name: " + pollResponse.getValue().getName());
                System.out.println("Key Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });

        //To ensure key is deleted on server side.
        Thread.sleep(30000);

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted keys need to be purged.
        keyAsyncClient.purgeDeletedKeyWithResponse("CloudRsaKey").subscribe(purgeResponse ->
                System.out.printf("Cloud Rsa key purge status response %n \n", purgeResponse.getStatusCode()));

        //To ensure key is purged on server side.
        Thread.sleep(15000);
    }
}
