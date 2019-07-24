// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query.metrics;

import java.time.Instant;

/**
 * Stores information about fetch execution
 */
public class FetchExecutionRange {
    private final Instant startTime;
    private final Instant endTime;
    private final String partitionId;
    private final long numberOfDocuments;
    private final long retryCount;
    private final String activityId;

    /**
     * Constructor
     *
     * @param activityId        The activityId of the fetch
     * @param startTime         The start time of the fetch
     * @param endTime           The end time of the fetch
     * @param partitionId       The partitionkeyrangeid from which you are fetching for
     * @param numberOfDocuments The number of documents that were fetched in the particular execution range
     * @param retryCount        The number of times we retried for this fetch execution range
     */
    FetchExecutionRange(String activityId, Instant startTime, Instant endTime, String partitionId, long numberOfDocuments, long retryCount) {
        this.activityId = activityId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partitionId = partitionId;
        this.numberOfDocuments = numberOfDocuments;
        this.retryCount = retryCount;
    }

    /**
     * Gets the start time of the fetch.
     *
     * @return the start time of the fetch.
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time of the fetch.
     *
     * @return the end time of the fetch.
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Gets the partition id that was fetched from.
     *
     * @return the partition id that was fetched from.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Gets the number of documents that where fetched in the particular execution range.
     *
     * @return the number of documents that where fetched in the particular execution range.
     */
    public long getNumberOfDocuments() {
        return numberOfDocuments;
    }

    /**
     * Gets the number of times we retried for this fetch execution range.
     *
     * @return the number of times we retried for this fetch execution range.
     */
    public long getRetryCount() {
        return retryCount;
    }

    /**
     * Gets the activityId of the fetch.
     *
     * @return the activityId of the fetch.
     */
    public String getActivityId() {
        return activityId;
    }
}
