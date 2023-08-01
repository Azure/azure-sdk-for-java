// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider;
import com.microsoft.azure.eventhubs.AzureActiveDirectoryTokenProvider.AuthenticationCallback;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventHubClientOptions;
import com.microsoft.azure.eventhubs.ITokenProvider;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.eventhubs.TransportType;
import com.microsoft.azure.eventhubs.impl.StringUtil;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
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

    private EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final EventHubClientFactory eventHubClientFactory,
            ICheckpointManager checkpointManager,
            ILeaseManager leaseManager,
            final boolean initializeLeaseManager,
            ScheduledExecutorService executorService) {
        this.initializeLeaseManager = initializeLeaseManager;
        if (this.initializeLeaseManager) {
            this.partitionManagerOptions = new AzureStoragePartitionManagerOptions();
        } else {
            // Using user-supplied implementation.
            // Establish generic defaults in case the user doesn't provide an options object.
            this.partitionManagerOptions = new PartitionManagerOptions();
        }

        if (executorService != null) {
            // User has supplied an ExecutorService, so use that.
            this.weOwnExecutor = false;
            this.executorService = executorService;
        } else {
            this.weOwnExecutor = true;
            this.executorService = Executors.newScheduledThreadPool(
                    this.executorServicePoolSize,
                    new EventProcessorHostThreadPoolFactory(hostName, eventHubPath, consumerGroupName));
        }
        eventHubClientFactory.setExecutor(this.executorService);

        this.hostContext = new HostContext(this.executorService,
                this, hostName,
                eventHubPath, consumerGroupName, eventHubClientFactory,
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

    /**
     * Builder class to create EventProcessorHost instances.
     * <p>
     * To use, start with: EventProcessorHost.EventProcessorHostBuilder.newBuilder(...)
     * Then either use the built-in Azure Storage-based lease and checkpoint managers, or user implementations.
     * Then either supply an Event Hub connection string or use Azure Active Directory (AAD) authentication.
     *    If using AAD auth, either provide a callback or an ITokenProvider
     * Finally, set various optional values as desired, then call build() to get an EventProcessorHost instance.
     */
    public static final class EventProcessorHostBuilder {
        /**
         * The process of building starts here, with arguments that are always required.
         * <p>
         * The hostName parameter is a name for this EventProcessorHost instance, which must be unique among
         * all instances consuming from the same Event Hub and consumer group. The name must be unique because
         * it is used to distinguish which instance owns the lease for a given partition of the event hub. An
         * easy way to generate a unique host name is to call EventProcessorHost.createHostName("mystring").
         *
         * @param hostName  a name for this host instance. See method notes.
         * @param consumerGroupName  The consumer group for the Event Hub
         * @return  interface for setting the lease and checkpoint managers
         */
        public static ManagerStep newBuilder(final String hostName, final String consumerGroupName) {
            return new Steps(hostName, consumerGroupName);
        }

        private EventProcessorHostBuilder() {
        }

        public interface ManagerStep {
            /**
             * Use the built-in Azure Storage-based lease and checkpoint managers.
             *
             * @param storageConnectionString  Connection string for the Azure Storage account
             * @param storageContainerName     name for the blob container within the Storage account
             * @param storageBlobPrefix        prefix for the names of the blobs within the blob container, can be empty or null
             * @return  interface for setting the Event Hub connection info and auth
             */
            AuthStep useAzureStorageCheckpointLeaseManager(String storageConnectionString, String storageContainerName, String storageBlobPrefix);

            /**
             * Use the built-in Azure Storage-based lease and checkpoint managers.
             *
             * @param storageCredentials         credentials for an Azure Storage account, such as an AAD token
             * @param storageContainerName     name for the blob container within the Storage account
             * @param storageBlobPrefix        prefix for the names of the blobs within the blob container, can be empty or null
             * @return  interface for setting the Event Hub connection info and auth
             */
            AuthStep useAzureStorageCheckpointLeaseManager(StorageCredentials storageCredentials, String storageContainerName, String storageBlobPrefix);

            /**
             * Use user-implemented lease and checkpoint managers.
             *
             * @param checkpointManager  user-supplied implementation of {@link ICheckpointManager}
             * @param leaseManager       user-supplied implementation of {@link ILeaseManager}
             * @return  interface for setting the Event Hub connection info and auth
             */
            AuthStep useUserCheckpointAndLeaseManagers(ICheckpointManager checkpointManager, ILeaseManager leaseManager);
        }

        public interface AuthStep {
            /**
             * Azure Portal can provide a connection string with auth information that applies only to one
             * individual Event Hub. In that case, the connection string contains the name of the Event Hub.
             *
             * @param eventHubConnectionString  Event Hub connection string (which contains the name of the Event Hub)
             * @return  interface for setting optional values
             */
            OptionalStep useEventHubConnectionString(String eventHubConnectionString);

            /**
             * Azure Portal can provide a connection string with auth information that applies to the entire
             * namespace instead of an individual Event Hub. Use this overload with such a connection string,
             * which requires you to specify the name of the Event Hub separately.
             *
             * @param eventHubConnectionString  Event Hub connection string (which does not contain the name of the Event Hub)
             * @param eventHubPath              name of the Event Hub
             * @return  interface for setting optional values
             */
            OptionalStep useEventHubConnectionString(String eventHubConnectionString, String eventHubPath);

            /**
             * When using AAD auth, call this method to specify the Event Hub, then add AAD-based auth information in the next step.
             *
             * @param endpoint      URI of the Event Hub namespace
             * @param eventHubPath  name of the Event Hub
             * @return  interface for setting AAD auth info
             */
            AADAuthStep useAADAuthentication(URI endpoint, String eventHubPath);
        }

        public interface AADAuthStep {
            /**
             * Provide a callback which will be called when a token is needed. See {@link AzureActiveDirectoryTokenProvider}
             *
             * @param authCallback  the callback
             * @param authority     AAD authority string which will be passed to the callback. Used for national cloud support.
             * @return  interface for setting optional values
             */
            OptionalStep useAuthenticationCallback(AuthenticationCallback authCallback, String authority);

            /**
             * Provide a user-implemented token provider which will be called when a token is needed.
             *
             * @param tokenProvider  user implementation of ITokenProvider
             * @return  interface for setting optional values
             */
            OptionalStep useTokenProvider(ITokenProvider tokenProvider);
        }

        public interface OptionalStep {
            /**
             * Event Processor Host runs tasks on the supplied threadpool, or creates an internal one.
             * @param executor  threadpool, or null to use an internal one
             * @return  interface for setting optional values
             */
            OptionalStep setExecutor(ScheduledExecutorService executor);

            /**
             * {@link RetryPolicy} for Event Hubs operations. Event Processor Host uses RetryPolicy.getDefault()
             * if none is supplied.
             *
             * @param retryPolicy  desired retry policy
             * @return  interface for setting optional values
             */
            OptionalStep setRetryPolicy(RetryPolicy retryPolicy);

            /**
             * {@link TransportType} for connections to the Event Hubs service. Defaults to TransportType.AMQP.
             * The transport type can also be set in the Event Hub connection string. The value set here will
             * override the value in the connection string, if any.
             *
             * @param transportType  desired transport type
             * @return  interface for setting optional values
             */
            OptionalStep setTransportType(TransportType transportType);

            /**
             * The timeout for Event Hubs operations. Defaults to MessagingFactory.DefaultOperationTimeout.
             * The timeout can also be set in the Event Hub connection string. The value set here will override
             * the value in the connection string, if any.
             *
             * @param operationTimeout  desired timeout
             * @return  interface for setting optional values
             */
            OptionalStep setOperationTimeout(Duration operationTimeout);

            /**
             * After setting all desired optional values, call this method to build an EventProcessorHost instance.
             *
             * @return  new EventProcessorHost instance
             */
            EventProcessorHost build();
        }

        private static class Steps implements ManagerStep, AuthStep, AADAuthStep, OptionalStep {
            private final String hostName;
            private final String consumerGroupName;

            // OptionalStep
            private ScheduledExecutorService executor = null;
            private RetryPolicy retryPolicy = null;
            private TransportType transportType = null;
            private Duration operationTimeout = null;

            // Auth steps
            private String eventHubConnectionString = null; // group 1
            private String eventHubPath = null; // optional for group 1, required for groups 2-3
            private URI endpoint = null; // groups 2-3
            private AuthenticationCallback authCallback = null; // group 2
            private String authority = null; // group 2
            private ITokenProvider tokenProvider = null; // group 3

            // ManagerStep
            private ICheckpointManager checkpointManager;
            private ILeaseManager leaseManager;
            private boolean initializeManagers = false;


            Steps(final String hostName, final String consumerGroupName) {
                if (StringUtil.isNullOrWhiteSpace(hostName) || StringUtil.isNullOrWhiteSpace(consumerGroupName)) {
                    throw new IllegalArgumentException("hostName and consumerGroupName cannot be null or empty");
                }

                this.hostName = hostName;
                this.consumerGroupName = consumerGroupName;
            }

            @Override
            public OptionalStep setExecutor(final ScheduledExecutorService executor) {
                // executor is allowed to be null, causes EPH to create and use an internal one
                this.executor = executor;
                return this;
            }

            @Override
            public OptionalStep setRetryPolicy(final RetryPolicy retryPolicy) {
                this.retryPolicy = retryPolicy;
                return this;
            }

            @Override
            public OptionalStep setTransportType(final TransportType transportType) {
                Objects.requireNonNull(transportType, "'transportType' cannot be null.");

                this.transportType = transportType;
                return this;
            }

            @Override
            public OptionalStep setOperationTimeout(final Duration operationTimeout) {
                Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");

                this.operationTimeout = operationTimeout;
                return this;
            }

            @Override
            public OptionalStep useAuthenticationCallback(final AuthenticationCallback authCallback, final String authority) {
                Objects.requireNonNull(authCallback, "'authCallback' cannot be null.");
                if (StringUtil.isNullOrWhiteSpace(authority)) {
                    throw new IllegalArgumentException("authority cannot be null or empty");
                }

                this.authCallback = authCallback;
                this.authority = authority;
                return this;
            }

            @Override
            public OptionalStep useTokenProvider(final ITokenProvider tokenProvider) {
                Objects.requireNonNull(tokenProvider, "'tokenProvider' cannot be null.");

                this.tokenProvider = tokenProvider;
                return this;
            }

            @Override
            public OptionalStep useEventHubConnectionString(final String eventHubConnectionString) {
                return useEventHubConnectionString(eventHubConnectionString, null);
            }

            @Override
            public OptionalStep useEventHubConnectionString(final String eventHubConnectionString, final String eventHubPath) {
                if (StringUtil.isNullOrWhiteSpace(eventHubConnectionString)) {
                    throw new IllegalArgumentException("eventHubConnectionString cannot be null or empty");
                }
                if ((eventHubPath != null) && StringUtil.isNullOrWhiteSpace(eventHubPath)) {
                    throw new IllegalArgumentException("eventHubPath cannot be empty. Use null if the connection string already contains the path.");
                }

                this.eventHubConnectionString = eventHubConnectionString;
                this.eventHubPath = eventHubPath;
                return this;
            }

            @Override
            public AADAuthStep useAADAuthentication(final URI endpoint, final String eventHubPath) {
                Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
                if (StringUtil.isNullOrWhiteSpace(eventHubPath)) {
                    throw new IllegalArgumentException("eventHubPath cannot be null or empty");
                }

                this.endpoint = endpoint;
                this.eventHubPath = eventHubPath;
                return this;
            }

            @Override
            public AuthStep useAzureStorageCheckpointLeaseManager(final String storageConnectionString,
                    final String storageContainerName, final String storageBlobPrefix) {
                AzureStorageCheckpointLeaseManager mgr = new AzureStorageCheckpointLeaseManager(storageConnectionString, storageContainerName, storageBlobPrefix);
                this.initializeManagers = true;
                return useUserCheckpointAndLeaseManagers(mgr, mgr);
            }

            @Override
            public AuthStep useAzureStorageCheckpointLeaseManager(final StorageCredentials storageCredentials,
                    final String storageContainerName, final String storageBlobPrefix) {
                AzureStorageCheckpointLeaseManager mgr = new AzureStorageCheckpointLeaseManager(storageCredentials, storageContainerName, storageBlobPrefix);
                this.initializeManagers = true;
                return useUserCheckpointAndLeaseManagers(mgr, mgr);
            }

            @Override
            public AuthStep useUserCheckpointAndLeaseManagers(final ICheckpointManager checkpointManager,
                    final ILeaseManager leaseManager) {
                Objects.requireNonNull(checkpointManager, "'checkpointManager' cannot be null.");
                Objects.requireNonNull(leaseManager, "'leaseManager' cannot be null.");

                this.checkpointManager = checkpointManager;
                this.leaseManager = leaseManager;
                return this;
            }

            @Override
            public EventProcessorHost build() {
                // One of these conditions MUST be true. Can't get to the OptionalStep interface where build() is available
                // without setting one of the auth options.
                EventHubClientFactory ehcFactory = null;
                if (this.eventHubConnectionString != null) {
                    normalizeConnectionStringAndEventHubPath();
                    ehcFactory = new EventHubClientFactory.EHCFWithConnectionString(this.eventHubConnectionString, this.retryPolicy);
                } else if (this.authCallback != null) {
                    ehcFactory = new EventHubClientFactory.EHCFWithAuthCallback(this.endpoint, this.eventHubPath,
                            this.authCallback, this.authority, packOptions());
                } else if (this.tokenProvider != null) {
                    ehcFactory = new EventHubClientFactory.EHCFWithTokenProvider(this.endpoint, this.eventHubPath, this.tokenProvider, packOptions());
                }
                return new EventProcessorHost(this.hostName,
                        this.eventHubPath,
                        this.consumerGroupName,
                        ehcFactory,
                        this.checkpointManager,
                        this.leaseManager,
                        this.initializeManagers,
                        this.executor);
            }

            private EventHubClientOptions packOptions() {
                return (new EventHubClientOptions()).setOperationTimeout(this.operationTimeout).setRetryPolicy(this.retryPolicy).setTransportType(this.transportType);
            }

            private void normalizeConnectionStringAndEventHubPath() {
                // The event hub path must appear in at least one of the eventHubPath argument or the connection string.
                // If it appears in both, then it must be the same in both. If it appears in only one, populate the other.
                ConnectionStringBuilder csb = new ConnectionStringBuilder(this.eventHubConnectionString);
                String extractedEntityPath = csb.getEventHubName();
                if ((this.eventHubPath != null) && !this.eventHubPath.isEmpty()) {
                    if (extractedEntityPath != null) {
                        if (this.eventHubPath.compareTo(extractedEntityPath) != 0) {
                            throw new IllegalArgumentException("Provided EventHub path in eventHubPath parameter conflicts with the path in provided EventHub connection string");
                        }
                        // else they are the same and that's fine
                    } else {
                        // There is no entity path in the connection string, so put it there.
                        csb.setEventHubName(this.eventHubPath);
                    }
                } else {
                    if ((extractedEntityPath != null) && !extractedEntityPath.isEmpty()) {
                        this.eventHubPath = extractedEntityPath;
                    } else {
                        throw new IllegalArgumentException("Provide EventHub entity path in either eventHubPath argument or in eventHubConnectionString");
                    }
                }

                if (this.transportType != null) {
                    csb.setTransportType(this.transportType);
                }
                if (this.operationTimeout != null) {
                    csb.setOperationTimeout(this.operationTimeout);
                }

                this.eventHubConnectionString = csb.toString();
            }
        }
    }
}
