// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class KeyVaultBackupAsyncClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupAsyncClient asyncClient;

    private final String blobStorageUrl = "https://testaccount.blob.core.windows.net/backup";
    private final String sasToken = "someSasToken";

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    /**
     * Tests that a Key Vault can be backed up.
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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken).blockLast();

        String backupBlobUri = backupPollResponse.getFinalResult().block();

        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));
    }

    /**
     * Tests that a backup operation can be obtained by using its {@code jobId}.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getBackupStatus(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        PollerFlux<KeyVaultBackupOperation, String> backupPollerFlux =
            asyncClient.beginBackup(blobStorageUrl, sasToken);
        String jobId = backupPollerFlux.blockFirst().getValue().getJobId();

        PollerFlux<KeyVaultBackupOperation, String> backupStatusPollerFlux = asyncClient.getBackupOperation(jobId);

        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse = backupPollerFlux.blockLast();
        AsyncPollResponse<KeyVaultBackupOperation, String> backupStatusPollResponse =
            backupStatusPollerFlux.blockLast();

        KeyVaultBackupOperation backupOperation = backupPollResponse.getValue();
        KeyVaultBackupOperation backupStatusOperation = backupStatusPollResponse.getValue();

        String backupBlobUri = backupPollResponse.getFinalResult().block();
        String backupStatusBlobUri = backupStatusPollResponse.getFinalResult().block();

        assertEquals(backupBlobUri, backupStatusBlobUri);
        assertEquals(backupOperation.getStartTime(), backupStatusOperation.getStartTime());
        assertEquals(backupOperation.getEndTime(), backupStatusOperation.getEndTime());
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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        // Create a backup
        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken).blockLast();

        // Restore the backup
        String backupBlobUri = backupPollResponse.getFinalResult().block();
        String[] segments = backupBlobUri.split("/");
        String folderName = segments[segments.length - 1];

        AsyncPollResponse<KeyVaultRestoreOperation, Void> restorePollResponse =
            asyncClient.beginRestore(blobStorageUrl, sasToken, folderName).blockLast();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, restorePollResponse.getStatus());
    }

    /**
     * Tests that a restore operation can be obtained by using its {@code jobId}.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRestoreStatus(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        // Create a backup
        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken).blockLast();

        // Restore the backup
        String backupBlobUri = backupPollResponse.getFinalResult().block();
        String[] segments = backupBlobUri.split("/");
        String folderName = segments[segments.length - 1];

        PollerFlux<KeyVaultRestoreOperation, Void> restorePollerFlux =
            asyncClient.beginRestore(blobStorageUrl, sasToken, folderName);
        String jobId = restorePollerFlux.blockFirst().getValue().getJobId();

        PollerFlux<KeyVaultRestoreOperation, Void> restoreStatusPollerFlux = asyncClient.getRestoreOperation(jobId);

        AsyncPollResponse<KeyVaultRestoreOperation, Void> restorePollResponse = restorePollerFlux.blockLast();
        AsyncPollResponse<KeyVaultRestoreOperation, Void> restoreStatusPollResponse =
            restoreStatusPollerFlux.blockLast();

        KeyVaultRestoreOperation backupOperation = restorePollResponse.getValue();
        KeyVaultRestoreOperation backupStatusOperation = restoreStatusPollResponse.getValue();

        assertEquals(backupOperation.getStartTime(), backupStatusOperation.getStartTime());
        assertEquals(backupOperation.getEndTime(), backupStatusOperation.getEndTime());
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @SuppressWarnings("ConstantConditions")
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveRestore(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        // Create a backup
        AsyncPollResponse<KeyVaultBackupOperation, String> backupPollResponse =
            asyncClient.beginBackup(blobStorageUrl, sasToken).blockLast();

        // Restore the backup
        String backupBlobUri = backupPollResponse.getFinalResult().block();
        String[] segments = backupBlobUri.split("/");
        String folderName = segments[segments.length - 1];

        AsyncPollResponse<KeyVaultRestoreOperation, Void> selectiveRestorePollResponse =
            asyncClient.beginSelectiveRestore("testKey", blobStorageUrl, sasToken, folderName).blockLast();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, selectiveRestorePollResponse.getStatus());
    }
}
