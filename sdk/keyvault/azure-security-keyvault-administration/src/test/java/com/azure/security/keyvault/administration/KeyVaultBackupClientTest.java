// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
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
        KeyVaultBackupAsyncClient asyncClient = spy(getClientBuilder(httpClient, forCleanup).buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(asyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }

        client = new KeyVaultBackupClient(asyncClient);
    }

    /**
     * Tests that a Key Vault can be backed up.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void beginBackup(HttpClient httpClient) {
        getClient(httpClient, false);

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
        getClient(httpClient, false);

        // Create a backup
        SyncPoller<KeyVaultBackupOperation, String> backupPoller = client.beginBackup(blobStorageUrl, sasToken);

        backupPoller.waitForCompletion();

        // Restore the backup
        String backupFolderUrl = backupPoller.getFinalResult();
        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller =
            client.beginRestore(backupFolderUrl, sasToken);

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
        KeyClient keyClient = new KeyClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(getPipeline(httpClient, false))
            .serviceVersion(KeyServiceVersion.V7_1)
            .buildClient();

        String keyName = interceptorManager.isPlaybackMode()
            ? "testKey"
            : testResourceNamer.randomName("backupKey", 20);
        CreateRsaKeyOptions rsaKeyOptions = new CreateRsaKeyOptions(keyName)
            .setExpiresOn(OffsetDateTime.of(2050, 1, 30, 0, 0, 0, 0, ZoneOffset.UTC))
            .setNotBefore(OffsetDateTime.of(2000, 1, 30, 12, 59, 59, 0, ZoneOffset.UTC));

        KeyVaultKey createdKey = keyClient.createRsaKey(rsaKeyOptions);

        getClient(httpClient, false);

        // Create a backup
        SyncPoller<KeyVaultBackupOperation, String> backupPoller = client.beginBackup(blobStorageUrl, sasToken);

        backupPoller.waitForCompletion();

        // Restore one key from said backup
        String backupFolderUrl = backupPoller.getFinalResult();
        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> selectiveKeyRestorePoller =
            client.beginSelectiveKeyRestore(createdKey.getName(), backupFolderUrl, sasToken);

        selectiveKeyRestorePoller.waitForCompletion();

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> response = selectiveKeyRestorePoller.poll();

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
    }
}
