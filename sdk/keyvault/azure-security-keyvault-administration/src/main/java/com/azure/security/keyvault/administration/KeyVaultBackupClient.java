// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultBackupOperation, String> beginBackup(String blobStorageUrl, String sasToken, Duration pollingInterval) {
        return asyncClient.beginBackup(blobStorageUrl, sasToken, pollingInterval).getSyncPoller();
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code backupFolderUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginRestore(String backupFolderUrl, String sasToken) {
        return asyncClient.beginRestore(backupFolderUrl, sasToken).getSyncPoller();
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code backupFolderUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginRestore(String backupFolderUrl, String sasToken, Duration pollingInterval) {
        return asyncClient.beginRestore(backupFolderUrl, sasToken, pollingInterval).getSyncPoller();
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code keyName}, {@code backupFolderUrl} or {@code sasToken} are {@code
     * null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginSelectiveRestore(String keyName, String backupFolderUrl, String sasToken) {
        return asyncClient.beginSelectiveRestore(keyName, backupFolderUrl, sasToken).getSyncPoller();
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob
     * storage backup folder.
     *
     * @param keyName The name of the key to be restored.
     * @param backupFolderUrl The URL for the Blob Storage resource where the backup is located, including the path to
     * the blob container where the backup resides. This would be the exact value that is returned as the result of a
     * backup operation. An example of such a URL may look like the following: https://contoso.blob.core.windows.net/backup/mhsm-contoso-2020090117323313.
     * @param sasToken A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param pollingInterval The interval at which the operation status will be polled for.
     * @return A {@link SyncPoller} to poll on the {@link KeyVaultRestoreOperation restore operation} status.
     * @throws NullPointerException if the {@code keyName}, {@code backupFolderUrl} or {@code sasToken} are {@code
     * null}.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultRestoreOperation, Void> beginSelectiveRestore(String keyName, String backupFolderUrl, String sasToken, Duration pollingInterval) {
        return asyncClient.beginSelectiveRestore(keyName, backupFolderUrl, sasToken, pollingInterval).getSyncPoller();
    }
}
