// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KeyVaultBackupAsyncClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupAsyncClient asyncClient;

    private void getAsyncClient(HttpClient httpClient, boolean forCleanup) {
        asyncClient = spy(getClientBuilder(httpClient, forCleanup).buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(asyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }
    }

    /**
     * Tests that a Key Vault or MHSM can be backed up.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginBackup(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken).blockLast();

        String backupBlobUri = backupPollResponse.getFinalResult().block();

        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));
    }

    /**
     * Tests that a Key Vault can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginRestore(HttpClient httpClient) {
        getAsyncClient(httpClient, false);

        // Create a backup
        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken)
                .takeUntil(asyncPollResponse ->
                    asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();

        KeyVaultBackupOperation backupOperation = backupPollResponse.getValue();
        assertNotNull(backupOperation);

        // Restore the backup
        String backupFolderUrl = backupOperation.getAzureStorageBlobContainerUrl();
        AsyncPollResponse<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePollResponse =
            asyncClient.beginRestore(backupFolderUrl, sasToken)
                .takeUntil(asyncPollResponse ->
                    asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();

        KeyVaultRestoreOperation restoreOperation = restorePollResponse.getValue();
        assertNotNull(restoreOperation);
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveKeyRestore(HttpClient httpClient) {
        KeyAsyncClient keyClient = new KeyClientBuilder()
            .vaultUrl(getEndpoint())
            .serviceVersion(KeyServiceVersion.V7_2)
            .pipeline(getPipeline(httpClient, false))
            .buildAsyncClient();

        String keyName = testResourceNamer.randomName("backupKey", 20);
        CreateRsaKeyOptions rsaKeyOptions = new CreateRsaKeyOptions(keyName)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC));

        KeyVaultKey createdKey = keyClient.createRsaKey(rsaKeyOptions).block();

        getAsyncClient(httpClient, false);

        // Create a backup
        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken)
                .takeUntil(asyncPollResponse ->
                    asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();

        KeyVaultBackupOperation backupOperation = backupPollResponse.getValue();
        assertNotNull(backupOperation);

        // Restore the backup
        String backupFolderUrl = backupOperation.getAzureStorageBlobContainerUrl();
        AsyncPollResponse<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> restorePollResponse =
            asyncClient.beginSelectiveKeyRestore(createdKey.getName(), backupFolderUrl, sasToken)
                .takeUntil(asyncPollResponse ->
                    asyncPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();

        KeyVaultSelectiveKeyRestoreOperation restoreOperation = restorePollResponse.getValue();
        assertNotNull(restoreOperation);
    }
}
