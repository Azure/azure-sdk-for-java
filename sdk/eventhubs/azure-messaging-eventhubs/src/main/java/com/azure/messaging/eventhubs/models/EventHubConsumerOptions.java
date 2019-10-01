// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.annotation.Fluent;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncConsumer;
import com.azure.messaging.eventhubs.EventHubClient;
import com.azure.messaging.eventhubs.EventHubConsumer;
import reactor.core.scheduler.Scheduler;

import java.util.Locale;
import java.util.Optional;

/**
 * The baseline set of options that can be specified when creating an {@link EventHubConsumer} or an
 * {@link EventHubAsyncConsumer} to configure its behavior.
 *
 * @see EventHubClient#createConsumer(String, String, EventPosition, EventHubConsumerOptions)
 * @see EventHubAsyncClient#createConsumer(String, String, EventPosition, EventHubConsumerOptions)
 */
@Fluent
public class EventHubConsumerOptions implements Cloneable {
    private final ClientLogger logger = new ClientLogger(EventHubConsumerOptions.class);

    /**
     * The maximum length, in characters, for the identifier assigned to an {@link EventHubAsyncConsumer}.
     */
    public static final int MAXIMUM_IDENTIFIER_LENGTH = 64;
    /**
     * The minimum value allowed for the prefetch count of the consumer.
     */
    public static final int MINIMUM_PREFETCH_COUNT = 1;
    /**
     * The maximum value allowed for the prefetch count of the consumer.
     */
    public static final int MAXIMUM_PREFETCH_COUNT = 8000;

    // Default number of events to fetch when creating the consumer.
    static final int DEFAULT_PREFETCH_COUNT = 500;

    private boolean trackLastEnqueuedEventProperties;
    private String identifier;
    private Long ownerLevel;
    private RetryOptions retry;
    private Scheduler scheduler;
    private int prefetchCount;

    /**
     * Creates a new instance with the default prefetch amount.
     */
    public EventHubConsumerOptions() {
        this.prefetchCount = DEFAULT_PREFETCH_COUNT;
    }

    /**
     * Sets an optional text-based identifier label to assign to an event consumer.
     *
     * @param identifier The receiver name.
     * @return The updated {@link EventHubConsumerOptions} object.
     * @throws IllegalArgumentException if {@code identifier} is greater than {@link
     *     #MAXIMUM_IDENTIFIER_LENGTH}.
     */
    public EventHubConsumerOptions setIdentifier(String identifier) {
        if (!ImplUtils.isNullOrEmpty(identifier) && identifier.length() > MAXIMUM_IDENTIFIER_LENGTH) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "identifier length cannot exceed %s", MAXIMUM_IDENTIFIER_LENGTH)));
        }

        this.identifier = identifier;
        return this;
    }

    /**
     * Sets the {@code ownerLevel} value on this consumer. When populated, the level indicates that a consumer is
     * intended to be the only reader of events for the requested partition and an associated consumer group. To do so,
     * this consumer will attempt to assert ownership over the partition; in the case where more than one exclusive
     * consumer attempts to assert ownership for the same partition/consumer group pair, the one having a larger {@link
     * EventHubConsumerOptions#getOwnerLevel()} value will "win".
     *
     * <p>
     * When an exclusive consumer is used, those consumers which are not exclusive or which have a lower priority will
     * either not be allowed to be created, if they already exist, will encounter an exception during the next attempted
     * operation.
     * </p>
     *
     * @param priority The priority associated with an exclusive consumer; for a non-exclusive consumer, this
     *     value should be {@code null}.
     * @return The updated {@link EventHubConsumerOptions} object.
     * @throws IllegalArgumentException if {@code priority} is not {@code null} and is less than 0.
     */
    public EventHubConsumerOptions setOwnerLevel(Long priority) {
        if (priority != null && priority < 0) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'priority' cannot be a negative value. Please specify a zero or positive long value."));
        }

        this.ownerLevel = priority;
        return this;
    }

    /**
     * Sets the retry policy used to govern retry attempts for receiving events. If not specified, the retry policy
     * configured on the associated {@link EventHubAsyncClient} is used.
     *
     * @param retry The retry policy to use when receiving events.
     * @return The updated {@link EventHubConsumerOptions} object.
     */
    public EventHubConsumerOptions setRetry(RetryOptions retry) {
        this.retry = retry;
        return this;
    }

    /**
     * Sets the count used by the receiver to control the number of events this receiver will actively receive and queue
     * locally without regard to whether a receive operation is currently active.
     *
     * @param prefetchCount The amount of events to queue locally.
     * @return The updated {@link EventHubConsumerOptions} object.
     * @throws IllegalArgumentException if {@code prefetchCount} is less than the {@link
     *     #MINIMUM_PREFETCH_COUNT} or greater than {@link #MAXIMUM_PREFETCH_COUNT}.
     */
    public EventHubConsumerOptions setPrefetchCount(int prefetchCount) {
        if (prefetchCount < MINIMUM_PREFETCH_COUNT) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "PrefetchCount, '%s' has to be above %s", prefetchCount, MINIMUM_PREFETCH_COUNT)));
        }

        if (prefetchCount > MAXIMUM_PREFETCH_COUNT) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "PrefetchCount, '%s', has to be below %s", prefetchCount, MAXIMUM_PREFETCH_COUNT)));
        }

        this.prefetchCount = prefetchCount;
        return this;
    }

    /**
     * Sets the scheduler for receiving events from Event Hubs. If not specified, the scheduler configured with the
     * associated {@link EventHubAsyncClient} is used.
     *
     * @param scheduler The scheduler for receiving events.
     * @return The updated {@link EventHubConsumerOptions} object.
     */
    public EventHubConsumerOptions setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Sets whether or not the consumer should request information on the last enqueued event on its associated
     * partition, and track that information as events are received.
     *
     * <p>When information about the partition's last enqueued event is being tracked, each event received from the
     * Event Hubs service will carry metadata about the partition that it otherwise would not. This results in a small
     * amount of additional network bandwidth consumption that is generally a favorable trade-off when considered
     * against periodically making requests for partition properties using the Event Hub client.</p>
     *
     * @param trackLastEnqueuedEventProperties {@code true} if the resulting consumer will keep track of the last
     *     enqueued information for that partition; {@code false} otherwise.
     *
     * @return The updated {@link EventHubConsumerOptions} object.
     */
    public EventHubConsumerOptions setTrackLastEnqueuedEventProperties(boolean trackLastEnqueuedEventProperties) {
        this.trackLastEnqueuedEventProperties = trackLastEnqueuedEventProperties;
        return this;
    }

    /**
     * Gets the optional text-based identifier label to assign to an event receiver. The identifier is used for
     * informational purposes only. If not specified, the receiver will have no assigned identifier label.
     *
     * @return The identifier of the receiver.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the retry options when receiving events. If not specified, the retry options configured on the associated
     * {@link EventHubAsyncClient} is used.
     *
     * @return The retry options when receiving events.
     */
    public RetryOptions getRetry() {
        return retry;
    }

    /**
     * Gets the owner level for this consumer. If {@link Optional#isPresent()} is {@code false}, then this is not an
     * exclusive consumer. Otherwise, it is an exclusive consumer, and there can only be one active consumer for each
     * partition and consumer group combination.
     *
     * @return An optional owner level for this consumer.
     */
    public Long getOwnerLevel() {
        return ownerLevel;
    }

    /**
     * Gets the scheduler for reading events from Event Hubs. If not specified, the scheduler configured with the
     * associated {@link EventHubAsyncClient} is used.
     *
     * @return The scheduler for reading events.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Gets the count used by the consumer to control the number of events this receiver will actively receive and queue
     * locally without regard to whether a receive operation is currently active.
     *
     * @return The prefetch count receiver will receive and queue locally regardless of whether or not a receive
     *     operation is active.
     */
    public int getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Gets whether or not the consumer should request information on the last enqueued event on its associated
     * partition, and track that information as events are received.
     *
     * @return {@code true} if the resulting consumer will keep track of the last enqueued information for that
     *     partition; {@code false} otherwise.
     */
    public boolean getTrackLastEnqueuedEventProperties() {
        return trackLastEnqueuedEventProperties;
    }

    /**
     * Creates a shallow clone of this instance.
     *
     * @return A shallow clone of this object.
     */
    @Override
    public EventHubConsumerOptions clone() {
        EventHubConsumerOptions clone;
        try {
            clone = (EventHubConsumerOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            clone = new EventHubConsumerOptions();
        }

        clone.setScheduler(this.getScheduler())
            .setIdentifier(this.getIdentifier())
            .setPrefetchCount(this.getPrefetchCount())
            .setOwnerLevel(this.getOwnerLevel());

        if (retry != null) {
            clone.setRetry(retry.clone());
        }

        return clone;
    }
}
