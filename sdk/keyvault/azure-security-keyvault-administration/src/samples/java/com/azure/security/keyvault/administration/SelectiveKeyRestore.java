// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;

/**
 * This sample demonstrates how to selectively restore a key from key vault backup.
 */
public class SelectiveKeyRestore {
    /**
     * Authenticates with the key vault and shows how to selectively restore a key from key vault backup synchronously.
     * For examples of how to perform async operations, please refer to
     * {@link BackupAndRestoreHelloWorldAsync the async client samples}.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        /* Instantiate an KeyVaultAccessControlClient that will be used to call the service. Notice that the client is
        using default Azure credentials. To make default credentials work, ensure that environment variables
        'AZURE_CLIENT_ID', 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.

        To get started, you'll need a URI to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultBackupClient backupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        /* Using the KeyVaultBackupClient, you can restore a single key from backup by key name. The data source for a
        selective key restore is a storage blob accessed using Shared Access Signature authentication. */
        String keyName = "<key-name>";
        String backupFolderUrl = "<backup-folder-url>";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> restorePoller =
            backupClient.beginSelectiveKeyRestore(keyName, backupFolderUrl, sasToken);

        restorePoller.waitForCompletion();
    }
}
