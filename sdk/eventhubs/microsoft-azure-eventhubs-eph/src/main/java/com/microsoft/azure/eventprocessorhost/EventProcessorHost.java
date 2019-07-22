// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * The main class of event processor host.
 */
public final class EventProcessorHost {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(EventProcessorHost.class);
    private static final Object UUID_SYNCHRONIZER = new Object();
    // weOwnExecutor exists to support user-supplied thread pools.
    private final boolean weOwnExecutor;
    private final ScheduledExecutorService executorService;
    private final int executorServicePoolSize = 16;
    private final HostContext hostContext;
    private boolean initializeLeaseManager = false;
    private volatile CompletableFuture<Void> unregistered = null;
    private PartitionManager partitionManager;
    private PartitionManagerOptions partitionManagerOptions = null;

    /**
     * Create a new host instance to process events from an Event Hub.
     * <p>
     * Since Event Hubs are generally used for scale-out, high-traffic scenarios, in most scenarios there will
     * be only one host instances per process, and the processes will be run on separate machines. Besides scale, this also
     * provides isolation: one process or machine crashing will not take out multiple host instances. However, it is
     * supported to run multiple host instances on one machine, or even within one process, for development and testing.
     * <p>
     * The hostName parameter is a name for this event processor host, which must be unique among all event processor host instances
     * receiving from this event hub+consumer group combination: the unique name is used to distinguish which event processor host
     * instance owns the lease for a given partition. An easy way to generate a unique hostName which also includes
     * other information is to call EventProcessorHost.createHostName("mystring").
     * <p>
     * This overload of the constructor uses the built-in lease and checkpoint managers. The
     * Azure Storage account specified by the storageConnectionString parameter is used by the built-in
     * managers to record leases and checkpoints, in the specified container.
     * <p>
     * The Event Hub connection string may be conveniently constructed using the ConnectionStringBuilder class
     * from the Java Event Hub client.
     *
     * @param hostName                 A name for this event processor host. See method notes.
     * @param eventHubPath             Specifies the Event Hub to receive events from.
     * @param consumerGroupName        The name of the consumer group to use when receiving from the Event Hub.
     * @param eventHubConnectionString Connection string for the Event Hub to receive from.
     * @param storageConnectionString  Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName     Azure Storage container name for use by built-in lease and checkpoint manager.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName) {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, storageConnectionString, storageContainerName, (ScheduledExecutorService) null);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * <p>
     * This overload adds an argument to specify a user-provided thread pool. The number of partitions in the
     * target event hub and the number of host instances should be considered when choosing the size of the thread pool:
     * how many partitions is one instance expected to own under normal circumstances? One thread per partition should
     * provide good performance, while being able to support more partitions adequately if a host instance fails and its
     * partitions must be redistributed.
     *
     * @param hostName                 A name for this event processor host. See method notes.
     * @param eventHubPath             Specifies the Event Hub to receive events from.
     * @param consumerGroupName        The name of the consumer group to use when receiving from the Event Hub.
     * @param eventHubConnectionString Connection string for the Event Hub to receive from.
     * @param storageConnectionString  Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName     Azure Storage container name for use by built-in lease and checkpoint manager.
     * @param executorService          User-supplied thread executor, or null to use EventProcessorHost-internal executor.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName,
            final ScheduledExecutorService executorService) {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, storageConnectionString, storageContainerName, (String) null, executorService);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * <p>
     * This overload adds an argument to specify a prefix used by the built-in lease manager when naming blobs in Azure Storage.
     *
     * @param hostName                 A name for this event processor host. See method notes.
     * @param eventHubPath             Specifies the Event Hub to receive events from.
     * @param consumerGroupName        The name of the consumer group to use when receiving from the Event Hub.
     * @param eventHubConnectionString Connection string for the Event Hub to receive from.
     * @param storageConnectionString  Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName     Azure Storage container name for use by built-in lease and checkpoint manager.
     * @param storageBlobPrefix        Prefix used when naming blobs within the storage container.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName,
            final String storageBlobPrefix) {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, storageConnectionString, storageContainerName, storageBlobPrefix,
                (ScheduledExecutorService) null);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * <p>
     * This overload allows the caller to specify both a user-supplied thread pool and
     * a prefix used by the built-in lease manager when naming blobs in Azure Storage.
     *
     * @param hostName                 A name for this event processor host. See method notes.
     * @param eventHubPath             Specifies the Event Hub to receive events from.
     * @param consumerGroupName        The name of the consumer group to use when receiving from the Event Hub.
     * @param eventHubConnectionString Connection string for the Event Hub to receive from.
     * @param storageConnectionString  Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName     Azure Storage container name for use by built-in lease and checkpoint manager.
     * @param storageBlobPrefix        Prefix used when naming blobs within the storage container.
     * @param executorService          User-supplied thread executor, or null to use EventProcessorHost-internal executor.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName,
            final String storageBlobPrefix,
            final ScheduledExecutorService executorService) {
        // Would like to check storageConnectionString and storageContainerName here but can't, because Java doesn't allow statements before
        // calling another constructor. storageBlobPrefix is allowed to be null or empty, doesn't need checking.
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString,
                new AzureStorageCheckpointLeaseManager(storageConnectionString, storageContainerName, storageBlobPrefix), executorService);
        this.initializeLeaseManager = true;
        this.partitionManagerOptions = new AzureStoragePartitionManagerOptions();
    }

    // Because Java won't let you do ANYTHING before calling another constructor. In particular, you can't
    // new up an object and pass it as TWO parameters of the other constructor.
    private EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final AzureStorageCheckpointLeaseManager combinedManager,
            final ScheduledExecutorService executorService) {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, combinedManager, combinedManager, executorService, null);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * <p>
     * This overload allows the caller to provide their own lease and checkpoint managers to replace the built-in
     * ones based on Azure Storage.
     *
     * @param hostName                 A name for this event processor host. See method notes.
     * @param eventHubPath             Specifies the Event Hub to receive events from.
     * @param consumerGroupName        The name of the consumer group to use when receiving from the Event Hub.
     * @param eventHubConnectionString Connection string for the Event Hub to receive from.
     * @param checkpointManager        Implementation of ICheckpointManager, to be replacement checkpoint manager.
     * @param leaseManager             Implementation of ILeaseManager, to be replacement lease manager.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            ICheckpointManager checkpointManager,
            ILeaseManager leaseManager) {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, checkpointManager, leaseManager, null, null);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * <p>
     * This overload allows the caller to provide their own lease and checkpoint managers to replace the built-in
     * ones based on Azure Storage, and to provide an executor service and a retry policy for communications with the event hub.
     *
     * @param hostName                 A name for this event processor host. See method notes.
     * @param eventHubPath             Specifies the Event Hub to receive events from.
     * @param consumerGroupName        The name of the consumer group to use when receiving from the Event Hub.
     * @param eventHubConnectionString Connection string for the Event Hub to receive from.
     * @param checkpointManager        Implementation of ICheckpointManager, to be replacement checkpoint manager.
     * @param leaseManager             Implementation of ILeaseManager, to be replacement lease manager.
     * @param executorService          User-supplied thread executor, or null to use EventProcessorHost-internal executor.
     * @param retryPolicy              Retry policy governing communications with the event hub.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            ICheckpointManager checkpointManager,
            ILeaseManager leaseManager,
            ScheduledExecutorService executorService,
            RetryPolicy retryPolicy) {
        if ((hostName == null) || hostName.isEmpty()) {
            throw new IllegalArgumentException("hostName argument must not be null or empty string");
        }

        // eventHubPath is allowed to be null or empty if it is provided in the connection string. That will be checked later.
        if ((consumerGroupName == null) || consumerGroupName.isEmpty()) {
            throw new IllegalArgumentException("consumerGroupName argument must not be null or empty");
        }

        if ((eventHubConnectionString == null) || eventHubConnectionString.isEmpty()) {
            throw new IllegalArgumentException("eventHubConnectionString argument must not be null or empty");
        }

        // The event hub path must appear in at least one of the eventHubPath argument or the connection string.
        // If it appears in both, then it must be the same in both. If it appears in only one, populate the other.
        ConnectionStringBuilder providedCSB = new ConnectionStringBuilder(eventHubConnectionString);
        String extractedEntityPath = providedCSB.getEventHubName();
        String effectiveEventHubPath = eventHubPath;
        String effectiveEventHubConnectionString = eventHubConnectionString;
        if ((effectiveEventHubPath != null) && !effectiveEventHubPath.isEmpty()) {
            if (extractedEntityPath != null) {
                if (effectiveEventHubPath.compareTo(extractedEntityPath) != 0) {
                    throw new IllegalArgumentException("Provided EventHub path in eventHubPath parameter conflicts with the path in provided EventHub connection string");
                }
                // else they are the same and that's fine
            } else {
                // There is no entity path in the connection string, so put it there.
                ConnectionStringBuilder rebuildCSB = new ConnectionStringBuilder()
                        .setEndpoint(providedCSB.getEndpoint())
                        .setEventHubName(effectiveEventHubPath)
                        .setSasKeyName(providedCSB.getSasKeyName())
                        .setSasKey(providedCSB.getSasKey());
                rebuildCSB.setOperationTimeout(providedCSB.getOperationTimeout());
                effectiveEventHubConnectionString = rebuildCSB.toString();
            }
        } else {
            if ((extractedEntityPath != null) && !extractedEntityPath.isEmpty()) {
                effectiveEventHubPath = extractedEntityPath;
            } else {
                throw new IllegalArgumentException("Provide EventHub entity path in either eventHubPath argument or in eventHubConnectionString");
            }
        }

        if (checkpointManager == null) {
            throw new IllegalArgumentException("Must provide an object which implements ICheckpointManager");
        }
        if (leaseManager == null) {
            throw new IllegalArgumentException("Must provide an object which implements ILeaseManager");
        }

        // executorService argument is allowed to be null, that is the indication to use an internal threadpool.

        // Normally will not be null because we're using the AzureStorage implementation.
        // If it is null, we're using user-supplied implementation. Establish generic defaults
        // in case the user doesn't provide an options object.
        this.partitionManagerOptions = new PartitionManagerOptions();

        if (executorService != null) {
            // User has supplied an ExecutorService, so use that.
            this.weOwnExecutor = false;
            this.executorService = executorService;
        } else {
            this.weOwnExecutor = true;
            this.executorService = Executors.newScheduledThreadPool(
                    this.executorServicePoolSize,
                    new EventProcessorHostThreadPoolFactory(hostName, effectiveEventHubPath, consumerGroupName));
        }

        this.hostContext = new HostContext(this.executorService,
                this, hostName,
                effectiveEventHubPath, consumerGroupName, effectiveEventHubConnectionString, retryPolicy,
                leaseManager, checkpointManager);

        this.partitionManager = new PartitionManager(hostContext);

        TRACE_LOGGER.info(this.hostContext.withHost("New EventProcessorHost created."));
    }

    /**
     * Convenience method for generating unique host names, safe to pass to the EventProcessorHost constructors
     * that take a hostName argument.
     * <p>
     * If a prefix is supplied, the constructed name begins with that string. If the prefix argument is null or
     * an empty string, the constructed name begins with "javahost". Then a dash '-' and a UUID are appended to
     * create a unique name.
     *
     * @param prefix String to use as the beginning of the name. If null or empty, a default is used.
     * @return A unique host name to pass to EventProcessorHost constructors.
     */
    public static String createHostName(String prefix) {
        String usePrefix = prefix;
        if ((usePrefix == null) || usePrefix.isEmpty()) {
            usePrefix = "javahost";
        }
        return usePrefix + "-" + safeCreateUUID();
    }

    /**
     * Synchronized string UUID generation convenience method.
     * <p>
     * We saw null and empty strings returned from UUID.randomUUID().toString() when used from multiple
     * threads and there is no clear answer on the net about whether it is really thread-safe or not.
     * <p>
     * One of the major users of UUIDs is the built-in lease and checkpoint manager, which can be replaced by
     * user implementations. This UUID generation method is public so user implementations can use it as well and
     * avoid the problems.
     *
     * @return A string UUID with dashes but no curly brackets.
     */
    public static String safeCreateUUID() {
        synchronized (EventProcessorHost.UUID_SYNCHRONIZER) {
            final UUID newUuid = UUID.randomUUID();
            return newUuid.toString();
        }
    }

    /**
     * The processor host name is supplied by the user at constructor time, but being able to get
     * it is useful because it means not having to carry both the host object and the name around.
     * As long as you have the host object, you can get the name back, such as for logging.
     *
     * @return The processor host name
     */
    public String getHostName() {
        return this.hostContext.getHostName();
    }

    // TEST USE ONLY
    void setPartitionManager(PartitionManager pm) {
        this.partitionManager = pm;
    }

    HostContext getHostContext() {
        return this.hostContext;
    }

    /**
     * Returns the existing partition manager options object. Unless you are providing implementations of
     * ILeaseManager and ICheckpointMananger, to change partition manager options, call this method to get
     * the existing object and call setters on it to adjust the values.
     *
     * @return the internally-created PartitionManangerObjects object or any replacement object set with setPartitionManangerOptions
     */
    public PartitionManagerOptions getPartitionManagerOptions() {
        return this.partitionManagerOptions;
    }

    /**
     * Set the partition manager options all at once. Normally this method is used only when providing user
     * implementations of ILeaseManager and ICheckpointManager, because it allows passing an object of a class
     * derived from PartitionManagerOptions, which could contain options specific to the user-implemented ILeaseManager
     * or ICheckpointMananger. When using the default, Azure Storage-based implementation, the recommendation is to
     * call getPartitionManangerOptions to return the existing options object, then call setters on that object to
     * adjust the values.
     *
     * @param options - a PartitionManangerOptions object (or derived object) representing the desired options
     */
    public void setPartitionManagerOptions(PartitionManagerOptions options) {
        this.partitionManagerOptions = options;
    }

    /**
     * Register class for event processor and start processing.
     * <p>
     * This overload uses the default event processor factory, which simply creates new instances of
     * the registered event processor class, and uses all the default options.
     * <p>
     * The returned CompletableFuture completes when host initialization is finished. Initialization failures are
     * reported by completing the future with an exception, so it is important to call get() on the future and handle
     * any exceptions thrown.
     * <pre>
     * class MyEventProcessor implements IEventProcessor { ... }
     * EventProcessorHost host = new EventProcessorHost(...);
     * {@literal CompletableFuture<Void>} foo = host.registerEventProcessor(MyEventProcessor.class);
     * foo.get();
     * </pre>
     *
     * @param <T>                Not actually a parameter. Represents the type of your class that implements IEventProcessor.
     * @param eventProcessorType Class that implements IEventProcessor.
     * @return Future that completes when initialization is finished.
     */
    public <T extends IEventProcessor> CompletableFuture<Void> registerEventProcessor(Class<T> eventProcessorType) {
        DefaultEventProcessorFactory<T> defaultFactory = new DefaultEventProcessorFactory<T>();
        defaultFactory.setEventProcessorClass(eventProcessorType);
        return registerEventProcessorFactory(defaultFactory, EventProcessorOptions.getDefaultOptions());
    }

    /**
     * Register class for event processor and start processing.
     * <p>
     * This overload uses the default event processor factory, which simply creates new instances of
     * the registered event processor class, but takes user-specified options.
     * <p>
     * The returned CompletableFuture completes when host initialization is finished. Initialization failures are
     * reported by completing the future with an exception, so it is important to call get() on the future and handle
     * any exceptions thrown.
     *
     * @param <T>                Not actually a parameter. Represents the type of your class that implements IEventProcessor.
     * @param eventProcessorType Class that implements IEventProcessor.
     * @param processorOptions   Options for the processor host and event processor(s).
     * @return Future that completes when initialization is finished.
     */
    public <T extends IEventProcessor> CompletableFuture<Void> registerEventProcessor(Class<T> eventProcessorType, EventProcessorOptions processorOptions) {
        DefaultEventProcessorFactory<T> defaultFactory = new DefaultEventProcessorFactory<T>();
        defaultFactory.setEventProcessorClass(eventProcessorType);
        return registerEventProcessorFactory(defaultFactory, processorOptions);
    }

    /**
     * Register a user-supplied event processor factory and start processing.
     * <p>
     * If creating a new event processor requires more work than just new'ing an objects, the user must
     * create an object that implements IEventProcessorFactory and pass it to this method, instead of calling
     * registerEventProcessor.
     * <p>
     * This overload uses default options for the processor host and event processor(s).
     * <p>
     * The returned CompletableFuture completes when host initialization is finished. Initialization failures are
     * reported by completing the future with an exception, so it is important to call get() on the future and handle
     * any exceptions thrown.
     *
     * @param factory User-supplied event processor factory object.
     * @return Future that completes when initialization is finished.
     */
    public CompletableFuture<Void> registerEventProcessorFactory(IEventProcessorFactory<?> factory) {
        return registerEventProcessorFactory(factory, EventProcessorOptions.getDefaultOptions());
    }

    /**
     * Register user-supplied event processor factory and start processing.
     * <p>
     * This overload takes user-specified options.
     * <p>
     * The returned CompletableFuture completes when host initialization is finished. Initialization failures are
     * reported by completing the future with an exception, so it is important to call get() on the future and handle
     * any exceptions thrown.
     *
     * @param factory          User-supplied event processor factory object.
     * @param processorOptions Options for the processor host and event processor(s).
     * @return Future that completes when initialization is finished.
     */
    public CompletableFuture<Void> registerEventProcessorFactory(IEventProcessorFactory<?> factory, EventProcessorOptions processorOptions) {
        if (this.unregistered != null) {
            throw new IllegalStateException("Register cannot be called on an EventProcessorHost after unregister. Please create a new EventProcessorHost instance.");
        }
        if (this.hostContext.getEventProcessorFactory() != null) {
            throw new IllegalStateException("Register has already been called on this EventProcessorHost");
        }

        this.hostContext.setEventProcessorFactory(factory);
        this.hostContext.setEventProcessorOptions(processorOptions);

        if (this.executorService.isShutdown() || this.executorService.isTerminated()) {
            TRACE_LOGGER.warn(this.hostContext.withHost("Calling registerEventProcessor/Factory after executor service has been shut down."));
            throw new RejectedExecutionException("EventProcessorHost executor service has been shut down");
        }

        if (this.initializeLeaseManager) {
            try {
                ((AzureStorageCheckpointLeaseManager) this.hostContext.getLeaseManager()).initialize(this.hostContext);
            } catch (InvalidKeyException | URISyntaxException | StorageException e) {
                TRACE_LOGGER.error(this.hostContext.withHost("Failure initializing default lease and checkpoint manager."));
                throw new RuntimeException("Failure initializing Storage lease manager", e);
            }
        }

        TRACE_LOGGER.info(this.hostContext.withHost("Starting event processing."));

        return this.partitionManager.initialize();
    }

    /**
     * Stop processing events and shut down this host instance.
     *
     * @return A CompletableFuture that completes when shutdown is finished.
     */
    public CompletableFuture<Void> unregisterEventProcessor() {
        TRACE_LOGGER.info(this.hostContext.withHost("Stopping event processing"));

        if (this.unregistered == null) {
            // PartitionManager is created in constructor. If this object exists, then
            // this.partitionManager is not null.
            this.unregistered = this.partitionManager.stopPartitions();

            // If we own the executor, stop it also.
            // Owned executor is also created in constructor.
            if (this.weOwnExecutor) {
                this.unregistered = this.unregistered.thenRunAsync(() -> {
                    // IMPORTANT: run this last stage in the default threadpool!
                    // If a task running in a threadpool waits for that threadpool to terminate, it's going to wait a long time...

                    // It is OK to call shutdown() here even if threads are still running.
                    // Shutdown() causes the executor to stop accepting new tasks, but existing tasks will
                    // run to completion. The pool will terminate when all existing tasks finish.
                    // By this point all new tasks generated by the shutdown have been submitted.
                    this.executorService.shutdown();

                    try {
                        this.executorService.awaitTermination(10, TimeUnit.MINUTES);
                    } catch (InterruptedException e) {
                        throw new CompletionException(e);
                    }
                }, ForkJoinPool.commonPool());
            }
        }

        return this.unregistered;
    }

    static class EventProcessorHostThreadPoolFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String namePrefix;
        private final String hostName;
        private final String entityName;
        private final String consumerGroupName;

        EventProcessorHostThreadPoolFactory(
                String hostName,
                String entityName,
                String consumerGroupName) {
            this.hostName = hostName;
            this.entityName = entityName;
            this.consumerGroupName = consumerGroupName;
            this.namePrefix = this.getNamePrefix();
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0);
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
            return t;
        }

        private String getNamePrefix() {
            return String.format(Locale.US, "[%s|%s|%s]-%s-",
                    this.entityName, this.consumerGroupName, this.hostName, POOL_NUMBER.getAndIncrement());
        }

        static class ThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                TRACE_LOGGER.warn("Uncaught exception occurred. Thread " + t.getName(), e);
            }
        }
    }
}
