// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.polling.implementation;

import com.typespec.core.util.polling.LongRunningOperationStatus;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * A simple structure representing the partial response received from an operation location URL, containing the
 * information of the status of the long-running operation.
 */
public final class PollResult {
    private LongRunningOperationStatus status;
    private String resourceLocation;

    /**
     * Gets the status of the long-running operation.
     *
     * @return the status represented as a {@link LongRunningOperationStatus}
     */
    public LongRunningOperationStatus getStatus() {
        return status;
    }

    /**
     * Sets the long-running operation status in the format of a string returned by the service. This is called by
     * the deserializer when a response is received.
     *
     * @param status the status of the long-running operation
     * @return the modified PollResult instance
     */
    @JsonSetter
    public PollResult setStatus(String status) {
        if (PollingConstants.STATUS_NOT_STARTED.equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.NOT_STARTED;
        } else if (PollingConstants.STATUS_IN_PROGRESS.equalsIgnoreCase(status)
            || PollingConstants.STATUS_RUNNING.equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.IN_PROGRESS;
        } else if (PollingConstants.STATUS_SUCCEEDED.equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if (PollingConstants.STATUS_FAILED.equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.FAILED;
        } else if (PollingConstants.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.USER_CANCELLED;
        } else {
            this.status = LongRunningOperationStatus.fromString(status, false);
        }
        return this;
    }

    /**
     * Sets the long-running operation status in the format of the {@link LongRunningOperationStatus} enum.
     *
     * @param status the status of the long-running operation
     * @return the modified PollResult instance
     */
    public PollResult setStatus(LongRunningOperationStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Gets the resource location URL to get the final result. This is often available in the response when the
     * long-running operation has been successfully completed.
     *
     * @return the resource location URL to get the final result
     */
    public String getResourceLocation() {
        return resourceLocation;
    }

    /**
     * Sets the resource location URL. this should only be called by the deserializer when a response is received.
     *
     * @param resourceLocation the resource location URL
     * @return the modified PollResult instance
     */
    public PollResult setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
        return this;
    }
}
