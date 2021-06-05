// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

// I don't know why a poll result type is needed for a Poller but sure I will create one just for now
public class PollResult {
    private LongRunningOperationStatus status;
    private String resourceLocation;

    public LongRunningOperationStatus getStatus() {
        return status;
    }

    public PollResult setStatus(String status) {
        if ("NotStarted".equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.NOT_STARTED;
        } else if ("InProgress".equalsIgnoreCase(status)
                || "Running".equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.IN_PROGRESS;
        } else if ("Succeeded".equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.SUCCESSFULLY_COMPLETED;
        } else if ("Failed".equalsIgnoreCase(status)) {
            this.status = LongRunningOperationStatus.FAILED;
        } else {
            this.status = LongRunningOperationStatus.fromString(status, true);
        }
        return this;
    }

    public PollResult setStatus(LongRunningOperationStatus status) {
        this.status = status;
        return this;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }
}
