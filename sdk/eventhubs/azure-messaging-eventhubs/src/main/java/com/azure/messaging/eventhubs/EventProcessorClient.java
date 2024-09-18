// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.PartitionProcessor;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import com.azure.messaging.eventhubs.models.ErrorContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * EventProcessorClient provides a convenient mechanism to consume events from all partitions of an Event Hub in the
 * context of a consumer group. Event Processor-based application consists of one or more instances of
 * EventProcessorClient(s) which are set up to consume events from the same Event Hub, consumer group to balance the
 * workload across different instances and track progress when events are processed. Based on the number of instances
 * running, each EventProcessorClient may own zero or more partitions to balance the workload among all the instances.
 *
 * <p><strong>Sample: Construct an {@link com.azure.messaging.eventhubs.EventProcessorClient}</strong></p>
 *
 * <p>The sample below uses an in-memory {@link com.azure.messaging.eventhubs.CheckpointStore} but
 * <a href="https://central.sonatype.com/artifact/com.azure/azure-messaging-eventhubs-checkpointstore-blob">
 *     azure-messaging-eventhubs-checkpointstore-blob</a> provides a checkpoint store backed by Azure Blob Storage.
 * Additionally, {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host name.  It is listed under the
 * "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal.  The credential used is
 * {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and development and
 * chooses the credential to used based on its running environment.  The {@code consumerGroup} is found by navigating
 * to the Event Hub instance, and selecting "Consumer groups" under the "Entities" panel. The {@code consumerGroup} is
 * required.  The credential used is {@code DefaultAzureCredential} because it combines
 * commonly used credentials in deployment and development and chooses the credential to used based on its running
 * environment.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventprocessorclientbuilder.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder&#40;&#41;
 *     .consumerGroup&#40;&quot;&lt;&lt; CONSUMER GROUP NAME &gt;&gt;&quot;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .checkpointStore&#40;new SampleCheckpointStore&#40;&#41;&#41;
 *     .processEvent&#40;eventContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Partition id = %s and sequence number of event = %s%n&quot;,
 *             eventContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
 *             eventContext.getEventData&#40;&#41;.getSequenceNumber&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .processError&#40;errorContext -&gt; &#123;
 *         System.out.printf&#40;&quot;Error occurred in partition processor for partition %s, %s%n&quot;,
 *             errorContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
 *             errorContext.getThrowable&#40;&#41;&#41;;
 *     &#125;&#41;
 *     .buildEventProcessorClient&#40;&#41;;
 * </pre>
 *<!-- end com.azure.messaging.eventhubs.eventprocessorclientbuilder.construct -->
 *
 * @see EventProcessorClientBuilder
 */
@ServiceClient(builder = EventProcessorClientBuilder.class)
public class EventProcessorClient {

    private static final long BASE_JITTER_IN_SECONDS = 2; // the initial delay jitter before starting the processor
    private final ClientLogger logger;

    private final String identifier;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final PartitionPumpManager partitionPumpManager;
    private final PartitionBasedLoadBalancer partitionBasedLoadBalancer;
    private final CheckpointStore checkpointStore;

    private final AtomicReference<ScheduledFuture<?>> runner = new AtomicReference<>();
    private final AtomicReference<ScheduledExecutorService> scheduler = new AtomicReference<>();
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final String consumerGroup;
    private final Duration loadBalancerUpdateInterval;
    private final EventProcessorClientOptions processorClientOptions;

    /**
     * Package-private constructor. Use {@link EventHubClientBuilder} to create an instance.
     *
     * @param eventHubClientBuilder The {@link EventHubClientBuilder}.
     * @param partitionProcessorFactory The factory to create new partition processor(s).
     * @param checkpointStore The store used for reading and updating partition ownership and checkpoints. information.
     * @param processError Error handler for any errors that occur outside the context of a partition.
     */
    EventProcessorClient(EventHubClientBuilder eventHubClientBuilder,
        Supplier<PartitionProcessor> partitionProcessorFactory, CheckpointStore checkpointStore,
        Consumer<ErrorContext> processError, Tracer tracer, EventProcessorClientOptions processorClientOptions) {

        Objects.requireNonNull(eventHubClientBuilder, "eventHubClientBuilder cannot be null.");
        this.processorClientOptions = Objects.requireNonNull(processorClientOptions,
            "processorClientOptions cannot be null.");

        Objects.requireNonNull(processorClientOptions.getConsumerGroup(), "'consumerGroup' cannot be null.");
        Objects.requireNonNull(partitionProcessorFactory, "partitionProcessorFactory cannot be null.");

        final EventHubAsyncClient eventHubAsyncClient = eventHubClientBuilder.buildAsyncClient();

        this.checkpointStore = Objects.requireNonNull(checkpointStore, "checkpointStore cannot be null");
        this.identifier = eventHubAsyncClient.getIdentifier();

        Map<String, Object> loggingContext = new HashMap<>();
        loggingContext.put("eventProcessorId", identifier);

        this.logger = new ClientLogger(EventProcessorClient.class, loggingContext);
        this.fullyQualifiedNamespace = eventHubAsyncClient.getFullyQualifiedNamespace().toLowerCase(Locale.ROOT);
        this.eventHubName = eventHubAsyncClient.getEventHubName().toLowerCase(Locale.ROOT);
        this.consumerGroup = processorClientOptions.getConsumerGroup().toLowerCase(Locale.ROOT);
        this.loadBalancerUpdateInterval = processorClientOptions.getLoadBalancerUpdateInterval();

        final EventHubsTracer eventHubsTracer = new EventHubsTracer(tracer, fullyQualifiedNamespace, eventHubName);
        this.partitionPumpManager = new PartitionPumpManager(checkpointStore, partitionProcessorFactory,
            eventHubClientBuilder, eventHubsTracer, processorClientOptions);

        this.partitionBasedLoadBalancer =
            new PartitionBasedLoadBalancer(this.checkpointStore, eventHubAsyncClient,
                this.fullyQualifiedNamespace, this.eventHubName, this.consumerGroup, this.identifier,
                processorClientOptions.getPartitionOwnershipExpirationInterval().getSeconds(), this.partitionPumpManager,
                processError, processorClientOptions.getLoadBalancingStrategy());
    }

    /**
     * Gets the processor options.
     *
     * @return Gets the processor options set.
     */
    EventProcessorClientOptions getEventProcessorClientOptions() {
        return processorClientOptions;
    }

    /**
     * Gets the consumer group.
     *
     * @return The consumer group.
     */
    String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Gets the Event Hub name.
     *
     * @return the Event Hub name.
     */
    String getEventHubName() {
        return eventHubName;
    }

    /**
     * Gets the fully-qualified namespace.
     *
     * @return The fully-qualified namespace.
     */
    String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * The identifier is a unique name given to this event processor instance.
     *
     * @return Identifier for this event processor.
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Starts processing of events for all partitions of the Event Hub that this event processor can own, assigning a
     * dedicated {@link PartitionProcessor} to each partition. If there are other Event Processors active for the same
     * consumer group on the Event Hub, responsibility for partitions will be shared between them.
     * <p>
     * Subsequent calls to start will be ignored if this event processor is already running. Calling start after {@link
     * #stop()} is called will restart this event processor.
     * </p>
     *
     * <p><strong>Starting the processor to consume events from all partitions</strong></p>
     * <!-- src_embed com.azure.messaging.eventhubs.eventprocessorclient.startstop -->
     * <pre>
     * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
     *
     * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
     * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
     * EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder&#40;&#41;
     *     .consumerGroup&#40;EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME&#41;
     *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
     *         credential&#41;
     *     .processEvent&#40;eventContext -&gt; &#123;
     *         System.out.printf&#40;&quot;Partition id = %s and sequence number of event = %s%n&quot;,
     *             eventContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
     *             eventContext.getEventData&#40;&#41;.getSequenceNumber&#40;&#41;&#41;;
     *     &#125;&#41;
     *     .processError&#40;errorContext -&gt; &#123;
     *         System.out.printf&#40;&quot;Error occurred in partition processor for partition %s, %s%n&quot;,
     *             errorContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
     *             errorContext.getThrowable&#40;&#41;&#41;;
     *     &#125;&#41;
     *     .checkpointStore&#40;new SampleCheckpointStore&#40;&#41;&#41;
     *     .buildEventProcessorClient&#40;&#41;;
     *
     * eventProcessorClient.start&#40;&#41;;
     *
     * &#47;&#47; Continue to perform other tasks while the processor is running in the background.
     * &#47;&#47;
     * &#47;&#47; Finally, stop the processor client when application is finished.
     * eventProcessorClient.stop&#40;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.eventhubs.eventprocessorclient.startstop -->
     */
    public synchronized void start() {
        if (!isRunning.compareAndSet(false, true)) {
            logger.info("Event processor is already running");
            return;
        }
        logger.info("Starting a new event processor instance.");

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        scheduler.set(executor);
        // Add a bit of jitter to initialDelay to minimize contention if multiple EventProcessors start at the same time
        Double jitterInMillis =
            ThreadLocalRandom.current().nextDouble() * TimeUnit.SECONDS.toMillis(BASE_JITTER_IN_SECONDS);

        runner.set(scheduler.get().scheduleWithFixedDelay(partitionBasedLoadBalancer::loadBalance,
            jitterInMillis.longValue(), loadBalancerUpdateInterval.toMillis(), TimeUnit.MILLISECONDS));
    }

    /**
     * Stops processing events for all partitions owned by this event processor. All {@link PartitionProcessor} will be
     * shutdown and any open resources will be closed.
     * <p>
     * Subsequent calls to stop will be ignored if the event processor is not running.
     * </p>
     *
     * <p><strong>Stopping the processor</strong></p>
     * <!-- src_embed com.azure.messaging.eventhubs.eventprocessorclient.startstop -->
     * <pre>
     * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
     *
     * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
     * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
     * EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder&#40;&#41;
     *     .consumerGroup&#40;EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME&#41;
     *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
     *         credential&#41;
     *     .processEvent&#40;eventContext -&gt; &#123;
     *         System.out.printf&#40;&quot;Partition id = %s and sequence number of event = %s%n&quot;,
     *             eventContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
     *             eventContext.getEventData&#40;&#41;.getSequenceNumber&#40;&#41;&#41;;
     *     &#125;&#41;
     *     .processError&#40;errorContext -&gt; &#123;
     *         System.out.printf&#40;&quot;Error occurred in partition processor for partition %s, %s%n&quot;,
     *             errorContext.getPartitionContext&#40;&#41;.getPartitionId&#40;&#41;,
     *             errorContext.getThrowable&#40;&#41;&#41;;
     *     &#125;&#41;
     *     .checkpointStore&#40;new SampleCheckpointStore&#40;&#41;&#41;
     *     .buildEventProcessorClient&#40;&#41;;
     *
     * eventProcessorClient.start&#40;&#41;;
     *
     * &#47;&#47; Continue to perform other tasks while the processor is running in the background.
     * &#47;&#47;
     * &#47;&#47; Finally, stop the processor client when application is finished.
     * eventProcessorClient.stop&#40;&#41;;
     * </pre>
     * <!-- end com.azure.messaging.eventhubs.eventprocessorclient.startstop -->
     */
    public synchronized void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            logger.info("Event processor has already stopped");
            return;
        }
        runner.get().cancel(true);
        scheduler.get().shutdown();
        stopProcessing();
    }

    /**
     * Returns {@code true} if the event processor is running. If the event processor is already running, calling {@link
     * #start()} has no effect.
     *
     * @return {@code true} if the event processor is running.
     */
    public synchronized boolean isRunning() {
        return isRunning.get();
    }

    private void stopProcessing() {
        partitionPumpManager.stopAllPartitionPumps();
        // finally, remove ownerid from checkpointstore as the processor is shutting down
        checkpointStore.listOwnership(fullyQualifiedNamespace, eventHubName, consumerGroup)
            .filter(ownership -> identifier.equals(ownership.getOwnerId()))
            .map(ownership -> ownership.setOwnerId(""))
            .collect(Collectors.toList())
            .flatMapMany(checkpointStore::claimOwnership)
            .blockLast(Duration.ofSeconds(10)); // block until the checkpoint store is updated
    }
}
