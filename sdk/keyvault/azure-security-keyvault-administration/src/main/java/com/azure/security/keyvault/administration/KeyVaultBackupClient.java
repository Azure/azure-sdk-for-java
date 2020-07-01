// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.administration.implementation.models.FullBackupOperation;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.RestoreOperation;
import com.azure.security.keyvault.administration.implementation.models.SelectiveKeyRestoreOperation;

/**
 * The {@link KeyVaultBackupClient} provides synchronous methods to perform full backup and restore of an Azure Key
 * Vault.
 */
@ServiceClient(builder = KeyVaultBackupClientBuilder.class)
public class KeyVaultBackupClient {
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
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @return A {@link SyncPoller} polling on the {@link FullBackupOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<FullBackupOperation, Void> startBackup(String blobStorageUrl, String sasToken) {
        return asyncClient.startBackup(blobStorageUrl, sasToken).getSyncPoller();
    }

    /**
     * Initiates a full restore of the Key Vault.
     *
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @return A {@link SyncPoller} polling on the {@link RestoreOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<RestoreOperation, Void> startRestore(String blobStorageUrl, String sasToken, String folderName) {
        return asyncClient.startRestore(blobStorageUrl, sasToken, folderName).getSyncPoller();
    }

    /**
     * Restores all versions of a given key using the supplied SAS token pointing to a previously stored Azure Blob storage backup folder.
     *
     * @param keyName        The name of the key to be restored.
     * @param blobStorageUrl The URL for the Blob Storage resource where the backup is located.
     * @param sasToken       A Shared Access Signature (SAS) token to authorize access to the blob.
     * @param folderName     The name of the folder containing the backup data to restore.
     * @return A {@link PollerFlux} polling on the {@link RestoreOperation backup operation} status.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code blobStorageUrl} or {@code sasToken} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SyncPoller<SelectiveKeyRestoreOperation, Void> startSelectiveRestore(String keyName, String blobStorageUrl, String sasToken, String folderName) {
        return asyncClient.startSelectiveRestore(keyName, blobStorageUrl, sasToken, folderName).getSyncPoller();
    }
}
