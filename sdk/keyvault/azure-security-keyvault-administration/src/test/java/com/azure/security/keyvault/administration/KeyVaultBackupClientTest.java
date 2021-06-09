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
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        backupPoller.waitForCompletion();

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
        String backupFolderUrl = backupPoller.getFinalResult();
        SyncPoller<KeyVaultRestoreOperation, Void> restorePoller = client.beginRestore(backupFolderUrl, sasToken);

        restorePoller.waitForCompletion();

        PollResponse<KeyVaultRestoreOperation> restoreResponse = restorePoller.poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, restoreResponse.getStatus());
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveKeyRestore(HttpClient httpClient) {
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
        String backupFolderUrl = backupPoller.getFinalResult();
        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, Void> selectiveKeyRestorePoller =
            client.beginSelectiveKeyRestore("testKey", backupFolderUrl, sasToken);

        selectiveKeyRestorePoller.waitForCompletion();

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> response = selectiveKeyRestorePoller.poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }
}
