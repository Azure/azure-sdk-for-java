// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

public class KeyVaultBackupClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupClient client;

    private final String blobStorageUrl = "https://testaccount.blob.core.windows.net/backup";
    private final String sasToken = "someSasToken";

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    /**
     * Tests that a Key Vault can be backed up.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginBackup(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        SyncPoller<KeyVaultBackupOperation, String> backupPoller = client.beginBackup(blobStorageUrl, sasToken);

        PollResponse<KeyVaultBackupOperation> backupResponse = backupPoller.waitForCompletion();

        String backupBlobUri = backupPoller.getFinalResult();

        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));
    }

    /**
     * Tests that a Key Vault can be restored from a backup.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginRestore(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        // Create a backup
        SyncPoller<KeyVaultBackupOperation, String> backupPoller = client.beginBackup(blobStorageUrl, sasToken);

        backupPoller.waitForCompletion();

        // Restore the backup
        String backupBlobUri = backupPoller.getFinalResult();
        String[] segments = backupBlobUri.split("/");
        String folderName = segments[segments.length - 1];

        SyncPoller<KeyVaultRestoreOperation, Void> restorePoller =
            client.beginRestore(blobStorageUrl, sasToken, folderName);

        restorePoller.waitForCompletion();

        PollResponse<KeyVaultRestoreOperation> restoreResponse = restorePoller.poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, restoreResponse.getStatus());
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveRestore(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no Managed HSM environment for pipeline testing.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        // Create a backup
        SyncPoller<KeyVaultBackupOperation, String> backupPoller = client.beginBackup(blobStorageUrl, sasToken);

        backupPoller.waitForCompletion();

        // Restore one key from said backup
        String backupBlobUri = backupPoller.getFinalResult();
        String[] segments = backupBlobUri.split("/");
        String folderName = segments[segments.length - 1];

        SyncPoller<KeyVaultRestoreOperation, Void> selectiveRestorePoller =
            client.beginSelectiveRestore("testKey", blobStorageUrl, sasToken, folderName);

        PollResponse<KeyVaultRestoreOperation> response = selectiveRestorePoller.poll();

        assertNotNull(response);
        assertEquals(LongRunningOperationStatus.IN_PROGRESS, response.getStatus());
        assertNotNull(response.getValue());

        selectiveRestorePoller.waitForCompletion();

        response = selectiveRestorePoller.poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }
}
