// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Abstract class for Create and Update WorkerOptions.
 */
@Fluent
public abstract class WorkerOptions {
    /**
     * The id property.
     */
    protected String workerId;

    /**
     * The queue(s) that this worker can receive work from.
     */
    protected Map<String, QueueAssignment> queueAssignments;

    /**
     * The total capacity score this worker has to manage multiple concurrent
     * jobs.
     */
    protected Integer totalCapacity;

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    protected Map<String, LabelValue> labels;

    /**
     * A set of non-identifying attributes attached to this worker.
     */
    protected Map<String, Object> tags;

    /**
     * The channel(s) this worker can handle and their impact on the workers
     * capacity.
     */
    protected Map<String, ChannelConfiguration> channelConfigurations;

    /**
     * A flag indicating this worker is open to receive offers or not.
     */
    protected Boolean availableForOffers;

    /**
     * Returns id.
     * @return id
     */
    public String getWorkerId() {
        return this.workerId;
    }

    /**
     * Returns queue assignments.
     * @return queueAssignments
     */
    public Map<String, QueueAssignment> getQueueAssignments() {
        return this.queueAssignments;
    }

    /**
     * Returns totalCapacity.
     * @return totalCapacity
     */
    public Integer getTotalCapacity() {
        return this.totalCapacity;
    }

    /**
     * Returns labels.
     * @return labels
     */
    public Map<String, LabelValue> getLabels() {
        return this.labels;
    }

    /**
     * Returns tags.
     * @return tags
     */
    public Map<String, Object> getTags() {
        return this.tags;
    }

    /**
     * Returns channelConfigurations.
     * @return channelConfigurations
     */
    public Map<String, ChannelConfiguration> getChannelConfigurations() {
        return this.channelConfigurations;
    }

    /**
     * Returns availableForOffers.
     * @return availableForOffers
     */
    public Boolean getAvailableForOffers() {
        return this.availableForOffers;
    }
}
