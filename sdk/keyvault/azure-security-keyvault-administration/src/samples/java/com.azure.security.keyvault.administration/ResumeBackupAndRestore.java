// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;

/**
 * This sample demonstrates how to selectively restore a key from key vault backup.
 */
public class ResumeBackupAndRestore {
    /**
     * Authenticates with the key vault and shows how to selectively restore a key from key vault backup.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        // Instantiate an backup client that will be used to call the service. Notice that the client is using default
        // Azure credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.

        // To get started, you'll need a URI to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-net/blob/master/sdk/keyvault/Azure.Security.KeyVault.Administration/README.md)
        // for links and instructions.
        KeyVaultBackupClient backupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Using the getBackupOperation() method, you can check the status and retrieve the result of a previously
        // started KeyVaultBackupOperation. For example, the ID from a started operation on one client can
        // be saved to persistent storage instead of waiting for completion immediately. At some later time, another
        // client can retrieve the persisted operation ID and check for status or wait for completion.
        SyncPoller<KeyVaultBackupOperation, String> keyVaultBackupOperation =
            backupClient.getBackupOperation("<backup-operation-id>");

        keyVaultBackupOperation.waitForCompletion();

        // Get the backup URI to begin a restore operation at some point.
        String backupUri = keyVaultBackupOperation.getFinalResult();

        // Similarly, you can check the status and retrieve the result of a previously started KeyVaultRestoreOperation.
        SyncPoller<KeyVaultRestoreOperation, Void> keyVaultRestoreOperation =
            backupClient.getRestoreOperation("<restore-operation-id>");

        keyVaultBackupOperation.waitForCompletion();
    }
}
