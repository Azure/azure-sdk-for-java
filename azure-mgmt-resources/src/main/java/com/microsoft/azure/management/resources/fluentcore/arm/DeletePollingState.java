/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import java.sql.Time;

/**
 * Type representing delete operation progress state.
 */
public class DeletePollingState {
    private long delayBeforeNextPoll;
    private Time lastPollTime;
    private String status;
    private String azureAsyncOperationPollUrl;
    private String locationHeaderPollUrl;

    public void setFrom(DeletePollingState other) {
        if (other.azureAsyncOperationPollUrl != null) {
            this.withAzureAsyncOperationPollUrl(other.azureAsyncOperationPollUrl);
        }
        if (other.locationHeaderPollUrl != null) {
            this.withLocationHeaderPollUrl(other.locationHeaderPollUrl);
        }
        this.withStatus(other.status);
        this.withLastPollTime(other.lastPollTime);
    }

    public long delayBeforeNextPoll() {
        return this.delayBeforeNextPoll;
    }
    public Time lastPollTime() {
        return this.lastPollTime;
    }
    public String status() {
        return this.status;
    }
    public String azureAsyncOperationPollUrl() {
        return this.azureAsyncOperationPollUrl;
    }
    public String locationHeaderPollUrl() {
        return this.locationHeaderPollUrl;
    }

    DeletePollingState withDelayBeforeNextPoll(long delayBeforeNextPoll) {
        this.delayBeforeNextPoll = delayBeforeNextPoll;
        return this;
    }
    DeletePollingState withLastPollTime(Time lastPollTime) {
        this.lastPollTime = lastPollTime;
        return this;
    }
    DeletePollingState withStatus(String status) {
        this.status = status;
        return this;
    }
    DeletePollingState withAzureAsyncOperationPollUrl(String azureAsyncOperationPollUrl) {
        this.azureAsyncOperationPollUrl = azureAsyncOperationPollUrl;
        return this;
    }
    DeletePollingState withLocationHeaderPollUrl(String locationHeaderPollUrl) {
        this.locationHeaderPollUrl = locationHeaderPollUrl;
        return this;
    }
}
