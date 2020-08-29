package com.azure.security.keyvault.administration.models;

import com.azure.security.keyvault.administration.implementation.models.Error;

/**
 * A class that contains the details of a restore operation.
 */
public class KeyVaultRestoreOperation extends KeyVaultLongRunningOperation {
    /**
     * Creates an object containing the details of a {@link KeyVaultRestoreOperation}.
     *
     * @param status                       Status of the {@link KeyVaultRestoreOperation}.
     * @param statusDetails                The status details of the {@link KeyVaultRestoreOperation}.
     * @param error                        Error encountered, if any, during the {@link KeyVaultRestoreOperation}.
     * @param startTime                    The start time of the {@link KeyVaultRestoreOperation} in UTC.
     * @param endTime                      The end time of the {@link KeyVaultRestoreOperation} in UTC.
     * @param jobId                        Identifier for the full {@link KeyVaultRestoreOperation}.
     */
    public KeyVaultRestoreOperation(String status, String statusDetails, Error error, String jobId, Long startTime,
                                    Long endTime) {
        super(status, statusDetails, error, jobId, startTime, endTime);
    }
}
