// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.implementation.util.ImplUtils;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Options when receiving events from Event Hubs.
 */
public class EventReceiverOptions implements Cloneable {
    /**
     * The name of the default consumer group in the Event Hubs service.
     */
    public static final String DEFAULT_CONSUMER_GROUP_NAME = "$Default";

    // The maximum length, in characters, for the identifier assigned to a receiver.
    private static final int MAXIMUM_IDENTIFIER_LENGTH = 64;
    // The minimum value allowed for the prefetch count of the receiver.
    private static final int MINIMUM_PREFETCH_COUNT = 1;
    private static final int DEFAULT_PREFETCH_COUNT = 500;
    private static final int MAXIMUM_PREFETCH_COUNT = 8000;

    private String identifier;
    private String consumerGroup;
    private Long priority;
    private Retry retryPolicy;
    private boolean keepUpdated;
    private Scheduler scheduler;
    private EventPosition beginReceivingAt;
    private int prefetchCount;

    /**
     * Creates a new instance with a position at the beginning of the partition stream, consumer group set to
     * {@link #DEFAULT_CONSUMER_GROUP_NAME},and the default prefetch amount.
     */
    public EventReceiverOptions() {
        this.consumerGroup = DEFAULT_CONSUMER_GROUP_NAME;
        this.prefetchCount = DEFAULT_PREFETCH_COUNT;
        this.beginReceivingAt = EventPosition.firstAvailableEvent();
    }

    /**
     * Sets an optional text-based identifier label to assign to an event receiver.
     *
     * @param identifier The receiver name.
     * @return The updated ReceiverOptions object.
     */
    public EventReceiverOptions identifier(String identifier) {
        validateIdentifier(identifier);
        this.identifier = identifier;
        return this;
    }

    /**
     * The position within the partition where the receiver should begin reading events.
     *
     * <p>
     * If not specified, the receiver will begin receiving all events that are contained in the partition, starting with
     * the first event that was enqueued and will continue receiving until there are no more events observed.
     *
     * @param position Position within the partition where the receiver should begin reading events.
     * @return The updated ReceiverOptions object.
     */
    public EventReceiverOptions beginReceivingAt(EventPosition position) {
        this.beginReceivingAt = position;
        return this;
    }

    /**
     * Sets the name of the consumer group.
     *
     * @param consumerGroup The name of the consumer group.
     * @return The updated ReceiverOptions object.
     */
    public EventReceiverOptions consumerGroup(String consumerGroup) {
        if (ImplUtils.isNullOrEmpty(consumerGroup)) {
            throw new IllegalArgumentException("'consumerGroup' cannot be null or empty.");
        }

        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Sets the exclusiveReceiverPriority value on this receiver. When populated, the priority indicates that a receiver is intended to be
     * the only reader of events for the requested partition and an associated consumer group.
     * To do so, this receiver will attempt to assert ownership over the partition; in the case where more than one
     * exclusive receiver attempts to assert ownership for the same partition/consumer group pair, the one having a
     * larger {@link EventReceiverOptions#exclusiveReceiverPriority()} value will "win".
     *
     * <p>
     * When an exclusive receiver is used, those receivers which are not exclusive or which have a lower priority will
     * either not be allowed to be created, if they already exist, will encounter an exception during the next attempted
     * operation.
     * </p>
     *
     * @param priority The priority associated with an exclusive receiver; for a non-exclusive receiver, this value
     * should be {@code null}.
     * @return The updated ReceiverOptions object.
     * @throws IllegalArgumentException if {@code priority} is not {@code null} and is less than 0.
     */
    public EventReceiverOptions exclusiveReceiverPriority(Long priority) {
        if (priority != null && priority < 0) {
            throw new IllegalArgumentException("'priority' cannot be a negative value. Please specify a zero or positive long value.");
        }

        this.priority = priority;
        return this;
    }

    /**
     * Sets the retry policy used to govern retry attempts for receiving events. If not specified, the retry policy
     * configured on the associated {@link EventHubClient} is used.
     *
     * @param retry The retry policy to use when receiving events.
     * @return The updated ReceiverOptions object.
     */
    public EventReceiverOptions retry(Retry retry) {
        this.retryPolicy = retry;
        return this;
    }

    /**
     * Sets the count used by the receiver to control the number of events this receiver will actively receive and queue
     * locally without regard to whether a receive operation is currently active.
     *
     * @param prefetchCount The amount of events to queue locally.
     * @return The updated ReceiverOptions object.
     * @throws IllegalArgumentException if {@code prefetchCount} is less than the {@link #MINIMUM_PREFETCH_COUNT} or
     * greater than {@link #MAXIMUM_PREFETCH_COUNT}.
     */
    public EventReceiverOptions prefetchCount(int prefetchCount) {
        if (prefetchCount < MINIMUM_PREFETCH_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "PrefetchCount, '%s' has to be above %s", prefetchCount, MINIMUM_PREFETCH_COUNT));
        }

        if (prefetchCount > MAXIMUM_PREFETCH_COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "PrefetchCount, '%s', has to be below %s", prefetchCount, MAXIMUM_PREFETCH_COUNT));
        }

        this.prefetchCount = prefetchCount;
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
    public EventReceiverOptions keepPartitionInformationUpdated(boolean keepUpdated) {
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
    public EventReceiverOptions scheduler(Scheduler scheduler) {
        Objects.requireNonNull(scheduler);
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Gets the optional text-based identifier label to assign to an event receiver.
     * The identifier is used for informational purposes only.  If not specified, the receiver will have no assigned
     * identifier label.
     *
     * @return The identifier of the receiver.
     */
    public String identifier() {
        return identifier;
    }

    /**
     * Gets the position within the partition where the receiver should begin reading events.
     *
     * @return The position within the partition where the receiver should begin reading events.
     */
    public EventPosition beginReceivingAt() {
        return this.beginReceivingAt;
    }

    /**
     * Gets the name of the consumer group.
     *
     * @return The name of the consumer group.
     */
    public String consumerGroup() {
        return consumerGroup;
    }

    /**
     * Gets the retry policy when receiving events. If not specified, the retry policy configured on the associated
     * {@link EventHubClient} is used.
     *
     * @return The retry policy when receiving events.
     */
    public Retry retry() {
        return retryPolicy;
    }

    /**
     * Gets the priority for this receiver. If {@link Optional#isPresent()} is {@code false}, then this is not an
     * exclusive receiver. Otherwise, it is and there can only be one receiver per (partition + consumer group)
     * combination.
     *
     * @return An optional priority for this receiver.
     */
    public Optional<Long> exclusiveReceiverPriority() {
        return Optional.ofNullable(priority);
    }

    /**
     * Gets whether or not the {@link EventReceiver#partitionInformation()} is updated when the receiver reads
     * events.
     *
     * @return {@code true} if the partition information should be kept up-to-date as events are received; otherwise,
     * false.
     */
    public boolean keepPartitionInformationUpdated() {
        return this.keepUpdated;
    }

    /**
     * Gets the scheduler for receiving events from Event Hubs. If not specified, the scheduler configured with the
     * associated {@link EventHubClient} is used.
     *
     * @return The scheduler for receiving events.
     */
    public Scheduler scheduler() {
        return scheduler;
    }

    /**
     * Gets the count used by the receiver to control the number of events this receiver will actively receive and queue
     * locally without regard to whether a receive operation is currently active.
     *
     * @return The prefetch count receiver will receive and queue locally regardless of whether or not a receive
     * operation is active.
     */
    public int prefetchCount() {
        return prefetchCount;
    }

    private void validateIdentifier(String identifier) {
        if (ImplUtils.isNullOrEmpty(identifier) && identifier.length() > MAXIMUM_IDENTIFIER_LENGTH) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "identifier length cannot exceed %s", MAXIMUM_IDENTIFIER_LENGTH));
        }
    }

    /**
     * Creates a shallow clone of this instance.
     *
     * The object is cloned, but this instance's fields are not cloned. {@link Duration} and {@link String} are
     * immutable objects and are not an issue. The implementation of {@link Retry} could be mutable. In addition, the
     * {@link #scheduler()} set is not cloned.
     *
     * @return A shallow clone of this object.
     */
    public EventReceiverOptions clone() {
        EventReceiverOptions clone;
        try {
            clone = (EventReceiverOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new EventReceiverOptions();
        }

        clone.beginReceivingAt(this.beginReceivingAt());
        clone.scheduler(this.scheduler());
        clone.consumerGroup(this.consumerGroup());
        clone.identifier(this.identifier());
        clone.prefetchCount(this.prefetchCount());
        clone.keepPartitionInformationUpdated(this.keepPartitionInformationUpdated());
        clone.retry(this.retry());

        Optional<Long> priority = this.exclusiveReceiverPriority();
        if (priority.isPresent()) {
            clone.exclusiveReceiverPriority(priority.get());
        }

        return clone;
    }
}
