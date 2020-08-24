// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;

/**
 * The set of options that can be specified for an {@link EventHubProducerAsyncClient} or {@link EventHubProducerClient}
 * to influence its behavior when publishing directly to an Event Hub partition.
 *
 * These options are ignored when publishing to the Event Hubs gateway for automatic
 * routing or when using a partition key.
 *
 * @see EventHubClientBuilder#enableIdempotentPartitionPublishing()
 *
 */
@Immutable
public final class PartitionPublishingOptions {
    private final Short ownerLevel;
    private final Long producerGroupId;
    private final Integer startingSequenceNumber;

    /**
     * Create a PartitionPublishingOptions with the producer group id, owner level and starting sequence number.
     *
     * @param producerGroupId See {@link #getProducerGroupId()}}
     * @param ownerLevel See {@link #getOwnerLevel()}
     * @param startingSequenceNumber See {@link #getStartingSequenceNumber()}
     */
    public PartitionPublishingOptions(Long producerGroupId, Short ownerLevel, Integer startingSequenceNumber) {
        this.ownerLevel = ownerLevel;
        this.producerGroupId = producerGroupId;
        this.startingSequenceNumber = startingSequenceNumber;
    }

    /**
     * Gets the owner level that indicates a publishing is intended to be performed exclusively for events in the
     * requested partition in the context of the associated producer group. To do so, publishing will attempt to assert ownership
     * over the partition; in the case where more than one publisher in the producer group attempts to assert ownership for the same
     * partition, the one having a larger owner level value will "win".
     *
     * When an owner level is specified, other exclusive publishers which have a lower owner level within the context of the same producer
     * group will either not be allowed to be created or, if they already exist, will encounter an exception during the next attempted operation.  Should
     * there be multiple producers in the producer group with the same owner level, each of them will be able to publish to the partition.
     *
     * Producers with no owner level or which belong to a different producer group are permitted to publish to the associated partition without
     * restriction or awareness of other exclusive producers.
     *
     * The owner level is only recognized and relevant when certain features of the producer are enabled. For example, it is used by
     * idempotent publishing.
     *
     * An {@link com.azure.core.amqp.exception.AmqpException} will occur if an {@link EventHubProducerAsyncClient} or
     * {@link EventHubProducerClient} is unable to publish events to the
     * Event Hub partition for the given producer group id. In this case, the errorCondition of {@link com.azure.core.amqp.exception.AmqpException}
     * will be set to {@link com.azure.core.amqp.exception.AmqpErrorCondition#PRODUCER_DISCONNECTED}.
     *
     * @see EventHubClientBuilder#enableIdempotentPartitionPublishing()
     *
     * @return The relative priority to associate with an exclusive publisher; if {@link null}, the Event Hubs service will control the value.
     */
    public Short getOwnerLevel() {
        return ownerLevel;
    }

    /**
     * Gets the identifier of the producer group that this producer is associated with when publishing to the
     * associated partition. Events will be published in the context of this group.
     *
     * The producer group is only recognized and relevant when certain features of the producer are enabled.
     * For example, it is used by idempotent publishing.
     *
     * @see EventHubClientBuilder#enableIdempotentPartitionPublishing()
     *
     * @return The identifier of the producer group to associate with the partition; if {@link null},
     * the Event Hubs service will control the value.
     */
    public Long getProducerGroupId() {
        return producerGroupId;
    }

    /**
     * Get the starting number that should be used for the automatic sequencing of events for the associated partition, when published by this producer.
     *
     * The starting sequence number is only recognized and relevant when certain features of the producer are enabled.
     * For example, it is used by idempotent publishing.
     *
     * @see EventHubClientBuilder#enableIdempotentPartitionPublishing()
     *
     * @return The starting sequence number to associate with the partition; if {@link null}, the Event Hubs service will control the value.
     */
    public Integer getStartingSequenceNumber() {
        return startingSequenceNumber;
    }
}
