// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.perf;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class that tests Event Hubs.
 */
abstract class ServiceTest<T extends EventHubsOptions> extends PerfStressTest<T> {
    protected final List<EventData> events;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    ServiceTest(T options) {
        super(options);

        final InputStream randomInputStream = TestDataCreationHelper.createRandomInputStream(options.getSize());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] eventBytes;
        try {
            int bytesRead;
            final byte[] data = new byte[4096];

            while ((bytesRead = randomInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            eventBytes = buffer.toByteArray();
        } catch (IOException e) {
            System.err.println("Unable to read input bytes." + e);
            final int size = Long.valueOf(options.getSize()).intValue();
            eventBytes = new byte[size];
            Arrays.fill(eventBytes, Integer.valueOf(95).byteValue());
        } finally {
            try {
                buffer.close();
            } catch (IOException e) {
                System.err.println("Unable to close bytebuffer. Error:" + e);
            }
        }

        final ArrayList<EventData> eventsList = new ArrayList<>();
        for (int number = 0; number < options.getCount(); number++) {
            final EventData eventData = new EventData(eventBytes);
            eventData.getProperties().put("index", number);
            eventsList.add(eventData);
        }

        this.events = Collections.unmodifiableList(eventsList);
    }

    void addEvents(EventDataBatch batch, int numberOfMessages) {
        for (int i = 0; i < numberOfMessages; i++) {
            final int index = numberOfMessages % events.size();
            final EventData event = events.get(index);

            try {
                if (!batch.tryAdd(event)) {
                    System.out.printf("Only added %s of %s events.%n", i, numberOfMessages);
                    break;
                }
            } catch (AmqpException e) {
                throw new RuntimeException("Event was too large for a single batch.", e);
            }
        }
    }

    /**
     * Creates a new instance of {@link EventHubClientBuilder}.
     *
     * @return An {@link EventHubClientBuilder}.
     */
    EventHubClientBuilder createEventHubClientBuilder() {
        final EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString(options.getConnectionString(), options.getEventHubName());

        if (options.getTransportType() != null) {
            builder.transportType(options.getTransportType());
        }

        return builder;
    }

    Mono<Void> sendMessages(EventHubProducerAsyncClient client, String partitionId, int totalMessagesToSend) {
        final CreateBatchOptions options = partitionId != null
            ? new CreateBatchOptions().setPartitionId(partitionId)
            : new CreateBatchOptions();

        final AtomicInteger number = new AtomicInteger(totalMessagesToSend);
        return Mono.defer(() -> client.createBatch(options)
            .flatMap(batch -> {
                EventData event = events.get(0);
                while (batch.tryAdd(event)) {
                    final int index = number.getAndDecrement() % events.size();
                    if (index < 0) {
                        break;
                    }

                    event = events.get(index);
                }

                return client.send(batch);
            }))
            .repeat(() -> number.get() > 0)
            .then()
            .doFinally(signal ->
                System.out.printf("%s: Sent %d messages.%n", partitionId, totalMessagesToSend));
    }
}
