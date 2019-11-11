// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class HelloWorld {

    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a key in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException, IllegalArgumentException {

        // Instantiate a key client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyClient keyClient = new KeyClientBuilder()
                .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // Let's create a Rsa key valid for 1 year. if the key
        // already exists in the key vault, then a new version of the key is created.
        Response<KeyVaultKey> createKeyResponse = keyClient.createRsaKeyWithResponse(new CreateRsaKeyOptions("CloudRsaKey")
                                                                                 .setExpiresOn(OffsetDateTime.now().plusYears(1))
                                                                                 .setKeySize(2048), new Context("key1", "value1"));

        // Let's validate create key operation succeeded using the status code information in the response.
        System.out.printf("Create Key operation succeeded with status code %s \n", createKeyResponse.getStatusCode());

        // Let's Get the Cloud Rsa Key from the key vault.
        KeyVaultKey cloudRsaKey = keyClient.getKey("CloudRsaKey");
        System.out.printf("Key is returned with name %s and type %s \n", cloudRsaKey.getName(),
            cloudRsaKey.getKeyType());

        // After one year, the Cloud Rsa Key is still required, we need to update the expiry time of the key.
        // The update method can be used to update the expiry attribute of the key.
        cloudRsaKey.getProperties().setExpiresOn(cloudRsaKey.getProperties().getExpiresOn().plusYears(1));
        KeyVaultKey updatedKey = keyClient.updateKeyProperties(cloudRsaKey.getProperties());
        System.out.printf("Key's updated expiry time %s \n", updatedKey.getProperties().getExpiresOn());

        // We need the Cloud Rsa key with bigger key size, so you want to update the key in key vault to ensure it has the required size.
        // Calling createRsaKey on an existing key creates a new version of the key in the key vault with the new specified size.
        keyClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1))
                .setKeySize(4096));

        // The Cloud Rsa Key is no longer needed, need to delete it from the key vault.
        SyncPoller<DeletedKey, Void> rsaDeletedKeyPoller = keyClient.beginDeleteKey("CloudRsaKey");

        PollResponse<DeletedKey> pollResponse = rsaDeletedKeyPoller.poll();

        DeletedKey rsaDeletedKey = pollResponse.getValue();
        System.out.println("Deleted Date  %s" + rsaDeletedKey.getDeletedOn().toString());
        System.out.printf("Deleted Key's Recovery Id %s", rsaDeletedKey.getRecoveryId());

        // Key is being deleted on server.
        rsaDeletedKeyPoller.waitForCompletion();

        // To ensure key is deleted on server side.
        Thread.sleep(30000);

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted keys need to be purged.
        keyClient.purgeDeletedKey("CloudRsaKey");
    }
}
