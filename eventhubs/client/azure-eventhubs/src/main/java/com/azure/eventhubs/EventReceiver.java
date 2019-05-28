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
 * A {@link EventReceiver#receive()} is tied to a Event Hub PartitionId + consumer group combination.
 *
 * <ul>
 *      <li>If the {@link EventReceiver} is created where {@link ReceiverOptions#exclusiveReceiverPriority()} has a
 *      value, then Event Hubs service will guarantee only 1 active receiver exists per partitionId and consumer group
 *      combination. This is the recommended approach to create a {@link EventReceiver}.</li>
 *      <li>Multiple receivers per partitionId and consumer group combination can be created using non-epoch receivers.</li>
 * </ul>
 *
 * @see EventHubClient#createReceiver(String)
 * @see EventHubClient#createReceiver(String, ReceiverOptions)
 */
public class EventReceiver {

    private PartitionProperties partitionInformation;

    /**
     * Gets the most recent information about a partition by the current receiver.
     *
     * @return If {@link ReceiverOptions}
     */
    public PartitionProperties partitionInformation() {
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
}
