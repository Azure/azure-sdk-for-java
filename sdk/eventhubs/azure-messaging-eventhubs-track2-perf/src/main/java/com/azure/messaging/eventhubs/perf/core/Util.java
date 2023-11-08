package com.azure.messaging.eventhubs.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Util {

    public static Mono<Void> preLoadEvents(EventHubProducerAsyncClient client, String partitionId, int totalMessagesToSend, byte[] eventDatabytes) {
        final CreateBatchOptions options = partitionId != null
            ? new CreateBatchOptions().setPartitionId(partitionId)
            : new CreateBatchOptions();

        final AtomicLong eventsToSend = new AtomicLong(totalMessagesToSend);
        final AtomicLong totalEvents = new AtomicLong(0);

        Mono<Void> partitionMono;
        if (CoreUtils.isNullOrEmpty(partitionId)) {
            partitionMono = client.getPartitionIds()
                .flatMap(partId -> client.getPartitionProperties(partId))
                .map(partitionProperties -> {
                    totalEvents.addAndGet(partitionProperties.getLastEnqueuedSequenceNumber() - partitionProperties.getBeginningSequenceNumber());
                    return Mono.empty();
                }).then();
        } else {
            partitionMono = client.getPartitionProperties(partitionId)
                .map(partitionProperties -> {
                    totalEvents.addAndGet(partitionProperties.getLastEnqueuedSequenceNumber() - partitionProperties.getBeginningSequenceNumber());
                    return Mono.empty();
                }).then();
        }
        return partitionMono.then(Mono.defer(() -> {
            if (totalEvents.get() < totalMessagesToSend) {
                eventsToSend.set(totalMessagesToSend - totalEvents.get());
                return client.createBatch(options)
                    .flatMap(batch -> {
                        EventData event = createEvent(eventDatabytes);
                        while (batch.tryAdd(event)) {
                            eventsToSend.getAndDecrement();
                        }
                        return client.send(batch);
                    }).repeat(() -> eventsToSend.get() > 0).then()
                    .doFinally(signal -> System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend));
            } else {
                return Mono.empty();
            }
        }));
    }

    public static EventData createEvent(byte[] eventDataBytes) {
        EventData eventData = new EventData(eventDataBytes);
        return eventData;
    }

    public static String generateString(int targetLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
            .limit(targetLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
        return generatedString;
    }


}
