// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;

import java.time.OffsetDateTime;

/**
 * A class that contains the details of a backup operation.
 */
@Metadata(properties = { MetadataProperties.IMMUTABLE })
public final class KeyVaultBackupOperation extends KeyVaultLongRunningOperation {
    private final String azureStorageBlobContainerUrl;

    /**
     * Creates an object containing the details of a {@link KeyVaultBackupOperation}.
     *
     * @param status Status of the {@link KeyVaultBackupOperation}.
     * @param statusDetails The status details of the {@link KeyVaultBackupOperation}.
     * @param error Error encountered, if any, during the {@link KeyVaultBackupOperation}.
     * @param startTime The start time of the {@link KeyVaultBackupOperation}.
     * @param endTime The end time of the {@link KeyVaultBackupOperation}.
     * @param operationId Identifier for the full {@link KeyVaultBackupOperation}.
     * @param azureStorageBlobContainerUrl The Azure blob storage container URI which contains the backup.
     */
    public KeyVaultBackupOperation(String status, String statusDetails, KeyVaultAdministrationError error,
        String operationId, OffsetDateTime startTime, OffsetDateTime endTime, String azureStorageBlobContainerUrl) {
        super(status, statusDetails, error, operationId, startTime, endTime);
        this.azureStorageBlobContainerUrl = azureStorageBlobContainerUrl;
    }

    /**
     * Get the Azure Blob Storage container URI where the backup resides.
     *
     * @return The backup URI in {@link String} form.
     */
    public String getAzureStorageBlobContainerUrl() {
        return azureStorageBlobContainerUrl;
    }
}
