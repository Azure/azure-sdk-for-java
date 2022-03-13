// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;

/**
 * This sample demonstrates how to fully backup and restore a key vault synchronously.
 */
public class BackupAndRestoreHelloWorld {
    /**
     * Authenticates with the key vault and shows how to fully backup and restore a key vault synchronously.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        /* Instantiate a KeyVaultBackupClient that will be used to call the service. Notice that the client is using
        default Azure credentials. To make default credentials work, ensure that environment variables
        'AZURE_CLIENT_ID', 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.

        To get started, you'll need a URI to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultBackupClient backupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        /* Using the KeyVaultBackupClient, you can back up your entire collection of keys. The backing store for full
        key backups is a blob storage container using Shared Access Signature authentication. For more details on
        creating a SAS token using the BlobServiceClient, see the Azure Storage Blobs client README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/README.md).
        Alternatively, it is possible to generate a SAS token in Storage Explorer (https://docs.microsoft.com/azure/vs-azure-tools-storage-manage-with-storage-explorer?tabs=windows#generate-a-shared-access-signature-in-storage-explorer).

        To ensure you have some keys for backup, you may want to first create a key using the KeyClient. To create a
        new KeyClient to create a key, see the 'Azure Key Vault Key client library for Java' README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md#create-key-client).

        In the sample below, you can set blobStorageUrl and sasToken based on environment variables, configuration
        settings, or any way that works for your application. */
        String blobStorageUrl = "<blob-storage-url>";
        String sasToken = "<sas-token>";
        SyncPoller<KeyVaultBackupOperation, String> backupPoller = backupClient.beginBackup(blobStorageUrl, sasToken);

        backupPoller.waitForCompletion();

        /* Now let's restore the entire collection of keys from the backup. We will need the get the URI for the
        location the backup, as well as Shared Access Signature for accessing it. */
        String backupFolderUrl = backupPoller.getFinalResult();

        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller =
            backupClient.beginRestore(backupFolderUrl, sasToken);

        restorePoller.waitForCompletion();
    }
}
