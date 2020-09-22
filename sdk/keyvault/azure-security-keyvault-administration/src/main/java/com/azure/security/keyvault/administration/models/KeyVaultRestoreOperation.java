// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

/**
 * A class that contains the details of a restore operation.
 */
public final class KeyVaultRestoreOperation extends KeyVaultLongRunningOperation {
    /**
     * Creates an object containing the details of a {@link KeyVaultRestoreOperation}.
     *
     * @param status Status of the {@link KeyVaultRestoreOperation}.
     * @param statusDetails The status details of the {@link KeyVaultRestoreOperation}.
     * @param error Error encountered, if any, during the {@link KeyVaultRestoreOperation}.
     * @param startTime The start time of the {@link KeyVaultRestoreOperation} in UTC.
     * @param endTime The end time of the {@link KeyVaultRestoreOperation} in UTC.
     * @param jobId Identifier for the full {@link KeyVaultRestoreOperation}.
     */
    public KeyVaultRestoreOperation(String status, String statusDetails, KeyVaultError error, String jobId, Long startTime, Long endTime) {
        super(status, statusDetails, error, jobId, startTime, endTime);
    }
}
