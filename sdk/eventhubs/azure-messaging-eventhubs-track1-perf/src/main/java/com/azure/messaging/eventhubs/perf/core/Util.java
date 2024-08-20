package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.PartitionSender;
import com.microsoft.azure.eventhubs.PayloadSizeExceededException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class Util {

    public static Mono<Void> preLoadEvents(EventHubClient client, String partitionId, byte[] eventDataBytes,
                                           int totalMessagesToSend) {
        final AtomicLong eventsToSend = new AtomicLong(totalMessagesToSend);
        final AtomicLong totalEvents = new AtomicLong(0);

        Mono<Void> partitionMono;
        if (CoreUtils.isNullOrEmpty(partitionId)) {
            partitionMono = Mono.fromFuture(client.getRuntimeInformation())
                .flatMap(eventHubRuntimeInformation -> {
                    String[] partitionIds = eventHubRuntimeInformation.getPartitionIds();
                    return Flux.fromArray(partitionIds)
                        .map(partId -> Mono.fromFuture(client.getPartitionRuntimeInformation(partId))
                            .map(partitionRuntimeInformation -> {
                                totalEvents.addAndGet(partitionRuntimeInformation.getLastEnqueuedSequenceNumber()
                                    - partitionRuntimeInformation.getBeginSequenceNumber());
                                return Mono.empty();
                            })).then();
                }).then();
        } else {
            partitionMono = Mono.fromFuture(client.getPartitionRuntimeInformation(partitionId))
                .flatMap(partitionProperties -> {
                    totalEvents.addAndGet(partitionProperties.getLastEnqueuedSequenceNumber()
                        - partitionProperties.getBeginSequenceNumber());
                    return Mono.empty();
                }).then();
        }

        if (!CoreUtils.isNullOrEmpty(partitionId)) {
            CompletableFuture<PartitionSender> createSenderFuture;
            try {
                createSenderFuture = client.createPartitionSender(partitionId);
            } catch (EventHubException e) {
                createSenderFuture = new CompletableFuture<>();
                createSenderFuture.completeExceptionally(
                    new RuntimeException("Unable to create partition sender: " + partitionId, e));
            }

            return partitionMono.then(Mono.usingWhen(
                Mono.fromCompletionStage(createSenderFuture),
                sender -> {
                    EventDataBatch currentBatch;

                    if (totalEvents.get() < eventsToSend.get()) {
                        eventsToSend.set(eventsToSend.get() - totalEvents.get());
                        while (eventsToSend.get() > 0) {
                            currentBatch = sender.createBatch();

                            EventData event = createEvent(eventDataBytes);
                            try {
                                while (currentBatch.tryAdd(event)) {
                                    eventsToSend.getAndDecrement();
                                }
                            } catch (PayloadSizeExceededException e) {
                                return Mono.error(new RuntimeException("Event was too large for a single batch.", e));
                            }
                            try {
                                sender.sendSync(currentBatch);
                            } catch (EventHubException e) {
                                return Mono.error(new RuntimeException("Could not send batch. Error: " + e));
                            }
                        }
                        System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend);
                        return Mono.empty();
                    } else {
                        return Mono.empty();
                    }
                },
                sender -> Mono.fromCompletionStage(sender.close())));
        } else {
            return partitionMono.then(Mono.defer(() -> {
                EventDataBatch currentBatch;

                if (totalEvents.get() < eventsToSend.get()) {
                    eventsToSend.set(eventsToSend.get() - totalEvents.get());
                    while (eventsToSend.get() > 0) {
                        try {
                            currentBatch = client.createBatch();
                        } catch (EventHubException e) {
                            throw new RuntimeException("Error creating Batch", e);
                        }

                        EventData event = createEvent(eventDataBytes);
                        try {
                            while (currentBatch.tryAdd(event)) {
                                eventsToSend.getAndDecrement();
                            }
                        } catch (PayloadSizeExceededException e) {
                            throw new RuntimeException("Event was too large for a single batch.", e);
                        }
                        try {
                            client.sendSync(currentBatch);
                        } catch (EventHubException e) {
                            throw new RuntimeException("Could not send batch. Error: " + e);
                        }
                    }
                    System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend);
                    return Mono.empty();
                } else {
                    return Mono.empty();
                }
            }));
        }
    }

    public static EventData createEvent(byte[] eventDataBytes) {
        return EventData.create(eventDataBytes);
    }

    public static String generateString(int targetLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
            .limit(targetLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
