// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Demonstrates how to use manage state using {@link EventProcessorClient}. Also shows usage of
 * {@link EventProcessorClientBuilder#processPartitionInitialization(Consumer)},
 * {@link EventProcessorClientBuilder#processError(Consumer)},
 * {@link EventProcessorClientBuilder#processEvent(Consumer)}, and
 * {@link EventProcessorClientBuilder#processPartitionClose(Consumer)}.
 *
 * A manufacturer has several machines on their assembly lines that emit temperature data. The manufacturer can use
 * {@link EventProcessorClient} to aggregate the temperature data to look for anomalies. For example, the temperature
 * data can say if a machine is over heating, or if no data for a machine has been emitted in awhile, it may be offline.
 *
 * The partition key for each produced event is the name of the machine. This ensures that the temperature data for that
 * machine always gets routed to the same partition.
 *
 * The contents of each event is the temperature of that machine in Celsius. A temperature range of 30C - 60C is normal.
 */
public class EventProcessorClientWithInitialization {
    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName"
        + "};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

    /**
     * Main method to demonstrate starting and stopping a {@link EventProcessorClient}.
     *
     * @param args The input arguments to this executable.
     *
     * @throws Exception If there are any errors while running the {@link EventProcessorClient}.
     */
    public static void main(String[] args) throws Exception {
        final MachineEventsProcessor aggregator = new MachineEventsProcessor();

        final EventProcessorClient client = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(EH_CONNECTION_STRING)
            .processPartitionInitialization(context -> aggregator.onInitialize(context))
            .processPartitionClose(context -> aggregator.onClose(context))
            .processEvent(event -> aggregator.onEvent(event))
            .processError(error -> aggregator.onError(error))
            .checkpointStore(new InMemoryCheckpointStore())
            .buildEventProcessorClient();

        System.out.println("Starting event processor");
        client.start();

        // Continue to perform other tasks while the processor is running in the background.
        TimeUnit.MINUTES.sleep(1);

        System.out.println("Stopping event processor");
        client.stop();
        System.out.println("Exiting process");
    }
}

class MachineEventsProcessor implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(EventProcessorClientSample.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();

    /**
     * Holds information about what machines this instance is processing temperature data for.
     * Key: Machine id.
     * Value: Temperature information for that machine.
     */
    private final ConcurrentHashMap<String, MachineInformation> machineInformation = new ConcurrentHashMap<>();

    /**
     * Holds information about what partitions this instance is processing and its associated machines.
     * Key: Partition id.
     * Value: List of machine ids in that partition.
     */
    private final ConcurrentHashMap<String, Set<String>> partitionsProcessing = new ConcurrentHashMap<>();

    void onEvent(EventContext eventContext) {
        final PartitionContext partitionContext = eventContext.getPartitionContext();
        final EventData event = eventContext.getEventData();
        final String contents = event.getBodyAsString();
        final int temperature;
        try {
            temperature = Integer.parseInt(contents);
        } catch (NumberFormatException ex) {
            logger.warn("Unable to parse temperature data. Partition: #{}. Seq #{}. Contents: #{}. Error: {}",
                partitionContext.getPartitionId(), event.getSequenceNumber(), contents, ex);
            return;
        }

        final String machineId = event.getPartitionKey();
        if (machineId == null || machineId.isEmpty()) {
            logger.warn("PartitionKey is not set on event. Partition: #{}. Seq #{}. Contents: #{}.",
                partitionContext.getPartitionId(), event.getSequenceNumber(), contents);
            return;
        }

        partitionsProcessing.compute(partitionContext.getPartitionId(), (key, value) -> {
            if (value == null) {
                value = new HashSet<>();
            }
            value.add(machineId);
            return value;
        });

        final MachineInformation information = machineInformation.computeIfAbsent(machineId, MachineInformation::new);
        information.onTemperatureEvent(event.getEnqueuedTime(), temperature);

        // Update checkpoint so customers know
        eventContext.updateCheckpoint();
    }

    void onInitialize(InitializationContext initializationContext) {
        final PartitionContext partition = initializationContext.getPartitionContext();
        partitionsProcessing.computeIfAbsent(partition.getPartitionId(), key -> new HashSet<>());
    }

    void onError(ErrorContext errorContext) {
        final PartitionContext partition = errorContext.getPartitionContext();

        logger.error("Error occurred processing partition '{}'? {}", partition.getPartitionId(),
            errorContext.getThrowable());
    }

    void onClose(CloseContext closeContext) {
        final PartitionContext partition = closeContext.getPartitionContext();
        final Set<String> machineIds = partitionsProcessing.remove(partition.getPartitionId());

        logger.info("Stopped processing partition '{}'. Reason: '{}'", partition.getPartitionId(),
            closeContext.getCloseReason());

        for (String id : machineIds) {
            final MachineInformation information = machineInformation.remove(id);
            if (information == null) {
                continue;
            }

            // We may want to persist the last calculated average temperature to a durable store, so another process can
            // continue from where this instance left off.
            logger.info("Stopped processing temperatures for machine: {}", information.getIdentifier());
            information.close();
        }
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        partitionsProcessing.clear();
        machineInformation.forEach((key, value) -> value.close());
        machineInformation.clear();
    }
}

class MachineInformation implements AutoCloseable {
    private final String identifier;
    private final DirectProcessor<AverageTemperature> averageTemperature = DirectProcessor.create();
    private final FluxSink<AverageTemperature> sink = averageTemperature.sink(FluxSink.OverflowStrategy.BUFFER);

    MachineInformation(String identifier) {
        this.identifier = identifier;
    }

    void onTemperatureEvent(Instant dateEnqueued, int temperature) {

    }

    String getIdentifier() {
        return identifier;
    }

    Flux<AverageTemperature> getAverageTemperatures() {
        return averageTemperature;
    }

    @Override
    public void close() {
        sink.complete();
        averageTemperature.dispose();
    }
}

class AverageTemperature {
    private final Instant timeCalculated;
    private final Double temperature;

    AverageTemperature(Instant timeCalculated, Double temperature) {
        this.timeCalculated = timeCalculated;
        this.temperature = temperature;
    }

    public Instant getTimeCalculated() {
        return timeCalculated;
    }

    public Double getTemperature() {
        return temperature;
    }
}
