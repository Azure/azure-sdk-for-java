// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration.codesnippets;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.administration.KeyVaultBackupClient;
import com.azure.v2.security.keyvault.administration.KeyVaultBackupClientBuilder;
import com.azure.v2.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions.HttpLogLevel;

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
        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.instantiation
        KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.instantiation

        return keyVaultBackupClient;
    }

    /**
     * Generates code sample for creating a {@link KeyVaultBackupClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link KeyVaultBackupClient}.
     */
    public KeyVaultBackupClient createClientWithHttpClient() {
        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.instantiation.withHttpClient
        KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpInstrumentationOptions(new HttpInstrumentationOptions().setHttpLogLevel(HttpLogLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.getSharedInstance())
            .buildClient();
        // END: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.instantiation.withHttpClient

        return keyVaultBackupClient;
    }

    /**
     * Generates code samples for using {/@link KeyVaultBackupClient#beginBackup(String, String)}.
     */
    public void beginBackup() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        Poller<KeyVaultBackupOperation, String> backupPoller = client.beginBackup(blobStorageUrl, sasToken);
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
        // END: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String
    }

    /**
     * Generates code samples for using {/@link KeyVaultBackupClient#beginRestore(String, String)}.
     */
    public void beginRestore() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        Poller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller =
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
        // END: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String
    }

    /**
     * Generates code samples for using {/@link KeyVaultBackupClient#beginSelectiveKeyRestore(String, String, String)}.
     */
    public void beginSelectiveKeyRestore() {
        KeyVaultBackupClient client = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";
        String keyName = "myKey";

        Poller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> restorePoller =
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
        // END: com.azure.v2.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String
    }
}
