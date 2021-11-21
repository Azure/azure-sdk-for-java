// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultBackupClientBuilder;

import java.time.Duration;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultBackupAsyncClient}.
 */
public class KeyVaultBackupAsyncClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultBackupAsyncClient}.
     *
     * @return An instance of {@link KeyVaultBackupAsyncClient}.
     */
    public KeyVaultBackupAsyncClient createAsyncClient() {
        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.instantiation
        KeyVaultBackupAsyncClient keyVaultBackupAsyncClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("https://myaccount.managedhsm.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.instantiation

        return keyVaultBackupAsyncClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupAsyncClient#beginBackup(String, String)}.
     */
    public void beginBackup() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.beginBackup#String-String
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z"
            + "&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

        client.beginBackup(blobStorageUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Backup failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(folderUrl ->
                System.out.printf("Backup completed. The storage location of this backup is: %s.%n", folderUrl));
        // END: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.beginBackup#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupAsyncClient#beginRestore(String, String)}.
     */
    public void beginRestore() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.beginRestore#String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z"
            + "&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

        client.beginRestore(folderUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Restore failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Backup restored successfully.%n"));
        // END: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.beginRestore#String-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultBackupAsyncClient#beginSelectiveKeyRestore(String, String, String)}.
     */
    public void beginSelectiveKeyRestore() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z"
            + "&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";
        String keyName = "myKey";

        client.beginSelectiveKeyRestore(folderUrl, sasToken, keyName)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Key restoration failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Key restored successfully.%n"));
        // END: com.azure.security.keyvault.administration.keyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String
    }
}
