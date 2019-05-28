// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This is a logical representation of receiving from a EventHub partition.
 *
 * <p>
 * A {@link EventReceiver#receive()} is tied to a ConsumerGroup + Event Hubs PartitionId combination.
 *
 * <ul>
 *      <li>If the {@link EventReceiver} is created where {@link ReceiverOptions#epoch()} has a value, then Event
 *      Hubs service will guarantee only 1 active receiver exists per ConsumerGroup + PartitionId combo.  This is the
 *      recommended approach to create a {@link EventReceiver}.</li>
 *      <li>Multiple receivers per ConsumerGroup + Partition combo can be created using non-epoch receivers.</li>
 * </ul>
 *
 * @see EventHubClient#createReceiver(String, EventPosition)
 * @see EventHubClient#createReceiver(String, EventPosition, ReceiverOptions)
 */
public class EventReceiver implements AutoCloseable {

    private PartitionInformation partitionInformation;

    /**
     * Gets the most recent information about a partition by the current receiver.
     *
     * @return If {@link ReceiverOptions}
     */
    public PartitionInformation partitionInformation() {
        return partitionInformation;
    }

    /**
     * Begin receiving events until there are no longer any events emitted specified by
     * {@link EventHubClientBuilder#timeout(Duration)}, are no longer any subscribers, or
     * {@link EventReceiver#close()} is called.
     *
     * @return A stream of events for this receiver.
     */
    public Flux<EventData> receive() {
        final ConnectableFlux<EventData> publish = receiveFromPartition().flatMap(x -> {
            String data = String.format("Something %s", x);
            return Flux.just(new EventData(data.getBytes(UTF_8)));
        }).publish();

        return publish.refCount();
    }

    private Flux<EventData> receiveFromPartition() {
        return Flux.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
    }
}
