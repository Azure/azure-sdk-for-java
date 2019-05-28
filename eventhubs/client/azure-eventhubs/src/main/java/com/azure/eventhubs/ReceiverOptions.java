// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.http.policy.RetryPolicy;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Options when receiving events from Event Hubs.
 */
public class ReceiverOptions {
    /**
     * The name of the default consumer group in the Event Hubs service.
     */
    public static final String DEFAULT_CONSUMER_GROUP_NAME = "$Default";

    private String name;
    private String consumerGroup;
    private Long epoch;
    private RetryPolicy retryPolicy;
    private boolean keepUpdated;
    private Duration receiveTimeout;
    private Scheduler scheduler;

    /**
     * Creates a new instance with the consumer group set to {@link #DEFAULT_CONSUMER_GROUP_NAME}.
     */
    public ReceiverOptions() {
        consumerGroup = DEFAULT_CONSUMER_GROUP_NAME;
    }

    /**
     * Sets the name of the receiver.
     *
     * @param name The receiver name.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the name of the consumer group.
     *
     * @param consumerGroup The name of the consumer group.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions consumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Sets the epoch value on this receiver. When specified, this becomes an Epoch {@link EventReceiver}.
     * An Epoch receiver guarantees that only one {@link EventReceiver} can listen to each
     * "partition + consumer group" combination.
     *
     * @param epoch The Epoch value for this receiver.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions epoch(long epoch) {
        this.epoch = epoch;
        return this;
    }

    /**
     * Sets the retry policy used to govern retry attempts for receiving events. If not specified, the retry policy
     * configured on the associated {@link EventHubClient} is used.
     *
     * @param retryPolicy The retry policy to use when receiving events.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions retryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        return this;
    }

    /**
     * Sets the timeout to apply when receiving events. If not specified, the timeout configured with the associated
     * {@link EventHubClient} is used.
     *
     * @param duration The timeout when receiving events.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions receiveTimeout(Duration duration) {
        this.receiveTimeout = duration;
        return this;
    }

    /**
     * Sets whether or not the {@link EventReceiver#partitionInformation()} is updated when the receiver reads
     * events.
     *
     * @param keepUpdated {@code true} if the partition information should be kept up-to-date as events are received;
     * otherwise, false.
     * @return The updated ReceiverOptions object.
     */
    public ReceiverOptions keepPartitionInformationUpdated(boolean keepUpdated) {
        this.keepUpdated = keepUpdated;
        return this;
    }

    /**
     * Sets the scheduler for receiving events from Event Hubs. If not specified, the scheduler configured with the
     * associated {@link EventHubClient} is used.
     *
     * @param scheduler The scheduler for receiving events.
     * @return The updated EventHubClientBuilder object.
     */
    public ReceiverOptions scheduler(Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Gets the name of the receiver.
     *
     * @return The name of the receiver.
     */
    String name() {
        return name;
    }

    /**
     * Gets the name of the consumer group.
     *
     * @return The name of the consumer group.
     */
    String consumerGroup() {
        return consumerGroup;
    }

    /**
     * Gets the retry policy when receiving events. If not specified, the retry policy configured on the associated
     * {@link EventHubClient} is used.
     *
     * @return The retry policy when receiving events.
     */
    RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    /**
     * Gets the epoch for this receiver. If {@link Optional#isPresent()} is {@code false}, then this is not an epoch
     * receiver. Otherwise, it is and there can only be one receiver per (partition + consumer group) combination.
     *
     * @return An optional epoch for this receiver.
     */
    Optional<Long> epoch() {
        return Optional.ofNullable(epoch);
    }

    /**
     * Gets whether or not the {@link EventReceiver#partitionInformation()} is updated when the receiver reads
     * events.
     *
     * @return {@code true} if the partition information should be kept up-to-date as events are received; otherwise,
     * false.
     */
    boolean keepPartitionInformationUpdated() {
        return this.keepUpdated;
    }

    /**
     * Gets the timeout to apply when receiving events. If not specified, the timeout configured with the associated
     * {@link EventHubClient} is used.
     *
     * @return The timeout when receiving events.
     */
    Duration receiveTimeout() {
        return this.receiveTimeout;
    }

    /**
     * Gets the scheduler for receiving events from Event Hubs. If not specified, the scheduler configured with the
     * associated {@link EventHubClient} is used.
     *
     * @return The scheduler for receiving events.
     */
    Scheduler scheduler() {
        return scheduler;
    }
}
