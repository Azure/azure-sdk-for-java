// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list, recover and purge deleted keys in a soft-delete enabled key vault.
 */
public class ManagingDeletedKeys {
    /**
     * Authenticates with the key vault and shows how to list, recover and purge deleted keys in a soft-delete enabled
     * key vault.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {
        /* NOTE: To manage deleted keys, your key vault needs to have soft-delete enabled. Soft-delete allows deleted
        keys to be retained for a given retention period (90 days). During this period deleted keys can be recovered
        and if a key needs to be permanently deleted then it needs to be purged.*/

        /* Instantiate a KeyClient that will be used to call the service. Notice that the client is using default Azure
        credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
        for links and instructions. */
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create EC and RSA keys valid for 1 year. If the key already exists in the key vault, then a new version
        // of the key is created.
        keyClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
            .setExpiresOn(OffsetDateTime.now().plusYears(1))
            .setKeySize(2048));

        keyClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
            .setExpiresOn(OffsetDateTime.now().plusYears(1)));

        // The RSA key is no longer needed, need to delete it from the key vault.
        SyncPoller<DeletedKey, Void> deletedKeyPoller = keyClient.beginDeleteKey("CloudEcKey");
        PollResponse<DeletedKey> deletedKeyPollResponse = deletedKeyPoller.poll();
        DeletedKey deletedKey = deletedKeyPollResponse.getValue();

        System.out.println("Deleted Date  %s" + deletedKey.getDeletedOn().toString());
        System.out.printf("Deleted Key's Recovery Id %s", deletedKey.getRecoveryId());

        // The key is being deleted on the server.
        deletedKeyPoller.waitForCompletion();

        // We accidentally deleted the EC key. Let's recover it.
        // A deleted key can only be recovered if the key vault is soft-delete enabled.
        SyncPoller<KeyVaultKey, Void> recoverEcKeyPoller = keyClient.beginRecoverDeletedKey("CloudEcKey");
        PollResponse<KeyVaultKey> recoveryEcKeyPollResponse = recoverEcKeyPoller.poll();
        KeyVaultKey recoveredKey = recoveryEcKeyPollResponse.getValue();

        System.out.println("Recovered Key Name %s" + recoveredKey.getName());
        System.out.printf("Recovered Key's Id %s", recoveredKey.getId());

        // The key is being recovered on the server.
        recoverEcKeyPoller.waitForCompletion();

        // The EC and RSA keys are no longer needed, need to delete them from the key vault.
        SyncPoller<DeletedKey, Void> ecDeletedKeyPoller = keyClient.beginDeleteKey("CloudEcKey");
        PollResponse<DeletedKey> ecDeletedKeyPollResponse = ecDeletedKeyPoller.poll();
        DeletedKey ecDeletedKey = ecDeletedKeyPollResponse.getValue();

        System.out.println("Deleted Date  %s" + ecDeletedKey.getDeletedOn().toString());
        System.out.printf("Deleted Key's Recovery Id %s", ecDeletedKey.getRecoveryId());

        // The key is being deleted on the server.
        ecDeletedKeyPoller.waitForCompletion();

        SyncPoller<DeletedKey, Void> rsaDeletedKeyPoller = keyClient.beginDeleteKey("CloudRsaKey");
        PollResponse<DeletedKey> rsaDeletedKeyPollResponse = rsaDeletedKeyPoller.poll();
        DeletedKey rsaDeletedKey = rsaDeletedKeyPollResponse.getValue();

        System.out.println("Deleted Date  %s" + rsaDeletedKey.getDeletedOn().toString());
        System.out.printf("Deleted Key's Recovery Id %s", rsaDeletedKey.getRecoveryId());

        // The key is being deleted on the server.
        rsaDeletedKeyPoller.waitForCompletion();

        // You can list all the deleted and non-purged keys, assuming key vault is soft-delete enabled.
        for (DeletedKey delKey : keyClient.listDeletedKeys()) {
            System.out.printf("Deleted key's recovery Id %s", delKey.getRecoveryId());
        }

        // If the keyvault is soft-delete enabled, then for permanent deletion deleted keys need to be purged.
        keyClient.purgeDeletedKey("CloudEcKey");
        keyClient.purgeDeletedKey("CloudRsaKey");

        // To ensure the key is purged server-side.
        Thread.sleep(15000);
    }
}
