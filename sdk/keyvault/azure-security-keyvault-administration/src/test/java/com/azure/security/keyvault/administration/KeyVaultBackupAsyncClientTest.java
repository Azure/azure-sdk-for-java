// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KeyVaultBackupAsyncClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupAsyncClient asyncClient;

    private final String blobStorageUrl = "https://testaccount.blob.core.windows.net/backup";
    private final String sasToken = "someSasToken";

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void createAsyncClient(HttpClient httpClient, boolean forCleanup) {
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
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        createAsyncClient(httpClient, false);

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
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        createAsyncClient(httpClient, false);

        // Create a backup
        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken).blockLast();

        // Restore the backup
        String backupFolderUrl = backupPollResponse.getFinalResult().block();

        AsyncPollResponse<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePollResponse =
            asyncClient.beginRestore(backupFolderUrl, sasToken).blockLast();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, restorePollResponse.getStatus());
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveKeyRestore(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        createAsyncClient(httpClient, false);

        // Create a backup
        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken).blockLast();

        // Restore the backup
        String backupFolderUrl = backupPollResponse.getFinalResult().block();
        AsyncPollResponse<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> selectiveKeyRestorePollResponse =
            asyncClient.beginSelectiveKeyRestore("testKey", backupFolderUrl, sasToken).blockLast();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, selectiveKeyRestorePollResponse.getStatus());
    }
}
