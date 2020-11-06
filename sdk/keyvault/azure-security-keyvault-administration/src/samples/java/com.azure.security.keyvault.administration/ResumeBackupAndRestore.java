// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;

/**
 * This sample demonstrates how to selectively restore a key from key vault backup synchronously.
 */
public class ResumeBackupAndRestore {
    /**
     * Authenticates with the key vault and shows how to re-hydrate long running synchronous operations such as backup
     * and restore.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        /* Instantiate a KeyVaultAccessControlClient that will be used to call the service. Notice that the client is
        using default Azure credentials. To make default credentials work, ensure that environment variables
        'AZURE_CLIENT_ID', 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.

        To get started, you'll need a URI to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultBackupClient backupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        /* The ID from a started backup operation on one client can be saved to persistent storage instead of waiting
        for completion immediately. */
        String blobStorageUrl = "<blob-storage-url>";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultBackupOperation, String> originalBackupPoller =
            backupClient.beginBackup(blobStorageUrl, sasToken);

        KeyVaultBackupOperation originalBackupOperation = originalBackupPoller.poll().getValue();
        String backupJobId = originalBackupOperation.getJobId();

        /* At some later time, another client can use the persisted operation ID, check the status and retrieve the
        result of a previously started backup operation. */
        SyncPoller<KeyVaultBackupOperation, String> anotherBackupPoller =
            backupClient.beginBackup(backupJobId);

        anotherBackupPoller.waitForCompletion();

        // Get the backup URL to begin a restore operation at some point.
        String backupFolderUrl = anotherBackupPoller.getFinalResult();

        SyncPoller<KeyVaultRestoreOperation, Void> originalRestorePoller =
            backupClient.beginRestore(backupFolderUrl, sasToken);

        KeyVaultRestoreOperation originalRestoreOperation = originalRestorePoller.poll().getValue();
        String restoreJobId = originalRestoreOperation.getJobId();

        /* Similarly to as with backup operations, you can check the status and retrieve the result of a previously
        started restore operation. */
        SyncPoller<KeyVaultRestoreOperation, Void> anotherRestorePoller =
            backupClient.beginRestore(restoreJobId);

        anotherRestorePoller.waitForCompletion();
    }
}
