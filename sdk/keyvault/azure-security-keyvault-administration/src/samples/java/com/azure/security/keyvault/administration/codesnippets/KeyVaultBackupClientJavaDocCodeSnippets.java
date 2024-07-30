// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultBackupClient;
import com.azure.security.keyvault.administration.KeyVaultBackupClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultBackupClient}.
 */
public class KeyVaultBackupClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultBackupClient}.
     *
     * @return An instance of {@link KeyVaultBackupClient}.
     */
    public KeyVaultBackupClient createClient() {
        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupClient.instantiation
        KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.administration.KeyVaultBackupClient.instantiation

        return keyVaultBackupClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginPreBackup(String, String)}.
     */
    public void beginPreBackup() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginPreBackup#String-String
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultBackupOperation, String> preBackupPoller = client.beginPreBackup(blobStorageUrl, sasToken);
        PollResponse<KeyVaultBackupOperation> pollResponse = preBackupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultBackupOperation> finalPollResponse = preBackupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Pre-backup check completed successfully.%n");
        } else {
            KeyVaultBackupOperation operation = preBackupPoller.poll().getValue();

            System.out.printf("Pre-backup check failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginPreBackup#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginBackup(String, String)}.
     */
    public void beginBackup() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultBackupOperation, String> backupPoller = client.beginBackup(blobStorageUrl, sasToken);
        PollResponse<KeyVaultBackupOperation> pollResponse = backupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultBackupOperation> finalPollResponse = backupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            String folderUrl = backupPoller.getFinalResult();

            System.out.printf("Backup completed. The storage location of this backup is: %s.%n", folderUrl);
        } else {
            KeyVaultBackupOperation operation = backupPoller.poll().getValue();

            System.out.printf("Backup failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginPreRestore(String, String)}.
     */
    public void beginPreRestore() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginPreRestore#String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> preRestorePoller =
            client.beginPreRestore(folderUrl, sasToken);
        PollResponse<KeyVaultRestoreOperation> pollResponse = preRestorePoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultRestoreOperation> finalPollResponse = preRestorePoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Pre-restore check completed successfully.%n");
        } else {
            KeyVaultRestoreOperation operation = preRestorePoller.poll().getValue();

            System.out.printf("Pre-restore check failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginPreRestore#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginRestore(String, String)}.
     */
    public void beginRestore() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller =
            client.beginRestore(folderUrl, sasToken);
        PollResponse<KeyVaultRestoreOperation> pollResponse = restorePoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultRestoreOperation> finalPollResponse = restorePoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Backup restored successfully.%n");
        } else {
            KeyVaultRestoreOperation operation = restorePoller.poll().getValue();

            System.out.printf("Restore failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginSelectiveKeyRestore(String, String, String)}.
     */
    public void beginSelectiveKeyRestore() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";
        String keyName = "myKey";

        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> restorePoller =
            client.beginSelectiveKeyRestore(folderUrl, sasToken, keyName);
        PollResponse<KeyVaultSelectiveKeyRestoreOperation> pollResponse = restorePoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> finalPollResponse = restorePoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Key restored successfully.%n");
        } else {
            KeyVaultSelectiveKeyRestoreOperation operation = restorePoller.poll().getValue();

            System.out.printf("Key restore failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String
    }
}
