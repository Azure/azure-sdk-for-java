// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;
import io.clientcore.core.http.client.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultBackupClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupClient client;

    private void getClient(HttpClient httpClient) throws IOException {
        client = getClientBuilder(buildSyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)).buildClient();
        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new TestUtils.AssertingHttpClientBuilder(httpClient).assertSync().build();
    }

    private <T, U> Poller<T, U> setPlaybackSyncPollerPollInterval(Poller<T, U> poller) {
        return interceptorManager.isPlaybackMode() ? poller.setPollInterval(Duration.ofMillis(1)) : poller;
    }

    protected void sleepIfRunningAgainstService(long millis) {
        if (!interceptorManager.isPlaybackMode()) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Tests that a Key Vault can be backed up.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginBackup(HttpClient httpClient) throws IOException {
        getClient(httpClient);

        Poller<KeyVaultBackupOperation, String> backupPoller
            = setPlaybackSyncPollerPollInterval(client.beginBackup(blobStorageUrl, sasToken));
        PollResponse<KeyVaultBackupOperation> pollResponse = backupPoller.waitForCompletion();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());

        String backupBlobUri = backupPoller.getFinalResult();

        assertNotNull(backupBlobUri);
        assertTrue(backupBlobUri.startsWith(blobStorageUrl));
    }

    /**
     * Tests that a Key Vault can be restored from a backup.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginRestore(HttpClient httpClient) throws IOException {
        getClient(httpClient);

        // Create a backup
        Poller<KeyVaultBackupOperation, String> backupPoller
            = setPlaybackSyncPollerPollInterval(client.beginBackup(blobStorageUrl, sasToken));
        PollResponse<KeyVaultBackupOperation> pollResponse = backupPoller.waitForCompletion();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollResponse.getStatus());

        // Restore the backup
        String backupFolderUrl = backupPoller.getFinalResult();
        Poller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller
            = setPlaybackSyncPollerPollInterval(client.beginRestore(backupFolderUrl, sasToken));

        restorePoller.waitForCompletion();

        PollResponse<KeyVaultRestoreOperation> restoreResponse = restorePoller.poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, restoreResponse.getStatus());

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }

    /**
     * Tests that a key can be restored from a backup.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveKeyRestore(HttpClient httpClient) throws IOException {
        getClient(httpClient);

        String keyName = testResourceNamer.randomName("backupKey", 20);

        // Create a backup
        Poller<KeyVaultBackupOperation, String> backupPoller
            = setPlaybackSyncPollerPollInterval(client.beginBackup(blobStorageUrl, sasToken));
        PollResponse<KeyVaultBackupOperation> backupPollResponse = backupPoller.waitForCompletion();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, backupPollResponse.getStatus());

        // Restore one key from said backup
        String backupFolderUrl = backupPoller.getFinalResult();
        Poller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> selectiveKeyRestorePoller
            = setPlaybackSyncPollerPollInterval(
                client.beginSelectiveKeyRestore(keyName, backupFolderUrl, sasToken));
        PollResponse<KeyVaultSelectiveKeyRestoreOperation> restorePollResponse
            = selectiveKeyRestorePoller.waitForCompletion();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, restorePollResponse.getStatus());

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }
}
