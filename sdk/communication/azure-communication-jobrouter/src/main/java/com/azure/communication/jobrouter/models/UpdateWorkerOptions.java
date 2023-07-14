// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.util.Map;

/**
 * Request options to update a worker.
 * Worker: An entity for jobs to be routed to.
 */
public final class UpdateWorkerOptions {
    /**
     * The id property.
     */
    private final String workerId;

    /**
     * The queue(s) that this worker can receive work from.
     */
    private Map<String, RouterQueueAssignment> queueAssignments;

    /**
     * The total capacity score this worker has to manage multiple concurrent
     * jobs.
     */
    private Integer totalCapacity;

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    private Map<String, LabelValue> labels;

    /**
     * A set of non-identifying attributes attached to this worker.
     */
    private Map<String, LabelValue> tags;

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
     * Constructor for UpdateWorkerOptions.
     * @param workerId The id property.
     */
    public UpdateWorkerOptions(String workerId) {
        this.workerId = workerId;
    }

    /**
     * Set the totalCapacity property: The total capacity score this worker has to manage multiple concurrent jobs.
     *
     * @param totalCapacity the totalCapacity value to set.
     * @return this
     */
    public UpdateWorkerOptions setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
        return this;
    }

    /**
     * Set the queueAssignments property: The queue(s) that this worker can receive work from.
     *
     * @param queueAssignments the queueAssignments value to set.
     * @return this
     */
    public UpdateWorkerOptions setQueueAssignments(Map<String, RouterQueueAssignment> queueAssignments) {
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
    public UpdateWorkerOptions setLabels(Map<String, LabelValue> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Set the tags property: A set of non-identifying attributes attached to this worker.
     *
     * @param tags the tags value to set.
     * @return this
     */
    public UpdateWorkerOptions setTags(Map<String, LabelValue> tags) {
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
    public UpdateWorkerOptions setChannelConfigurations(Map<String, ChannelConfiguration> channelConfigurations) {
        this.channelConfigurations = channelConfigurations;
        return this;
    }

    /**
     * Set the availableForOffers property: A flag indicating this worker is open to receive offers or not.
     *
     * @param availableForOffers the availableForOffers value to set.
     * @return this
     */
    public UpdateWorkerOptions setAvailableForOffers(Boolean availableForOffers) {
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
    public Map<String, RouterQueueAssignment> getQueueAssignments() {
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
    public Map<String, LabelValue> getTags() {
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
    public Boolean isAvailableForOffers() {
        return this.availableForOffers;
    }
}
