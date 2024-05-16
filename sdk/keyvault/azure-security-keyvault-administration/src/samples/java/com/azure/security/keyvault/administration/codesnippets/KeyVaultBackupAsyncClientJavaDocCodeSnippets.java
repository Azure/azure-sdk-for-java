// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
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
        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.instantiation
        KeyVaultBackupAsyncClient keyVaultBackupAsyncClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.instantiation

        return keyVaultBackupAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link KeyVaultBackupAsyncClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link KeyVaultBackupAsyncClient}.
     */
    public KeyVaultBackupAsyncClient createAsyncClientWithHttpClient() {
        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.instantiation.withHttpClient
        KeyVaultBackupAsyncClient keyVaultBackupAsyncClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.instantiation.withHttpClient
        return keyVaultBackupAsyncClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupAsyncClient#beginPreBackup(String, String)}.
     */
    public void beginPreBackup() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreBackup#String-String
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        client.beginPreBackup(blobStorageUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Pre-backup check failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Pre-backup check completed successfully.%n"));
        // END: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreBackup#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupAsyncClient#beginBackup(String, String)}.
     */
    public void beginBackup() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginBackup#String-String
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        client.beginBackup(blobStorageUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Backup failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(folderUrl ->
                System.out.printf("Backup completed. The storage location of this backup is: %s.%n", folderUrl));
        // END: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginBackup#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupAsyncClient#beginPreRestore(String, String)}.
     */
    public void beginPreRestore() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreRestore#String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        client.beginPreRestore(folderUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Pre-restore check failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Pre-restore check completed successfully.%n"));
        // END: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginPreRestore#String-String
    }

    /**
     * Generates code samples for using {@link KeyVaultBackupAsyncClient#beginRestore(String, String)}.
     */
    public void beginRestore() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginRestore#String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        client.beginRestore(folderUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Restore failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Backup restored successfully.%n"));
        // END: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginRestore#String-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultBackupAsyncClient#beginSelectiveKeyRestore(String, String, String)}.
     */
    public void beginSelectiveKeyRestore() {
        KeyVaultBackupAsyncClient client = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";
        String keyName = "myKey";

        client.beginSelectiveKeyRestore(folderUrl, sasToken, keyName)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Key restoration failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Key restored successfully.%n"));
        // END: com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient.beginSelectiveKeyRestore#String-String-String
    }
}
