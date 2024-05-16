// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to:
 *
 * <ul>
 * <li>Send events to specific event hub partition by defining partition id using
 * {@link CreateBatchOptions#setPartitionId(String)}.</li>
 * <li>Set a custom retry policy for Event Hub operations.</li>
 * </ul>
 */
public class PublishEventsToSpecificPartition {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how to send a batch of events with partition id configured.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Set some custom retry options other than the default set.
        AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(30))
            .setMaxRetries(2)
            .setMode(AmqpRetryMode.EXPONENTIAL);

        // Instantiate a client that will be used to call the service.
        //
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                tokenCredential)
            .retryOptions(retryOptions)
            .buildAsyncProducerClient();

        // To send our events, we need to know what partition to send it to. For the sake of this example, we take the
        // first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = producer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // We will publish three events based on simple sentences.
        Flux<EventData> events = Flux.just(
            new EventData("This is the first event.".getBytes(UTF_8)),
            new EventData("This is the second event.".getBytes(UTF_8)),
            new EventData("This is the third event.".getBytes(UTF_8)));

        // Create a batch to send the events.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionId(firstPartition);
        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(options).block());

        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        events.flatMap(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (batch.tryAdd(event)) {
                return Mono.empty();
            }

            // The batch is full, so we create a new batch and send the batch. Mono.when completes when both operations
            // have completed.
            return Mono.when(
                producer.send(batch),
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);

                    // Add that event that we couldn't before.
                    if (!newBatch.tryAdd(event)) {
                        throw Exceptions.propagate(new IllegalArgumentException(String.format(
                            "Event is too large for an empty batch. Max size: %s. Event: %s",
                            newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
                    }

                    return newBatch;
                }));
        }).then()
            .doFinally(signal -> {
                final EventDataBatch batch = currentBatch.getAndSet(null);
                if (batch != null) {
                    producer.send(batch).block(OPERATION_TIMEOUT);
                }
            })
            .subscribe(unused -> System.out.println("Complete"),
                error -> System.out.println("Error sending events: " + error),
                () -> System.out.println("Completed sending events."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        } finally {
            // Disposing of our producer.
            producer.close();
        }
    }
}
