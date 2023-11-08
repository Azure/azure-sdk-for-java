// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Sample code to demonstrate checkpointing after processing some number of events.
 */
public class EventProcessorClientCheckpointing {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessorClientSample.class);
    private static final int NUMBER_OF_EVENTS_BEFORE_CHECKPOINTING = 200;
    private static final int MAX_BATCH_SIZE = 50;
    private static final Map<String, SamplePartitionProcessor> SAMPLE_PARTITION_PROCESSOR_MAP = new HashMap<>();

    /**
     * @param args The input arguments to this executable.
     *
     * @throws Exception If there are any errors while running the {@link EventProcessorClient}.
     */
    public static void main(String[] args) throws Exception {
        // This error handler logs the error that occurred and keeps the processor running. If the error occurred in
        // a specific partition and had to be closed, the ownership of the partition will be given up and will allow
        // other processors to claim ownership of the partition.
        Consumer<ErrorContext> processError = errorContext -> {
            LOGGER.error("Error while processing {}, {}, {}, {}", errorContext.getPartitionContext().getEventHubName(),
                errorContext.getPartitionContext().getConsumerGroup(),
                errorContext.getPartitionContext().getPartitionId(),
                errorContext.getThrowable().getMessage());
        };

        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Create an EventProcessorClient that processes 50 events in a batch or waits up to a maximum of 30 seconds
        // before processing any available events up to that point. The batch could be empty if no events are received
        // within that 30 seconds window.
        //
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                tokenCredential)
            .processEventBatch(
                batchContext -> onEventBatchReceived(batchContext, NUMBER_OF_EVENTS_BEFORE_CHECKPOINTING),
                MAX_BATCH_SIZE, Duration.ofSeconds(30))
            .processError(processError)
            .checkpointStore(new SampleCheckpointStore())
            .buildEventProcessorClient();

        System.out.println("Starting event processor");
        eventProcessorClient.start();

        // Continue to perform other tasks while the processor is running in the background.
        //
        // eventProcessorClient.start() is a non-blocking call, the program will proceed to the next line of code after
        // setting up and starting the processor.
        Thread.sleep(TimeUnit.MINUTES.toMillis(10));

        System.out.println("Stopping event processor");

        eventProcessorClient.stop();

        System.out.println("Exiting process");
    }

    /**
     * Creates or gets and delegates a {@link SamplePartitionProcessor} to take care of processing and updating the
     * checkpoint if needed.
     *
     * @param batchContext Events to process.
     * @param numberOfEventsBeforeCheckpointing Number of events to process before checkpointing.
     */
    private static void onEventBatchReceived(EventBatchContext batchContext, int numberOfEventsBeforeCheckpointing) {
        final String partitionId = batchContext.getPartitionContext().getPartitionId();

        final SamplePartitionProcessor samplePartitionProcessor = SAMPLE_PARTITION_PROCESSOR_MAP.computeIfAbsent(
            partitionId, key -> new SamplePartitionProcessor(key, numberOfEventsBeforeCheckpointing));

        samplePartitionProcessor.processEventBatch(batchContext);
    }

    /**
     * Class keeps track of the number of events processed for each partition and checkpoints when at least
     * {@code numberOfEventsBeforeCheckpointing} has been processed.
     *
     * In practice, only {@link #processEvent(EventContext)} OR {@link #processEventBatch(EventBatchContext)} will be
     * used.
     */
    private static final class SamplePartitionProcessor {
        private final Logger logger;
        private final String partitionId;
        private final int numberOfEventsBeforeCheckpointing;

        private int eventsProcessed;

        private SamplePartitionProcessor(String partitionId, int numberOfEventsBeforeCheckpointing) {
            this.partitionId = partitionId;
            this.numberOfEventsBeforeCheckpointing = numberOfEventsBeforeCheckpointing;

            final String loggerName = SamplePartitionProcessor.class + partitionId;
            this.logger = LoggerFactory.getLogger(loggerName);
        }

        /**
         * Processes some events and checkpointing after at least {@link #numberOfEventsBeforeCheckpointing} events have
         * been processed.
         *
         * @param eventBatchContext Batch of events to process.
         */
        private void processEventBatch(EventBatchContext eventBatchContext) {

            // There's nothing to process.
            if (eventBatchContext.getEvents().isEmpty()) {
                return;
            }

            final String partitionId = eventBatchContext.getPartitionContext().getPartitionId();

            for (EventData event : eventBatchContext.getEvents()) {
                logger.info("Processing event: Partition id = {}; sequence number = {}; body = {}",
                    partitionId, event.getSequenceNumber(), event.getBodyAsString());
            }

            eventsProcessed = eventsProcessed + eventBatchContext.getEvents().size();

            // We have processed at least the minimum number of events. If so, then checkpoint and reset the counter.
            if (eventsProcessed >= numberOfEventsBeforeCheckpointing) {
                eventBatchContext.updateCheckpoint();

                eventsProcessed = 0;
            }
        }

        /**
         * Processes an event and checkpoints if {@link #numberOfEventsBeforeCheckpointing} events have been processed.
         *
         * @param eventContext Event to process.
         */
        private void processEvent(EventContext eventContext) {

            final EventData event = eventContext.getEventData();

            // It is possible to have no event if maxWaitTime was specified.  This means that no event was received
            // during that interval.
            if (event == null) {
                return;
            }

            logger.info("Processing event: Partition id = {}; sequence number = {}; body = {}",
                partitionId, event.getSequenceNumber(), event.getBodyAsString());

            // We have processed the number of events required. If so, then checkpoint and reset the counter.
            if (++eventsProcessed % numberOfEventsBeforeCheckpointing == 0) {
                eventContext.updateCheckpoint();

                eventsProcessed = 0;
            }
        }
    }
}

