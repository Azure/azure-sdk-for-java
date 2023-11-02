// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import java.util.List;
import java.util.Map;

/**
 * Request options to create a worker.
 * Worker: An entity for jobs to be routed to.
 */
public final class CreateWorkerOptions {
    /**
     * The id property.
     */
    private final String workerId;

    /**
     * The queue(s) that this worker can receive work from.
     */
    private List<String> queues;

    /**
     * The total capacity score this worker has to manage multiple concurrent
     * jobs.
     */
    private final Integer capacity;

    /**
     * A set of key/value pairs that are identifying attributes used by the
     * rules engines to make decisions.
     */
    private Map<String, RouterValue> labels;

    /**
     * A set of non-identifying attributes attached to this worker.
     */
    private Map<String, RouterValue> tags;

    /**
     * The channel(s) this worker can handle and their impact on the workers
     * capacity.
     */
    private List<RouterChannel> channels;

    /**
     * A flag indicating this worker is open to receive offers or not.
     */
    private Boolean availableForOffers;

    /**
     * Constructor for CreateWorkerOptions.
     * @param workerId The id property.
     * @param capacity The total capacity score this worker has to manage multiple concurrent jobs.
     */
    public CreateWorkerOptions(String workerId, Integer capacity) {
        this.workerId = workerId;
        this.capacity = capacity;
    }

    /**
     * Set the queues property: The queue(s) that this worker can receive work from.
     *
     * @param queues the queues value to set.
     * @return this
     */
    public CreateWorkerOptions setQueues(List<String> queues) {
        this.queues = queues;
        return this;
    }

    /**
     * Set the labels property: A set of key/value pairs that are identifying attributes used by the rules engines to
     * make decisions.
     *
     * @param labels the labels value to set.
     * @return this
     */
    public CreateWorkerOptions setLabels(Map<String, RouterValue> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Set the tags property: A set of non-identifying attributes attached to this worker.
     *
     * @param tags the tags value to set.
     * @return this
     */
    public CreateWorkerOptions setTags(Map<String, RouterValue> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Set the channels property: The channel(s) this worker can handle and their impact on the workers
     * capacity.
     *
     * @param channels the channels value to set.
     * @return this
     */
    public CreateWorkerOptions setChannels(List<RouterChannel> channels) {
        this.channels = channels;
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
     * Returns queues.
     * @return queues.
     */
    public List<String> getQueues() {
        return this.queues;
    }

    /**
     * Returns capacity.
     * @return capacity
     */
    public Integer getCapacity() {
        return this.capacity;
    }

    /**
     * Returns labels.
     * @return labels
     */
    public Map<String, RouterValue> getLabels() {
        return this.labels;
    }

    /**
     * Returns tags.
     * @return tags
     */
    public Map<String, RouterValue> getTags() {
        return this.tags;
    }

    /**
     * Returns channels.
     * @return channels
     */
    public List<RouterChannel> getChannels() {
        return this.channels;
    }

    /**
     * Returns availableForOffers.
     * @return availableForOffers
     */
    public Boolean isAvailableForOffers() {
        return this.availableForOffers;
    }
}
