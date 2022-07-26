// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to asynchronously list, recover and purge deleted keys in a soft-delete enabled key vault.
 */
public class ManagingDeletedKeysAsync {
    /**
     * Authenticates with the key vault and shows how to asynchronously list, recover and purge deleted keys in a
     * soft-delete enabled key vault.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {
        /* NOTE: To manage deleted keys, your key vault needs to have soft-delete enabled. Soft-delete allows deleted
        keys to be retained for a given retention period (90 days). During this period deleted keys can be recovered
        and if a key needs to be permanently deleted then it needs to be purged.*/

        /* Instantiate a KeyAsyncClient that will be used to call the service. Notice that the client is using default
        Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */
        KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Let's create EC and RSA keys valid for 1 year. If the key already exists in the key vault, then a new version
        // of the key is created.
        keyAsyncClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1)))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and type %s %n", keyResponse.getName(),
                    keyResponse.getKeyType()));

        Thread.sleep(2000);

        keyAsyncClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
                .setExpiresOn(OffsetDateTime.now().plusYears(1)))
            .subscribe(keyResponse ->
                System.out.printf("Key is created with name %s and type %s %n", keyResponse.getName(),
                    keyResponse.getKeyType()));

        Thread.sleep(2000);

        // The RSA key is no longer needed, need to delete it from the key vault.
        keyAsyncClient.beginDeleteKey("CloudEcKey")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Key Name: " + pollResponse.getValue().getName());
                System.out.println("Key Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });

        // To ensure the key is deleted server-side.
        Thread.sleep(30000);

        // We accidentally deleted the EC key. Let's recover it.
        // A deleted key can only be recovered if the key vault is soft-delete enabled.
        keyAsyncClient.beginRecoverDeletedKey("CloudEcKey")
            .subscribe(pollResponse -> {
                System.out.println("Recovery Status: " + pollResponse.getStatus().toString());
                System.out.println("Recover Key Name: " + pollResponse.getValue().getName());
                System.out.println("Recover Key Type: " + pollResponse.getValue().getKeyType());
            });

        // To ensure the key is recovered server-side before moving forward.
        Thread.sleep(10000);

        // The EC and RSA keys are no longer needed, need to delete them from the key vault.
        keyAsyncClient.beginDeleteKey("CloudEcKey")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Key Name: " + pollResponse.getValue().getName());
                System.out.println("Key Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });

        keyAsyncClient.beginDeleteKey("CloudRsaKey")
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Delete Key Name: " + pollResponse.getValue().getName());
                System.out.println("Key Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
            });

        // To ensure the key is deleted server-side.
        Thread.sleep(30000);

        // You can list all the deleted and non-purged keys, assuming key vault is soft-delete enabled.
        keyAsyncClient.listDeletedKeys()
            .subscribe(deletedKey ->
                System.out.printf("Deleted key's recovery Id %s %n", deletedKey.getRecoveryId()));

        Thread.sleep(15000);

        // If the keyvault is soft-delete enabled, then for permanent deletion  deleted keys need to be purged.
        keyAsyncClient.purgeDeletedKeyWithResponse("CloudRsaKey")
            .subscribe(purgeResponse ->
                System.out.printf("Storage account key purge status response %d %n", purgeResponse.getStatusCode()));

        keyAsyncClient.purgeDeletedKeyWithResponse("CloudEcKey")
            .subscribe(purgeResponse ->
                System.out.printf("Bank account key purge status response %d %n", purgeResponse.getStatusCode()));

        // To ensure the key is purged server-side.
        Thread.sleep(15000);
    }
}
