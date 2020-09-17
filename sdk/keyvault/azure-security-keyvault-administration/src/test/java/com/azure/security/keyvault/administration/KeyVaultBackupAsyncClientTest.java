// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
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
