// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously list, recover and purge deleted keys in a soft-delete enabled key vault.
 */
public class ManagingDeletedKeysAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously list, recover and purge deleted keys in a soft-delete enabled key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {

        // NOTE: To manage deleted keys, your key vault needs to have soft-delete enabled. Soft-delete allows deleted keys
        // to be retained for a given retention period (90 days). During this period deleted keys can be recovered and if
        // a key needs to be permanently deleted then it needs to be purged.

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
                .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildAsyncClient();

        // Let's create Ec and Rsa keys valid for 1 year. if the key
        // already exists in the key vault, then a new version of the key is created.
        keyAsyncClient.createEcKey(new EcKeyCreateOptions("CloudEcKey")
                .expires(OffsetDateTime.now().plusYears(1)))
                .subscribe(keyResponse ->
                    System.out.printf("Key is created with name %s and type %s \n", keyResponse.name(), keyResponse.keyMaterial().kty()));

        Thread.sleep(2000);

        keyAsyncClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
                .expires(OffsetDateTime.now().plusYears(1)))
                .subscribe(keyResponse ->
                    System.out.printf("Key is created with name %s and type %s \n", keyResponse.name(), keyResponse.keyMaterial().kty()));

        Thread.sleep(2000);

        // The Cloud Rsa Key is no longer needed, need to delete it from the key vault.
        keyAsyncClient.deleteKey("CloudEcKey").subscribe(deletedKeyResponse ->
            System.out.printf("Deleted Key's Recovery Id %s \n", deletedKeyResponse.recoveryId()));

        //To ensure key is deleted on server side.
        Thread.sleep(30000);

        // We accidentally deleted Cloud Ec key. Let's recover it.
        // A deleted key can only be recovered if the key vault is soft-delete enabled.
        keyAsyncClient.recoverDeletedKey("CloudEcKey").subscribe(recoveredKeyResponse ->
            System.out.printf("Recovered Key with name %s \n", recoveredKeyResponse.name()));

        //To ensure key is recovered on server side.
        Thread.sleep(10000);

        // The Cloud Ec and Rsa keys are no longer needed, need to delete them from the key vault.
        keyAsyncClient.deleteKey("CloudEcKey").subscribe(deletedKeyResponse ->
            System.out.printf("Deleted Key's Recovery Id %s \n", deletedKeyResponse.recoveryId()));

        keyAsyncClient.deleteKey("CloudRsaKey").subscribe(deletedKeyResponse ->
                System.out.printf("Deleted Key's Recovery Id %s \n", deletedKeyResponse.recoveryId()));

        // To ensure key is deleted on server side.
        Thread.sleep(30000);

        // You can list all the deleted and non-purged keys, assuming key vault is soft-delete enabled.
        keyAsyncClient.listDeletedKeys().subscribe(deletedKey ->
            System.out.printf("Deleted key's recovery Id %s \n", deletedKey.recoveryId()));

        Thread.sleep(15000);

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted keys need to be purged.
        keyAsyncClient.purgeDeletedKey("CloudRsaKey").subscribe(purgeResponse ->
            System.out.printf("Storage account key purge status response %d \n", purgeResponse.statusCode()));

        keyAsyncClient.purgeDeletedKey("CloudEcKey").subscribe(purgeResponse ->
            System.out.printf("Bank account key purge status response %d \n", purgeResponse.statusCode()));

        // To ensure key is purged on server side.
        Thread.sleep(15000);
    }
}
