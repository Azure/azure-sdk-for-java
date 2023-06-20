// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.JobStateSelector;
import com.azure.core.annotation.Fluent;

/**
 * Request options to list jobs.
 * Job: A unit of work to be routed.
 */
@Fluent
public class ListJobsOptions {

    /**
     * JobStateSelector.
     */
    private final JobStateSelector jobStateSelector;

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
    public ListJobsOptions(JobStateSelector jobStateSelector, String queueId, String channelId, String classificationPolicyId, Integer maxPageSize) {
        this.jobStateSelector = jobStateSelector;
        this.queueId = queueId;
        this.channelId = channelId;
        this.classificationPolicyId = classificationPolicyId;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Returns jobStateSelector.
     * @return jobStateSelector.
     */
    public JobStateSelector getJobStateSelector() {
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
}
