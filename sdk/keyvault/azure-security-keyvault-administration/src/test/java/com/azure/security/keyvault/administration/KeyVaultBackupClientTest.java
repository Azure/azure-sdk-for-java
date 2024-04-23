// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KeyVaultBackupClientTest extends KeyVaultBackupClientTestBase {
    private KeyVaultBackupClient client;

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        client = spy(getClientBuilder(buildSyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), forCleanup)
            .buildClient());
        if (interceptorManager.isPlaybackMode()) {
            when(client.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    /**
     * Tests that a Key Vault can be backed up.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginBackup(HttpClient httpClient) {
        getClient(httpClient, false);

        SyncPoller<KeyVaultBackupOperation, String> backupPoller =
            setPlaybackSyncPollerPollInterval(client.beginBackup(blobStorageUrl, sasToken));

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
        getClient(httpClient, false);

        // Create a backup
        SyncPoller<KeyVaultBackupOperation, String> backupPoller =
            setPlaybackSyncPollerPollInterval(client.beginBackup(blobStorageUrl, sasToken));

        backupPoller.waitForCompletion();

        // Restore the backup
        String backupFolderUrl = backupPoller.getFinalResult();
        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller =
            setPlaybackSyncPollerPollInterval(client.beginRestore(backupFolderUrl, sasToken));

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
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginSelectiveKeyRestore(HttpClient httpClient) {
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl(getEndpoint())
            .serviceVersion(KeyServiceVersion.V7_2)
            .pipeline(getPipeline(httpClient, false))
            .buildClient();

        String keyName = testResourceNamer.randomName("backupKey", 20);
        CreateRsaKeyOptions rsaKeyOptions = new CreateRsaKeyOptions(keyName)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC));

        KeyVaultKey createdKey = keyClient.createRsaKey(rsaKeyOptions);

        getClient(httpClient, false);

        // Create a backup
        SyncPoller<KeyVaultBackupOperation, String> backupPoller =
            setPlaybackSyncPollerPollInterval(client.beginBackup(blobStorageUrl, sasToken));

        backupPoller.waitForCompletion();

        // Restore one key from said backup
        String backupFolderUrl = backupPoller.getFinalResult();
        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> selectiveKeyRestorePoller =
            setPlaybackSyncPollerPollInterval(client.beginSelectiveKeyRestore(createdKey.getName(), backupFolderUrl,
                sasToken));

        selectiveKeyRestorePoller.waitForCompletion();

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> response = selectiveKeyRestorePoller.poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());

        // For some reason, the service might still think a restore operation is running even after returning a success
        // signal. This gives it some time to "clear" the operation.
        sleepIfRunningAgainstService(30000);
    }
}
