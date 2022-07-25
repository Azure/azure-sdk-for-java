// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.ChannelConfiguration;
import com.azure.communication.jobrouter.models.LabelValue;
import com.azure.communication.jobrouter.models.QueueAssignment;

import java.util.Map;

/**
 * Request options to create a worker.
 * Worker: An entity for jobs to be routed to.
 */
public class CreateWorkerOptions {
    /**
     * The id property.
     */
    private final String workerId;

    /**
     * The queue(s) that this worker can receive work from.
     */
    private Map<String, QueueAssignment> queueAssignments;

    /**
     * The total capacity score this worker has to manage multiple concurrent
     * jobs.
     */
    private final Integer totalCapacity;

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    private Map<String, LabelValue> labels;

    /**
     * A set of non-identifying attributes attached to this worker.
     */
    private Map<String, Object> tags;

    /**
     * The channel(s) this worker can handle and their impact on the workers
     * capacity.
     */
    private Map<String, ChannelConfiguration> channelConfigurations;

    /**
     * A flag indicating this worker is open to receive offers or not.
     */
    private Boolean availableForOffers;

    /**
     * Constructor for CreateWorkerOptions.
     * @param workerId The id property.
     * @param totalCapacity The total capacity score this worker has to manage multiple concurrent jobs.
     */
    public CreateWorkerOptions(String workerId, Integer totalCapacity) {
        this.workerId = workerId;
        this.totalCapacity = totalCapacity;
    }

    /**
     * Set the queueAssignments property: The queue(s) that this worker can receive work from.
     *
     * @param queueAssignments the queueAssignments value to set.
     * @return this
     */
    public CreateWorkerOptions setQueueAssignments(Map<String, QueueAssignment> queueAssignments) {
        this.queueAssignments = queueAssignments;
        return this;
    }

    /**
     * Set the labels property: A set of key/value pairs that are identifying attributes used by the rules engines to
     * make decisions.
     *
     * @param labels the labels value to set.
     * @return this
     */
    public CreateWorkerOptions setLabels(Map<String, LabelValue> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Set the tags property: A set of non-identifying attributes attached to this worker.
     *
     * @param tags the tags value to set.
     * @return this
     */
    public CreateWorkerOptions setTags(Map<String, Object> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Set the channelConfigurations property: The channel(s) this worker can handle and their impact on the workers
     * capacity.
     *
     * @param channelConfigurations the channelConfigurations value to set.
     * @return this
     */
    public CreateWorkerOptions setChannelConfigurations(Map<String, ChannelConfiguration> channelConfigurations) {
        this.channelConfigurations = channelConfigurations;
        return this;
    }

    /**
     * Set the availableForOffers property: A flag indicating this worker is open to receive offers or not.
     *
     * @param availableForOffers the availableForOffers value to set.
     * @return this
     */
    public CreateWorkerOptions setAvailableForOffers(Boolean availableForOffers) {
        this.availableForOffers = availableForOffers;
        return this;
    }

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
