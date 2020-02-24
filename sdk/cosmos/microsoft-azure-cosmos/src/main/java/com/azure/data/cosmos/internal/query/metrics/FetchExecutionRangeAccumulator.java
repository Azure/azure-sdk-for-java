// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query.metrics;

import org.apache.commons.lang3.time.StopWatch;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Accumlator that acts as a builder of FetchExecutionRanges
 */
public class FetchExecutionRangeAccumulator {
    private final String partitionKeyRangeId;
    private final Instant constructionTime;
    private final StopWatch stopwatch;
    private List<FetchExecutionRange> fetchExecutionRanges;
    private Instant startTime;
    private Instant endTime;
    private boolean isFetching;

    public FetchExecutionRangeAccumulator(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
        this.constructionTime = Instant.now();
        // This stopwatch is always running and is only used to calculate deltas that are synchronized with the construction time.
        this.stopwatch = new StopWatch();
        stopwatch.start();
        this.fetchExecutionRanges = new ArrayList<FetchExecutionRange>();
    }

    /**
     * Gets the FetchExecutionRanges and resets the accumulator.
     *
     * @return the SchedulingMetricsResult.
     */
    public List<FetchExecutionRange> getExecutionRanges() {
        List<FetchExecutionRange> returnValue = this.fetchExecutionRanges;
        this.fetchExecutionRanges = new ArrayList<>();
        return returnValue;
    }

    /**
     * Updates the most recent start time internally.
     */
    public void beginFetchRange() {
        if (!this.isFetching) {
            // Calculating the start time as the construction time and the stopwatch as a delta.
            this.startTime = this.constructionTime.plus(Duration.ofMillis(this.stopwatch.getTime(TimeUnit.MILLISECONDS)));
            this.isFetching = true;
        }
    }

    /**
     * Updates the most recent end time internally and constructs a new FetchExecutionRange
     *
     * @param numberOfDocuments The number of documents that were fetched for this range.
     * @param retryCount        The number of times we retried for this fetch execution range.
     */
    public void endFetchRange(String activityId, long numberOfDocuments, long retryCount) {
        if (this.isFetching) {
            // Calculating the end time as the construction time and the stopwatch as a delta.
            this.endTime = this.constructionTime.plus(Duration.ofMillis(this.stopwatch.getTime(TimeUnit.MILLISECONDS)));
            FetchExecutionRange fetchExecutionRange = new FetchExecutionRange(
                    activityId,
                    this.startTime,
                    this.endTime,
                    this.partitionKeyRangeId,
                    numberOfDocuments,
                    retryCount);
            this.fetchExecutionRanges.add(fetchExecutionRange);
            this.isFetching = false;
        }
    }

}
