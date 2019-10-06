// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.EcKeyCreateOptions;
import com.azure.security.keyvault.keys.models.RsaKeyCreateOptions;

import java.time.OffsetDateTime;

/**
 * Sample demonstrates how to list, recover and purge deleted keys in a soft-delete setEnabled key vault.
 */
public class ManagingDeletedKeys {
    /**
     * Authenticates with the key vault and shows how to list, recover and purge deleted keys in a soft-delete setEnabled key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws IllegalArgumentException, InterruptedException {

        // NOTE: To manage deleted keys, your key vault needs to have soft-delete setEnabled. Soft-delete allows deleted keys
        // to be retained for a given retention period (90 days). During this period deleted keys can be recovered and if
        // a key needs to be permanently deleted then it needs to be purged.

        // Instantiate a client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        KeyClient keyClient = new KeyClientBuilder()
                .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        // Let's create Ec and Rsa keys valid for 1 year. if the key
        // already exists in the key vault, then a new getVersion of the key is getCreated.
        keyClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
                .setExpires(OffsetDateTime.now().plusYears(1))
                .setKeySize(2048));

        keyClient.createEcKey(new EcKeyCreateOptions("CloudEcKey")
                .setExpires(OffsetDateTime.now().plusYears(1)));

        // The Cloud Rsa Key is no longer needed, need to delete it from the key vault.
        keyClient.deleteKey("CloudEcKey");

        //To ensure key is deleted on server side.
        Thread.sleep(30000);

        // We accidentally Cloud Ec key. Let's recover it.
        // A deleted key can only be recovered if the key vault is soft-delete setEnabled.
        keyClient.recoverDeletedKey("CloudEcKey");

        //To ensure key is recovered on server side.
        Thread.sleep(30000);

        // The Cloud Ec and Rsa keys are no longer needed, need to delete them from the key vault.
        keyClient.deleteKey("CloudEcKey");
        keyClient.deleteKey("CloudRsaKey");

        //To ensure key is deleted on server side.
        Thread.sleep(30000);

        // You can list all the deleted and non-purged keys, assuming key vault is soft-delete setEnabled.
        for (DeletedKey deletedKey : keyClient.listDeletedKeys()) {
            System.out.printf("Deleted key's recovery Id %s", deletedKey.getRecoveryId());
        }

        // If the keyvault is soft-delete setEnabled, then for permanent deletion deleted keys need to be purged.
        keyClient.purgeDeletedKey("CloudEcKey");
        keyClient.purgeDeletedKey("CloudRsaKey");

        //To ensure key is purged on server side.
        Thread.sleep(15000);
    }
}
