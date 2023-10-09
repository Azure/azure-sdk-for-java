package com.azure.android;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static java.nio.charset.StandardCharsets.UTF_8;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.azure.android.eventhubs.EventProcessorClientCheckpointing;
import com.azure.android.eventhubs.SampleCheckpointStore;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubBufferedProducerClient;
import com.azure.messaging.eventhubs.EventHubBufferedProducerClientBuilder;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.EventHubProperties;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import com.azure.messaging.eventhubs.models.SendOptions;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import reactor.core.Disposable;
import reactor.core.publisher.Flux;

@RunWith(AndroidJUnit4.class)
public class EventHubsSampleTests {
    /**
     * The following tests are Event Hubs samples ported directly, with assertions added to ensure
     * that they will fail instrumented tests rather than simply logging errors and passing tests.
     */
    final String eventhubsNamespace= "android-eventhubs.servicebus.windows.net";
    final String eventhubsName = "android-eh-instance";
    final ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
            .clientId(BuildConfig.AZURE_CLIENT_ID)
            .clientSecret(BuildConfig.AZURE_CLIENT_SECRET)
            .tenantId(BuildConfig.AZURE_TENANT_ID)
            .build();

    @Test
    public void ConsumeEventsTest() {
        final Duration OPERATION_TIMEOUT = Duration.ofSeconds(20);
        final int NUMBER_OF_EVENTS = 10;
        CountDownLatch countDownLatch = new CountDownLatch(NUMBER_OF_EVENTS);

        // Create a consumer.
        //
        // The "$Default" consumer group is created by default. This value can be found by going to the Event Hub
        // instance you are connecting to, and selecting the "Consumer groups" page.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
                .credential(eventhubsNamespace, eventhubsName,
                        clientSecretCredential)
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .buildAsyncConsumerClient();
        assertNotNull(consumer.getConsumerGroup());
        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = consumer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);
        // This shouldn't happen, but if we are unable to get the partitions within the timeout period.
        assertNotNull(firstPartition);

        // We start receiving any events that come from `firstPartition`, print out the contents, and decrement the
        // countDownLatch.  EventPosition.latest() tells the service we only want events that are sent to the partition
        // AFTER we begin listening.
        Disposable subscription = consumer.receiveFromPartition(firstPartition, EventPosition.latest())
                .subscribe(partitionEvent -> {
                            EventData event = partitionEvent.getData();
                            PartitionContext partitionContext = partitionEvent.getPartitionContext();

                            String contents = new String(event.getBody(), UTF_8);
                            assertNotNull(partitionContext.getPartitionId());
                            assertNotNull(event.getSequenceNumber());
                            assertNotNull(contents);
                            countDownLatch.countDown();
                        },
                        error -> {
                            fail("Error occurred while consuming events: " + error);

                            // Count down until 0, so the main thread does not keep waiting for events.
                            while (countDownLatch.getCount() > 0) {
                                countDownLatch.countDown();
                            }
                        });

        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
                .credential(eventhubsNamespace, eventhubsName,
                        clientSecretCredential)
                .buildAsyncProducerClient();

        // Because the consumer is only listening to new events, we need to send some events to `firstPartition`.
        // We set the send options to send the events to `firstPartition`.
        SendOptions sendOptions = new SendOptions().setPartitionId(firstPartition);

        // We create 10 events to send to the service and block until the send has completed.
        Flux.range(0, NUMBER_OF_EVENTS).flatMap(number -> {
            String body = String.format("Hello world! Number: %s", number);
            return producer.send(Collections.singletonList(new EventData(body.getBytes(UTF_8))), sendOptions);
        }).blockLast(OPERATION_TIMEOUT);

        try {
            // We wait for all the events to be received before continuing.
            boolean isSuccessful = countDownLatch.await(OPERATION_TIMEOUT.getSeconds(), TimeUnit.SECONDS);
            assertTrue(String.format("Did not complete successfully. There are: %s events left.",
                    countDownLatch.getCount()), isSuccessful);
        } catch (InterruptedException e) {
            fail(e.toString());
        } finally {
            // Dispose and close of all the resources we've created.
            subscription.dispose();
            producer.close();
            consumer.close();
        }
    }

    @Test
    public void PublishEventsWithAzureIdentityTest() {
        List<EventData> telemetryEvents = Arrays.asList(
                new EventData("Roast beef".getBytes(UTF_8)),
                new EventData("Cheese".getBytes(UTF_8)),
                new EventData("Tofu".getBytes(UTF_8)),
                new EventData("Turkey".getBytes(UTF_8)));

        // Create a producer.
        EventHubProducerClient producer = new EventHubClientBuilder()
                .credential(
                        eventhubsNamespace,
                        eventhubsName,
                        clientSecretCredential)
                .buildProducerClient();
        assertNotNull(producer.getIdentifier());

        // Creates an EventDataBatch where the Event Hubs service will automatically load balance the events between all
        // available partitions.
        EventDataBatch currentBatch = producer.createBatch();
        assertNotNull(currentBatch);

        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        for (EventData event : telemetryEvents) {
            if (currentBatch.tryAdd(event)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            producer.send(currentBatch);
            currentBatch = producer.createBatch();

            // Add that event that we couldn't before.
            assertTrue(String.format("Event is too large for an empty batch. Skipping. Max size: %s. Event: %s%n",
                    currentBatch.getMaxSizeInBytes(), event.getBodyAsString()), currentBatch.tryAdd(event));
        }
        producer.send(currentBatch);
    }

    @Test
    public void PublishEventsBufferedProducer() {
        // Create a producer.
        EventHubBufferedProducerClient producer = new EventHubBufferedProducerClientBuilder()
                .credential(
                        eventhubsNamespace,
                        eventhubsName,
                        clientSecretCredential)
                .onSendBatchSucceeded(succeededContext -> onSuccess(succeededContext))
                .onSendBatchFailed(failedContext -> onFailed(failedContext))
                .buildClient();

        // Querying the partition identifiers for the Event Hub. Then calling client.getPartitionProperties with the
        // identifier to get information about each partition.
        final EventHubProperties properties = producer.getEventHubProperties();
        assertNotNull(properties.getName());
        assertNotNull(properties.getCreatedAt());
        assertNotNull(properties.getPartitionIds());

        // Sending a set of events to be distributed to partition 0.
        final List<EventData> events = IntStream.range(0, 10).mapToObj(index -> {
            return new EventData("Event # " + index);
        }).collect(Collectors.toList());

        final SendOptions sendOptions = new SendOptions()
                .setPartitionId("0");

        assertNotNull(sendOptions.getPartitionId());
        producer.enqueueEvents(events, sendOptions);
        producer.close();
    }
    // For PublishEventsBufferedProducer.
    private static void onSuccess(SendBatchSucceededContext succeededContext) {
        final List<EventData> events = StreamSupport.stream(succeededContext.getEvents().spliterator(), false)
                .collect(Collectors.toList());
        assertNotNull(events);
    }

    //For PublishEventsBufferedProducer. Invoked when a batch could not be published to an Event Hub
    private static void onFailed(SendBatchFailedContext failedContext) {
        final List<EventData> events = StreamSupport.stream(failedContext.getEvents().spliterator(), false)
                .collect(Collectors.toList());

        fail(String.format("Failed to publish events to partition '%s'. # of Events: %d.  Error: %s%n",
                failedContext.getPartitionId(), events.size(), failedContext.getThrowable()));
    }

    @Test
    public void EventProcessorClientSampleTest() throws InterruptedException {
        Consumer<EventContext> processEvent = eventContext -> {
            assertNotNull(eventContext.getPartitionContext().getEventHubName());
            assertNotNull(eventContext.getPartitionContext().getConsumerGroup());
            assertNotNull(eventContext.getPartitionContext().getPartitionId());
            assertNotNull(eventContext.getEventData().getSequenceNumber());
            eventContext.updateCheckpoint();
        };

        Consumer<ErrorContext> processError = errorContext -> {
            fail(String.format("Error while processing %s, %s, %s, %s",
                    errorContext.getPartitionContext().getEventHubName(),
                    errorContext.getPartitionContext().getConsumerGroup(),
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable().getMessage()));
        };

        SampleCheckpointStore checkpointStore = new SampleCheckpointStore();
        // Create a processor client.
        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .credential(eventhubsNamespace, eventhubsName,
                        clientSecretCredential)
                .processEvent(processEvent)
                .processError(processError)
                .checkpointStore(checkpointStore);

        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        eventProcessorClient.start();
        eventProcessorClient.start(); // should be a no-op
        assertNotNull(eventProcessorClient.getIdentifier());
        assertTrue(eventProcessorClient.isRunning());
        assertNotNull(checkpointStore.listOwnership(eventhubsNamespace, eventhubsName,
                EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME));

        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));

        eventProcessorClient.stop();
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        assertFalse(eventProcessorClient.isRunning());

        eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        eventProcessorClient.start();
        assertNotNull(eventProcessorClient.getIdentifier());
        assertTrue(eventProcessorClient.isRunning());
        // Continue to perform other tasks while the processor is running in the background.
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        eventProcessorClient.stop();
    }

    // TODO: Add assertions for this
    @Test
    public void EventProcessorClientCheckpointingTest() throws InterruptedException {
        final int NUMBER_OF_EVENTS_BEFORE_CHECKPOINTING = 200;
        final int MAX_BATCH_SIZE = 50;
        Consumer<ErrorContext> processError = errorContext -> {
            fail(String.format("Error while processing %s, %s, %s, %s",
                    errorContext.getPartitionContext().getEventHubName(),
                    errorContext.getPartitionContext().getConsumerGroup(),
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable().getMessage()));
        };

        // Create an EventProcessorClient that processes 50 events in a batch or waits up to a
        // maximum of 30 seconds before processing any available events up to that point. The batch
        // could be empty if no events are received within that 30 seconds window.
        //
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
                .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
                .credential(eventhubsNamespace, eventhubsName,
                        clientSecretCredential)
                .processEventBatch(
                        batchContext -> onEventBatchReceived(batchContext,
                                NUMBER_OF_EVENTS_BEFORE_CHECKPOINTING),
                        MAX_BATCH_SIZE, Duration.ofSeconds(30))
                .processError(processError)
                .checkpointStore(new SampleCheckpointStore())
                .buildEventProcessorClient();

        assertNotNull(eventProcessorClient);
        assertNotNull(eventProcessorClient.getIdentifier());

        eventProcessorClient.start();

        // Continue to perform other tasks while the processor is running in the background.
        //
        // eventProcessorClient.start() is a non-blocking call, the program will proceed to the next
        // line of code after setting up and starting the processor.
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        assertTrue(eventProcessorClient.isRunning());

        eventProcessorClient.stop();
        assertFalse(eventProcessorClient.isRunning());
    }

    /**
     * Creates or gets and delegates a {@link EventProcessorClientCheckpointing.SamplePartitionProcessor}
     * to take care of processing and updating the checkpoint if needed.
     *
     * @param batchContext Events to process.
     * @param numberOfEventsBeforeCheckpointing Number of events to process before checkpointing.
     */
    public static void onEventBatchReceived(EventBatchContext batchContext, int numberOfEventsBeforeCheckpointing) {
        final String partitionId = batchContext.getPartitionContext().getPartitionId();
        final Map<String, EventProcessorClientCheckpointing.SamplePartitionProcessor> SAMPLE_PARTITION_PROCESSOR_MAP = new HashMap<>();

        final EventProcessorClientCheckpointing.SamplePartitionProcessor samplePartitionProcessor = SAMPLE_PARTITION_PROCESSOR_MAP.computeIfAbsent(
                partitionId, key -> new EventProcessorClientCheckpointing.SamplePartitionProcessor(key, numberOfEventsBeforeCheckpointing));
        assertNotNull(samplePartitionProcessor);
        samplePartitionProcessor.processEventBatch(batchContext);
    }
}
