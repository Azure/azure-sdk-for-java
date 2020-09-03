// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

/**
 * A class that contains the details of a backup operation.
 */
public final class KeyVaultBackupOperation extends KeyVaultLongRunningOperation {
    private final String azureStorageBlobContainerUri;

    /**
     * Creates an object containing the details of a {@link KeyVaultBackupOperation}.
     *
     * @param status Status of the {@link KeyVaultBackupOperation}.
     * @param statusDetails The status details of the {@link KeyVaultBackupOperation}.
     * @param error Error encountered, if any, during the {@link KeyVaultBackupOperation}.
     * @param startTime The start time of the {@link KeyVaultBackupOperation} in UTC.
     * @param endTime The end time of the {@link KeyVaultBackupOperation} in UTC.
     * @param jobId Identifier for the full {@link KeyVaultBackupOperation}.
     * @param azureStorageBlobContainerUri The Azure blob storage container URI which contains the backup.
     */
    public KeyVaultBackupOperation(String status, String statusDetails, KeyVaultError error, String jobId, Long startTime, Long endTime, String azureStorageBlobContainerUri) {
        super(status, statusDetails, error, jobId, startTime, endTime);
        this.azureStorageBlobContainerUri = azureStorageBlobContainerUri;
    }

    /**
     * Get the Azure Blob Storage container URI where the backup resides.
     *
     * @return The backup URI in {@link String} form.
     */
    public String getAzureStorageBlobContainerUri() {
        return azureStorageBlobContainerUri;
    }
}
