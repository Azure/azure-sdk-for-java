// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * The data feed ingestion settings model.
 */
@Fluent
public final class DataFeedIngestionSettings {
    private final OffsetDateTime ingestionStartTime;
    private int sourceRequestConcurrency;
    private Duration ingestionRetryDelay;
    private Duration stopRetryAfter;
    private Duration ingestionStartOffset;

    /**
     * Create a DataFeedIngestionSettings object.
     *
     * @param ingestionStartTime the data feed ingestion start time value.
     */
    public DataFeedIngestionSettings(OffsetDateTime ingestionStartTime) {
        this.ingestionStartTime = ingestionStartTime;
    }

    /**
     * Get the ingestion start time.
     *
     * @return the ingestion start time.
     */
    public OffsetDateTime getIngestionStartTime() {
        return this.ingestionStartTime;
    }


    /**
     * Get the max concurrency of data ingestion queries against user data source. 0 means
     * no limitation.
     *
     * @return the the max concurrency of data ingestion queries against user data source value.
     */
    public Integer getDataSourceRequestConcurrency() {
        return this.sourceRequestConcurrency;
    }

    /**
     * Get the duration for minimum retry interval for failed data ingestion tasks.
     *
     * @return the duration for minimum retry interval for failed data ingestion tasks value.
     */
    public Duration getIngestionRetryDelay() {
        return ingestionRetryDelay;
    }

    /**
     * Get the duration for which to stop before retrying data ingestion after the data slice first schedule time.
     *
     * @return the the duration for which to stop before retrying data ingestion after the data slice first schedule
     * time value.
     */
    public Duration getStopRetryAfter() {
        return stopRetryAfter;
    }

    /**
     * Get the duration that the beginning of data ingestion task will delay for every
     * data slice according to this offset.
     *
     * @return the the duration that the beginning of data ingestion task will delay for every
     * data slice according to this offset value.
     */
    public Duration getIngestionStartOffset() {
        return this.ingestionStartOffset;
    }

    /**
     * Set the max concurrency of data ingestion queries against user data source. 0 means no limitation.
     *
     * @param maxConcurrency the maxConcurrency value to set.
     *
     * @return the DataFeedIngestionSettings object itself.
     */
    public DataFeedIngestionSettings setDataSourceRequestConcurrency(Integer maxConcurrency) {
        this.sourceRequestConcurrency = maxConcurrency;
        return this;
    }

    /**
     * Set the duration retry interval for failed data ingestion tasks.
     *
     * @param minRetryInterval the minRetryInterval value to set.
     *
     * @return the DataFeedIngestionSettings object itself.
     */
    public DataFeedIngestionSettings setIngestionRetryDelay(Duration minRetryInterval) {
        this.ingestionRetryDelay = minRetryInterval;
        return this;
    }

    /**
     * Set the duration for which to stop retrying data ingestion after the data slice first schedule time.
     *
     * @param stopRetryAfterDuration the stopRetryAfterDuration value to set.
     *
     * @return the DataFeedIngestionSettings object itself.
     */
    public DataFeedIngestionSettings setStopRetryAfter(Duration stopRetryAfterDuration) {
        this.stopRetryAfter = stopRetryAfterDuration;
        return this;
    }

    /**
     * Set the duration that the beginning of data ingestion task will delay for every
     * data slice according to this offset.
     *
     * @param startOffset the duration that the beginning of data ingestion task will delay for every
     * data slice according to this offset value to set.
     *
     * @return the DataFeedIngestionSettings object itself.
     */
    public DataFeedIngestionSettings setIngestionStartOffset(Duration startOffset) {
        this.ingestionStartOffset = startOffset;
        return this;
    }
}
