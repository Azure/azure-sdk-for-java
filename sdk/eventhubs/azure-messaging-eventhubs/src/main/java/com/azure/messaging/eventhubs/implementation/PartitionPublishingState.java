// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.PartitionPublishingProperties;

import java.util.concurrent.Semaphore;


/**
 * Store the starting and running state of a partition, which an idempotent producer sends events to.
 */
public final class PartitionPublishingState {
    private Short ownerLevel;
    private Long producerGroupId;
    private Integer sequenceNumber;

    /**
     * Indicate whether the state has ever been retrieved from the link, or it's just a pure client created object.
     * Once the state is retrieved from the link, this value is always true.
     */
    private boolean fromLink = false;
    // Idempotent producer requires all event data batches of a partition are sent out sequentially.
    private final Semaphore semaphore = new Semaphore(1);

    /**
     * Create a PartitionPublishingState with producer group id, owner level and starting sequence number
     * being {@code null}.
     */
    public PartitionPublishingState() {
    }

    public PartitionPublishingState(PartitionPublishingProperties partitionPublishingProperties) {
        this(partitionPublishingProperties.getProducerGroupId(),
            partitionPublishingProperties.getOwnerLevel(), partitionPublishingProperties.getSequenceNumber());
    }

    public PartitionPublishingState(PartitionPublishingState that) {
        this(that.getProducerGroupId(), that.getOwnerLevel(), that.getSequenceNumber());
    }

    /**
     * Create a PartitionPublishingState with the producer group id, owner level and starting sequence number.
     *
     * @param producerGroupId See {@link #getProducerGroupId()}}
     * @param ownerLevel See {@link #getOwnerLevel()}
     * @param sequenceNumber See {@link #getSequenceNumber()} ()}
     */
    public PartitionPublishingState(Long producerGroupId, Short ownerLevel, Integer sequenceNumber) {
        this.ownerLevel = ownerLevel;
        this.producerGroupId = producerGroupId;
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Gets the owner level that indicates a publishing is intended to be performed exclusively for events in the
     * requested partition in the context of the associated producer group. To do so, publishing will attempt to assert
     * ownership over the partition; in the case where more than one publisher in the producer group attempts to assert
     * ownership for the same partition, the one having a larger owner level value will "win".
     *
     * When an owner level is specified, other exclusive publishers which have a lower owner level within the context of
     * the same producer group will either not be allowed to be created or, if they already exist, will encounter an
     * exception during the next attempted operation. Should there be multiple producers in the producer group with the
     * same owner level, each of them will be able to publish to the partition.
     *
     * Producers with no owner level or which belong to a different producer group are permitted to publish to the
     * associated partition without restriction or awareness of other exclusive producers.
     *
     * The owner level is only recognized and relevant when certain features of the producer are enabled. For example,
     * it is used by idempotent publishing.
     *
     * An {@link com.azure.core.amqp.exception.AmqpException} will occur if an {@link EventHubProducerAsyncClient} or
     * {@link EventHubProducerClient} is unable to publish events to the
     * Event Hub partition for the given producer group id. In this case, the errorCondition of
     * {@link com.azure.core.amqp.exception.AmqpException} will be set to
     * {@link com.azure.core.amqp.exception.AmqpErrorCondition#PRODUCER_EPOCH_STOLEN}.
     *
     * @see EventHubClientBuilder#idempotentPartitionPublishing()
     *
     * @return The relative priority to associate with an exclusive publisher; if {@code null},
     * the Event Hubs service will control the value.
     */

    public PartitionPublishingProperties toPartitionPublishingProperties() {
        return new PartitionPublishingProperties(producerGroupId, ownerLevel, sequenceNumber);
    }

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
     * @see EventHubClientBuilder#idempotentPartitionPublishing()
     *
     * @return The identifier of the producer group to associate with the partition; if {@code null},
     * the Event Hubs service will control the value.
     */
    public Long getProducerGroupId() {
        return producerGroupId;
    }

    /**
     * Get the starting number that should be used for the automatic sequencing of events for the associated partition,
     * when published by this producer.
     *
     * The starting sequence number is only recognized and relevant when certain features of the producer are enabled.
     * For example, it is used by idempotent publishing.
     *
     * @see EventHubClientBuilder#enableIdempotentPartitionPublishing() ()
     *
     * @return The starting sequence number to associate with the partition; if {@code null},
     * the Event Hubs service will control the value.
     */
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Set the owner level.
     * @param ownerLevel The owner level of the idempotent producer.
     */
    public void setOwnerLevel(Short ownerLevel) {
        this.ownerLevel = ownerLevel;
    }

    /**
     * Set the producer group id.
     * @param producerGroupId The producer group id of the idempotent producer.
     */
    public void setProducerGroupId(Long producerGroupId) {
        this.producerGroupId = producerGroupId;
    }

    /**
     * Set the sequence number.
     * @param sequenceNumber The next publishing sequence number of a partition when an idempotent producer send
     * an {@link EventData} to.
     */
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }


    public void incrementSequenceNumber(int delta) {
        this.setSequenceNumber(PartitionPublishingUtils.incrementSequenceNumber(this.sequenceNumber, delta));
    }


    /**
     * An idempotent producer must sequentially send events to an EventHubs partition. This {@link Semaphore}
     * is used to send sequentially.
     * @return The {@link Semaphore} used to ensure the send to the partition sequentially.
     */
    public Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * @return whether the state has ever been retrieved from the link, or it's just a pure client created object.
     */
    public boolean isFromLink() {
        return fromLink;
    }

    public void setFromLink(boolean fromLink) {
        this.fromLink = fromLink;
    }

    @Override
    public String toString() {
        return "PartitionPublishingState{"
            + "ownerLevel=" + ownerLevel
            + ", producerGroupId=" + producerGroupId
            + ", sequenceNumber=" + sequenceNumber
            + '}';
    }
}
