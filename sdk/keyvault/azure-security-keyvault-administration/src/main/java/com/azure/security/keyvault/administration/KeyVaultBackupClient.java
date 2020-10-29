// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;

import java.time.Duration;

/**
 * The {@link KeyVaultBackupClient} provides synchronous methods to perform full backup and restore of an Azure Key
 * Vault.
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class)
public final class KeyVaultBackupClient {
    private final KeyVaultBackupAsyncClient asyncClient;

    /**
     * Creates an {@link KeyVaultBackupClient} that uses a {@code pipeline} to service requests
     *
     * @param asyncClient The {@link KeyVaultBackupAsyncClient} that the client routes its request through.
     */
    KeyVaultBackupClient(KeyVaultBackupAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Get the vault endpoint URL.
     *
     * @return The vault endpoint URL.
     */
    public String getVaultUrl() {
        return asyncClient.getVaultUrl();
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link SyncPoller} polling on the {@link KeyVaultBackupOperation backup operation} status.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken) {
        return asyncClient.beginBackup(blobStorageUrl, sasToken).getSyncPoller();
    }

    /**
     * Initiates a full backup of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup will be located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link SyncPoller} polling on the {@link KeyVaultBackupOperation backup operation} status.
     * @throws NullPointerException if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken, Duration pollingInterval) {
        return asyncClient.beginBackup(blobStorageUrl, sasToken, pollingInterval).getSyncPoller();
    }

    /**
     * Gets a pending {@link KeyVaultBackupOperation backup operation} from the Key Vault.
     *
     * @param jobId The operation identifier.
     * @throws NullPointerException if the {@code jobId} is null.
     * @return A {@link SyncPoller} to poll on the backup operation status.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultBackupOperation, String> getBackupOperation(String jobId) {
        return asyncClient.getBackupOperation(jobId).getSyncPoller();
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName The name of the folder containing the backup data to restore.
     * @return A {@link SyncPoller} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws NullPointerException if the {@code blobStorageUrl}, {@code sasToken} or {@code folderName} are {@code
     * null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginRestore(String blobStorageUrl, String sasToken, String folderName) {
        return asyncClient.beginRestore(blobStorageUrl, sasToken, folderName).getSyncPoller();
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName The name of the folder containing the backup data to restore.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link SyncPoller} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws NullPointerException if the {@code blobStorageUrl}, {@code sasToken} or {@code folderName} are {@code
     * null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginRestore(String blobStorageUrl, String sasToken, String folderName, Duration pollingInterval) {
        return asyncClient.beginRestore(blobStorageUrl, sasToken, folderName, pollingInterval).getSyncPoller();
    }

    /**
     * Gets a pending {@link KeyVaultRestoreOperation full or selective restore operation} from the Key Vault.
     *
     * @param jobId The operation identifier.
     * @throws NullPointerException if the {@code jobId} is null.
     * @return A {@link SyncPoller} to poll on the restore operation status.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultRestoreOperation, Void> getRestoreOperation(String jobId) {
        return asyncClient.getRestoreOperation(jobId).getSyncPoller();
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName The name of the folder containing the backup data to restore.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws NullPointerException if the {@code keyName}, {@code blobStorageUrl}, {@code sasToken} or {@code
     * folderName} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginSelectiveRestore(String keyName, String blobStorageUrl, String sasToken, String folderName) {
        return asyncClient.beginSelectiveRestore(keyName, blobStorageUrl, sasToken, folderName).getSyncPoller();
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName The name of the folder containing the backup data to restore.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link PollerFlux} polling on the {@link KeyVaultRestoreOperation backup operation} status.
     * @throws NullPointerException if the {@code keyName}, {@code blobStorageUrl}, {@code sasToken} or {@code
     * folderName} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginSelectiveRestore(String keyName, String blobStorageUrl, String sasToken, String folderName, Duration pollingInterval) {
        return asyncClient.beginSelectiveRestore(keyName, blobStorageUrl, sasToken, folderName, pollingInterval).getSyncPoller();
    }
}
