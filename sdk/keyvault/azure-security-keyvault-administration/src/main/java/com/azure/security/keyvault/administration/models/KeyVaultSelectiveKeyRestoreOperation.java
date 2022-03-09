// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

import java.time.OffsetDateTime;

/**
 * A class that contains the details of a selective key restore operation.
 */
@Immutable
public final class KeyVaultSelectiveKeyRestoreOperation extends KeyVaultLongRunningOperation {
    /**
     * Creates an object containing the details of a {@link KeyVaultSelectiveKeyRestoreOperation}.
     *
     * @param status Status of the {@link KeyVaultSelectiveKeyRestoreOperation}.
     * @param statusDetails The status details of the {@link KeyVaultSelectiveKeyRestoreOperation}.
     * @param error Error encountered, if any, during the {@link KeyVaultSelectiveKeyRestoreOperation}.
     * @param startTime The start time of the {@link KeyVaultSelectiveKeyRestoreOperation}.
     * @param endTime The end time of the {@link KeyVaultSelectiveKeyRestoreOperation}.
     * @param operationId Identifier for the full {@link KeyVaultSelectiveKeyRestoreOperation}.
     */
    public KeyVaultSelectiveKeyRestoreOperation(String status, String statusDetails, KeyVaultAdministrationError error,
                                                String operationId, OffsetDateTime startTime, OffsetDateTime endTime) {
        super(status, statusDetails, error, operationId, startTime, endTime);
    }
}
