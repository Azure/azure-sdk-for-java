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
    private RouterJobStatusSelector jobStateSelector;

    /**
     * QueueId.
     */
    private String queueId;

    /**
     * ChannelId.
     */
    private String channelId;

    /**
     * ClassificationPolicyId.
     */
    private String classificationPolicyId;

    private OffsetDateTime scheduledBefore;

    private OffsetDateTime scheduledAfter;

    /**
     * MaxPageSize.
     */
    private Integer maxPageSize;

    /**
     * Constructor for ListJobsOptions.
     */
    public ListJobsOptions() {
    }

    /**
     * Setter for RouterJobStatusSelector.
     * @param jobStateSelector of type RouterJobStatusSelector.
     * @return object of type ListJobOptions.
     */
    public ListJobsOptions setJobStateSelector(RouterJobStatusSelector jobStateSelector) {
        this.jobStateSelector = jobStateSelector;
        return this;
    }

    /**
     * Setter for QueueId.
     * @param queueId queueId.
     * @return object of type ListJobOptions.
     */
    public ListJobsOptions setQueueId(String queueId) {
        this.queueId = queueId;
        return this;
    }

    /**
     * Setter for channelId.
     * @param channelId channelId.
     * @return object of type ListJobOptions.
     */
    public ListJobsOptions setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    /**
     * Setter for classificationPolicyId.
     * @param classificationPolicyId classificationPolicyId.
     * @return object of type ListJobOptions.
     */
    public ListJobsOptions setClassificationPolicyId(String classificationPolicyId) {
        this.classificationPolicyId = classificationPolicyId;
        return this;
    }

    /**
     * Setter for maxPageSize.
     * @param maxPageSize maxPageSize.
     * @return object of type ListJobOptions.
     */
    public ListJobsOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
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
