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
        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupClient.instantiation
        KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("https://myaccount.managedhsm.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.administration.keyVaultBackupClient.instantiation

        return keyVaultBackupClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginBackup(String, String)}.
     */
    public void beginBackup() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupClient.beginBackup#String-String
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z"
            + "&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

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
        // END: com.azure.security.keyvault.administration.keyVaultBackupClient.beginBackup#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginRestore(String, String)}.
     */
    public void beginRestore() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupClient.beginRestore#String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z"
            + "&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> backupPoller =
            client.beginRestore(folderUrl, sasToken);

        PollResponse<KeyVaultRestoreOperation> pollResponse = backupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultRestoreOperation> finalPollResponse = backupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Backup restored successfully.%n");
        } else {
            KeyVaultRestoreOperation operation = backupPoller.poll().getValue();

            System.out.printf("Restore failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: com.azure.security.keyvault.administration.keyVaultBackupClient.beginRestore#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupClient#beginSelectiveKeyRestore(String, String, String)}.
     */
    public void beginSelectiveKeyRestore() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupClient.beginSelectiveKeyRestore#String-String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z"
            + "&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";
        String keyName = "myKey";

        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> backupPoller =
            client.beginSelectiveKeyRestore(folderUrl, sasToken, keyName);

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> pollResponse = backupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> finalPollResponse = backupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Key restored successfully.%n");
        } else {
            KeyVaultSelectiveKeyRestoreOperation operation = backupPoller.poll().getValue();

            System.out.printf("Key restore failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: com.azure.security.keyvault.administration.keyVaultBackupClient.beginSelectiveKeyRestore#String-String-String
    }
}
