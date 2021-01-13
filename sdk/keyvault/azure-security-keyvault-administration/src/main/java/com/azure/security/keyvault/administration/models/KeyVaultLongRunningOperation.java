// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

/**
 * A class that contains the details of a long running operation.
 */
public class KeyVaultLongRunningOperation {
    private final String status;
    private final String statusDetails;
    private final KeyVaultError error;
    private final String jobId;
    private final Long startTime;
    private final Long endTime;

    /**
     * Creates an object containing the details of a {@link KeyVaultLongRunningOperation}.
     *
     * @param status Status of the {@link KeyVaultLongRunningOperation}.
     * @param statusDetails The status details of the {@link KeyVaultLongRunningOperation}.
     * @param error Error encountered, if any, during the {@link KeyVaultLongRunningOperation}.
     * @param startTime The start time of the {@link KeyVaultLongRunningOperation} in UTC.
     * @param endTime The end time of the {@link KeyVaultLongRunningOperation} in UTC.
     * @param jobId Identifier for the full {@link KeyVaultLongRunningOperation}.
     */
    public KeyVaultLongRunningOperation(String status, String statusDetails, KeyVaultError error, String jobId, Long startTime, Long endTime) {
        this.status = status;
        this.statusDetails = statusDetails;
        this.error = error;
        this.startTime = startTime;
        this.endTime = endTime;
        this.jobId = jobId;
    }

    /**
     * Get the status of the {@link KeyVaultLongRunningOperation}.
     *
     * @return The backup status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the status details of the {@link KeyVaultLongRunningOperation}.
     *
     * @return The backup status details.
     */
    public String getStatusDetails() {
        return statusDetails;
    }

    /**
     * Get the error encountered, if any, during the {@link KeyVaultLongRunningOperation}.
     *
     * @return The error.
     */
    public KeyVaultError getError() {
        return error;
    }

    /**
     * Get the start time of the {@link KeyVaultLongRunningOperation} in UTC.
     *
     * @return The start time in UTC.
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Get the end time of the {@link KeyVaultLongRunningOperation} in UTC.
     *
     * @return The end time in UTC.
     */
    public Long getEndTime() {
        return endTime;
    }

    /**
     * Get the identifier for the {@link KeyVaultLongRunningOperation}.
     *
     * @return The job ID.
     */
    public String getJobId() {
        return jobId;
    }
}
