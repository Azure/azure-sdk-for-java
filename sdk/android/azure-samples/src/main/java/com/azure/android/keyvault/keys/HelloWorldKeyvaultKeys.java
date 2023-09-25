// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.android.keyvault.keys;

import android.util.Log;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class HelloWorldKeyvaultKeys {
    /**
     * Authenticates with the key vault and shows how to set, get, update and delete a key in the key vault.
     *
     * @param key Unused. Arguments to the program.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */

    private static final String TAG = "KeyvaultHelloWorld";

    public static void main(String endpoint, ClientSecretCredential clientSecretCredential) throws InterruptedException, IllegalArgumentException {
        /* Instantiate a KeyClient that will be used to call the service. Notice that the client is using default Azure
        credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */

        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl(endpoint)
            .credential(clientSecretCredential)
            .buildClient();

        // Let's create an RSA key valid for 1 year. If the key already exists in the key vault, then a new version of
        // the key is created.
        Response<KeyVaultKey> createKeyResponse =
            keyClient.createRsaKeyWithResponse(new CreateRsaKeyOptions("HelloCloudRsaKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1))
                .setKeySize(2048), new Context("key1", "value1"));

        // Let's validate the create key operation succeeded using the status code information in the response.
        Log.i(TAG, String.format("Create Key operation succeeded with status code %s \n", createKeyResponse.getStatusCode()));

        // Let's get the RSA key from the key vault.
        KeyVaultKey cloudRsaKey = keyClient.getKey("HelloCloudRsaKey");

        Log.i(TAG, String.format("Key is returned with name %s and type %s \n", cloudRsaKey.getName(),
            cloudRsaKey.getKeyType()));

        // After one year, the RSA key is still required, we need to update the expiry time of the key.
        // The update method can be used to update the expiry attribute of the key.
        cloudRsaKey.getProperties().setExpiresOn(cloudRsaKey.getProperties().getExpiresOn().plusYears(1));

        KeyVaultKey updatedKey = keyClient.updateKeyProperties(cloudRsaKey.getProperties());

        Log.i(TAG, String.format("Key's updated expiry time %s \n", updatedKey.getProperties().getExpiresOn()));

        // We need the RSA key with bigger key size, so you want to update the key in key vault to ensure it has the
        // required size. Calling createRsaKey() on an existing key creates a new version of the key in the key vault
        // with the new specified size.
        keyClient.createRsaKey(new CreateRsaKeyOptions("HelloCloudRsaKey")
            .setExpiresOn(OffsetDateTime.now().plusYears(1))
            .setKeySize(4096));

        // The RSA key is no longer needed, need to delete it from the key vault.
        SyncPoller<DeletedKey, Void> rsaDeletedKeyPoller = keyClient.beginDeleteKey("HelloCloudRsaKey");
        PollResponse<DeletedKey> pollResponse = rsaDeletedKeyPoller.poll();
        DeletedKey rsaDeletedKey = pollResponse.getValue();

        Log.i(TAG, String.format("Deleted Date  %s", rsaDeletedKey.getDeletedOn().toString()));
        Log.i(TAG, String.format("Deleted Key's Recovery Id %s", rsaDeletedKey.getRecoveryId()));

        // The key is being deleted on the server.
        rsaDeletedKeyPoller.waitForCompletion();

        // To ensure the key is deleted server-side.
        Thread.sleep(30000);

        // If the keyvault is soft-delete enabled, then deleted keys need to be purged for permanent deletion.
        keyClient.purgeDeletedKey("HelloCloudRsaKey");
        Log.i(TAG, "HelloCloudRsaKey purged from vault");
    }
}
