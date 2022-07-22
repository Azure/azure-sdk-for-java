// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Demonstrates state management while processing events with {@link EventProcessorClient}. Shows usage of
 * {@link EventProcessorClientBuilder#processPartitionInitialization(Consumer)},
 * {@link EventProcessorClientBuilder#processError(Consumer)},
 * {@link EventProcessorClientBuilder#processEvent(Consumer)}, and
 * {@link EventProcessorClientBuilder#processPartitionClose(Consumer)}.
 *
 * An in-memory {@link CheckpointStore} is used to store checkpoint information for the sake of this demo. Production
 * environments should leverage a durable store.
 *
 * A manufacturer has several machines on their assembly lines that emit temperature data. The manufacturer can use
 * {@link EventProcessorClient} to aggregate the temperature data to look for anomalies. For example, the temperature
 * data can say if a machine is over heating, or if no temperature data for a machine has been collected for a while, it
 * may be offline.
 *
 * The partition key for each produced event is the name of the machine. This ensures that the temperature data for that
 * machine always gets routed to the same partition. The contents of each event is the temperature of that machine in
 * Celsius.
 *
 * Every 5 seconds, {@link MachineInformation} reports the current average temperature of the machine.
 */
public class EventProcessorClientAggregateEventsSample {
    private static final Duration REPORTING_INTERVAL = Duration.ofSeconds(5);
    private static final String EH_CONNECTION_STRING = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
        + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

    /**
     * Main method to demonstrate starting and stopping a {@link EventProcessorClient}.
     *
     * @param args The input arguments to this executable.
     * @throws Exception If there are any errors while running the {@link EventProcessorClient}.
     */
    public static void main(String[] args) throws Exception {
        final MachineEventsProcessor aggregator = new MachineEventsProcessor(REPORTING_INTERVAL);

        final EventProcessorClient client = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .connectionString(EH_CONNECTION_STRING)
            .processPartitionInitialization(context -> aggregator.onInitialize(context))
            .processPartitionClose(context -> aggregator.onClose(context))
            .processEvent(event -> aggregator.onEvent(event))
            .processError(error -> aggregator.onError(error))
            .checkpointStore(new SampleCheckpointStore())
            .buildEventProcessorClient();

        System.out.println("Starting event processor");
        final AtomicBoolean isRunning = new AtomicBoolean(true);
        client.start();

        // Continue to perform other tasks while the processor is running in the background. In this sample, we are
        // randomly generating fake machine events.
        generateEvents(isRunning).subscribe();

        System.out.println("Sleeping...");
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        isRunning.set(false);

        System.out.println("Stopping event processor");
        client.stop();
        System.out.println("Exiting process");
    }

    /**
     * Helper method that generates events for machines "2A", "9B", and "6C" and sends them to the service.
     */
    private static Mono<Void> generateEvents(AtomicBoolean isRunning) {
        final Logger logger = LoggerFactory.getLogger("Producer");
        final Scheduler scheduler = Schedulers.boundedElastic();
        final Duration operationTimeout = Duration.ofSeconds(5);
        final String[] machineIds = new String[]{"2A", "9B", "6C"};
        final Random random = new Random();
        final EventHubProducerAsyncClient client = new EventHubClientBuilder()
            .connectionString(EH_CONNECTION_STRING)
            .buildAsyncProducerClient();

        return Mono.<Void>fromRunnable(() -> {
            while (isRunning.get()) {
                int milliseconds = random.nextInt(1000);

                try {
                    TimeUnit.MILLISECONDS.sleep(milliseconds);
                } catch (InterruptedException ignored) {
                }

                final String machineId = machineIds[random.nextInt(machineIds.length)];
                // We want a temperature between 0 - 100.
                final int temperature = Math.abs(random.nextInt() % 101);

                logger.info("[{}] Temperature: {}C", machineId, temperature);

                final EventData event = new EventData(String.valueOf(temperature));
                final CreateBatchOptions batchOptions = new CreateBatchOptions().setPartitionKey(machineId);

                client.createBatch(batchOptions).flatMap(batch -> {
                    batch.tryAdd(event);
                    return client.send(batch);
                }).block(operationTimeout);
            }
        }).subscribeOn(scheduler)
            .doFinally(signal -> {
                logger.info("Disposing of producer.");
                client.close();
            });
    }
}

/**
 * Keeps track of machine information by analyzing data in Event Hubs.
 */
class MachineEventsProcessor implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(EventProcessorClientSample.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final Duration reportingInterval;

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

    /**
     * Creates an instance of {@link MachineEventsProcessor}.
     *
     * @param reportingInterval Interval at which to report temperature events.
     */
    MachineEventsProcessor(Duration reportingInterval) {
        this.reportingInterval = reportingInterval;
    }

    /**
     * Processes each event by extracting temperature and machine information from the event.
     *
     * @param eventContext The event received from Event Hubs.
     */
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

        final MachineInformation information = machineInformation.computeIfAbsent(machineId,
            key -> new MachineInformation(key, reportingInterval));

        information.onTemperatureEvent(event.getEnqueuedTime(), temperature);

        // Update checkpoint so customers know
        eventContext.updateCheckpoint();
    }

    /**
     * On initialisation, keeps track of which partitions it is processing.
     *
     * @param initializationContext Initialisation information.
     */
    void onInitialize(InitializationContext initializationContext) {
        final PartitionContext partition = initializationContext.getPartitionContext();
        partitionsProcessing.computeIfAbsent(partition.getPartitionId(), key -> new HashSet<>());
    }

    /**
     * When an occurs, reports that error to a log.
     *
     * @param errorContext Error that occurred while processing events.
     */
    void onError(ErrorContext errorContext) {
        final PartitionContext partition = errorContext.getPartitionContext();

        logger.error("Error occurred processing partition '{}'? {}", partition.getPartitionId(),
            errorContext.getThrowable());
    }

    /**
     * When a partition is lost, will dispose of machine information it is processing. In a real service, it might
     * persist the averages it currently has, so there is no processed data lost.
     *
     * @param closeContext Close context for that partition.
     */
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

    /**
     * Disposes of all the machine information and closes the processor.
     */
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

/**
 * Contains information about a single machine in the factory.
 */
class MachineInformation implements AutoCloseable {
    private final String identifier;
    private final Logger logger = LoggerFactory.getLogger(MachineInformation.class);
    private final AtomicReference<List<Integer>> temperatures = new AtomicReference<>(new ArrayList<>());
    private final ConnectableFlux<AverageTemperature> averageTemperatures;
    private final DirectProcessor<Boolean> onDispose = DirectProcessor.create();
    private final AtomicBoolean isDisposed = new AtomicBoolean();

    private volatile Instant lastReported = Instant.EPOCH;

    /**
     * Creates a new instance.
     *
     * @param identifier Identifier for the machine.
     * @param reportingInterval Interval at which to emit average temperature information.
     */
    MachineInformation(String identifier, Duration reportingInterval) {
        this.identifier = identifier;
        this.averageTemperatures = Flux.interval(reportingInterval)
            .takeUntilOther(onDispose)
            .map(unused -> {
                final Instant timeCalculated = Instant.now();
                final List<Integer> temperaturesInInterval = temperatures.getAndSet(new ArrayList<>());
                if (temperaturesInInterval.size() == 0) {
                    logger.info("[{}]: Average: null", this.identifier);
                    return new AverageTemperature(timeCalculated, null);
                }

                final int sum = temperaturesInInterval.stream().reduce(0, Integer::sum);
                double average = sum / (double) temperaturesInInterval.size();

                logger.info("[{}]: Average: {}", this.identifier, average);
                return new AverageTemperature(timeCalculated, average);
            }).publish();

        averageTemperatures.connect();
    }

    /**
     * Submits additional temperature data for that machine.
     *
     * @param dateEnqueued The time the temperature was acquired.
     * @param temperature The temperature.
     */
    void onTemperatureEvent(Instant dateEnqueued, int temperature) {
        lastReported = dateEnqueued;
        temperatures.getAndUpdate(list -> {
            list.add(temperature);
            return list;
        });
    }

    /**
     * Gets the machine identifier.
     *
     * @return The machine identifier.
     */
    String getIdentifier() {
        return identifier;
    }

    /**
     * A stream of average temperature information for the machine.
     *
     * @return Stream of average temperature information.
     */
    Flux<AverageTemperature> getAverageTemperatures() {
        return averageTemperatures;
    }

    /**
     * Gets the last reported time.
     *
     * @return The last reported time.
     */
    public Instant getLastReported() {
        return lastReported;
    }

    /**
     * Disposes of the information.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        final FluxSink<Boolean> sink = onDispose.sink();
        sink.next(true);
        sink.complete();
    }
}

/**
 * Average temperature calculation.
 */
class AverageTemperature {
    private final Instant timeCalculated;
    private final Double temperature;

    /**
     * Creates an instance.
     *
     * @param timeCalculated The time that the average temperature was calculated.
     * @param temperature The average temperature in Celsius. {@code null} if there was no data during that period.
     */
    AverageTemperature(Instant timeCalculated, Double temperature) {
        this.timeCalculated = timeCalculated;
        this.temperature = temperature;
    }

    /**
     * Gets the time that the measurement was calculated.
     *
     * @return The time that the measurement was calculated.
     */
    public Instant getTimeCalculated() {
        return timeCalculated;
    }

    /**
     * Gets the average temperature in Celsius.
     *
     * @return The average temperature in Celsius, or {@code null} if there was no data when the temperature was
     *     calculated.
     */
    public Double getTemperature() {
        return temperature;
    }
}
