// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * Request options to list jobs.
 * Job: A unit of work to be routed.
 */
@Fluent
public class ListJobsOptions {

    /**
     * JobStateSelector.
     */
    private final RouterJobStatusSelector jobStateSelector;

    /**
     * QueueId.
     */
    private final String queueId;

    /**
     * ChannelId.
     */
    private final String channelId;

    /**
     * ClassificationPolicyId.
     */
    private final String classificationPolicyId;

    private OffsetDateTime scheduledBefore;

    private OffsetDateTime scheduledAfter;

    /**
     * MaxPageSize.
     */
    private final Integer maxPageSize;

    /**
     * Constructor for ListJobsOptions.
     *
     * @param jobStateSelector JobStateSelector.
     * @param queueId QueueId.
     * @param channelId ChannelId.
     * @param classificationPolicyId ClassificationPolicyId.
     * @param maxPageSize Maximum number of items per page.
     */
    public ListJobsOptions(RouterJobStatusSelector jobStateSelector, String queueId, String channelId, String classificationPolicyId, Integer maxPageSize) {
        this.jobStateSelector = jobStateSelector;
        this.queueId = queueId;
        this.channelId = channelId;
        this.classificationPolicyId = classificationPolicyId;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Set scheduledBefore
     * @param scheduledBefore
     * @return ListJobOptions object
     */
    public ListJobsOptions setScheduledBefore(OffsetDateTime scheduledBefore) {
        this.scheduledBefore = scheduledBefore;
        return this;
    }

    /**
     * Set scheduledAfter
     * @param scheduledAfter
     * @return ListJobOptions object
     */
    public ListJobsOptions setScheduledAfter(OffsetDateTime scheduledAfter) {
        this.scheduledAfter = scheduledAfter;
        return this;
    }

    /**
     * Returns jobStateSelector.
     * @return jobStateSelector.
     */
    public RouterJobStatusSelector getJobStateSelector() {
        return this.jobStateSelector;
    }

    /**
     * Returns queueId.
     * @return queueId.
     */
    public String getQueueId() {
        return this.queueId;
    }

    /**
     * Returns channelId.
     * @return channelId.
     */
    public String getChannelId() {
        return this.channelId;
    }

    /**
     * Returns classificationId.
     * @return classificationPolicyId.
     */
    public String getClassificationPolicyId() {
        return this.classificationPolicyId;
    }

    /**
     * Returns maxPageSize.
     * @return maxPageSize.
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }

    /**
     * Returns scheduledBefore
     * @return scheduledBefore
     */
    public OffsetDateTime getScheduledBefore() {
        return this.scheduledBefore;
    }

    /**
     * Returns scheduledAfter
     * @return scheduledAfter
     */
    public OffsetDateTime getScheduledAfter() {
        return this.scheduledAfter;
    }
}
