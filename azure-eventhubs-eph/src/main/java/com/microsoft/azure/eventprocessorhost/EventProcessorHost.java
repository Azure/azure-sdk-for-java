/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.storage.StorageException;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EventProcessorHost
{
    private final String hostName;
    private final String eventHubPath;
    private final String consumerGroupName;
    private String eventHubConnectionString;
    private RetryPolicy retryPolicy;

    private ICheckpointManager checkpointManager;
    private ILeaseManager leaseManager;
    private boolean initializeLeaseManager = false;
    private boolean unregistered = false;
    private PartitionManager partitionManager;
    private IEventProcessorFactory<?> processorFactory = null;
    private EventProcessorOptions processorOptions;
    private PartitionManagerOptions partitionManagerOptions = null;

    // weOwnExecutor exists to support user-supplied thread pools.
    private final ExecutorService executorService;
    private final boolean weOwnExecutor;
    
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(EventProcessorHost.class);

	private static final Object uuidSynchronizer = new Object();

	/**
	 * Create a new host to process events from an Event Hub.
	 * 
     * Since Event Hubs are generally used for scale-out, high-traffic scenarios, generally there will
     * be only one host per process, and the processes will be run on separate machines. However, it is
     * supported to run multiple hosts on one machine, or even within one process, if throughput is less
     * of a concern, or for development and testing.
     * <p>
     * This overload of the constructor uses the built-in lease and checkpoint managers. The
     * Azure Storage account specified by the storageConnectionString parameter is used by the built-in
     * managers to record leases and checkpoints.
     * <p>
     * The Event Hub connection string may be conveniently constructed using the ConnectionStringBuilder class
     * from the Java Event Hub client.
	 * 
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
	 * @param storageConnectionString	Connection string for the Azure Storage account to use for persisting leases and checkpoints.
	 */
	@Deprecated
    public EventProcessorHost(
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString)
    {
        this(EventProcessorHost.createHostName(null), eventHubPath, consumerGroupName, eventHubConnectionString, storageConnectionString);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * 
     * The hostName parameter is a name for this event processor host, which must be unique among all event processor hosts
     * receiving from this Event Hub/consumer group combination. The overload which does not have a hostName argument defaults to
     * "javahost-" followed by a UUID, which is created by calling EventProcessorHost.createHostName(null). An easy way to
     * generate a unique hostName which also includes other information is to call EventProcessorHost.createHostName("mystring"). 
     * 
     * @param hostName		A name for this event processor host. See method notes.
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
	 * @param storageConnectionString	Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     */
    @Deprecated
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString)
    {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, new AzureStorageCheckpointLeaseManager(storageConnectionString), 
        		(ExecutorService)null);
        this.initializeLeaseManager = true;
    }

    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload adds an argument to specify the Azure Storage container name that will be used to persist leases and checkpoints.
     * 
     * @param hostName		A name for this event processor host. See method notes.
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
	 * @param storageConnectionString	Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName		Azure Storage container name for use by built-in lease and checkpoint manager.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName)
    {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, storageConnectionString, storageContainerName, (ExecutorService)null);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload adds an argument to specify the Azure Storage container name that will be used to persist leases and checkpoints.
     * 
     * @param hostName		A name for this event processor host. See method notes.
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
	 * @param storageConnectionString	Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName		Azure Storage container name for use by built-in lease and checkpoint manager.
     * @param executorService			User-supplied thread executor, or null to use EventProcessorHost-internal executor.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName,
            final ExecutorService executorService)
    {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, storageConnectionString, storageContainerName, (String)null, executorService);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload adds an argument to specify the Azure Storage container name that will be used to persist leases and checkpoints.
     * 
     * @param hostName		A name for this event processor host. See method notes.
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
	 * @param storageConnectionString	Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName		Azure Storage container name for use by built-in lease and checkpoint manager.
     * @param storageBlobPrefix			Prefix used when naming blobs within the storage container.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName,
            final String storageBlobPrefix)
    {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, storageConnectionString, storageContainerName, storageBlobPrefix,
        		(ExecutorService)null);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload adds an argument to specify the Azure Storage container name that will be used to persist leases and checkpoints.
     * 
     * @param hostName		A name for this event processor host. See method notes.
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
	 * @param storageConnectionString	Connection string for the Azure Storage account to use for persisting leases and checkpoints.
     * @param storageContainerName		Azure Storage container name for use by built-in lease and checkpoint manager.
     * @param storageBlobPrefix			Prefix used when naming blobs within the storage container.
     * @param executorService			User-supplied thread executor, or null to use EventProcessorHost-internal executor.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString,
            final String storageContainerName,
            final String storageBlobPrefix,
            final ExecutorService executorService)
    {
    	// Want to check storageConnectionString and storageContainerName here but can't because Java doesn't allow statements before
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
            final ExecutorService executorService)
    {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, combinedManager, combinedManager, executorService, null);
    }


    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload allows the caller to provide their own lease and checkpoint managers to replace the built-in
     * ones based on Azure Storage.
     * 
     * @param hostName		A name for this event processor host. See method notes.
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
     * @param checkpointManager			Implementation of ICheckpointManager, to be replacement checkpoint manager.
     * @param leaseManager				Implementation of ILeaseManager, to be replacement lease manager.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            ICheckpointManager checkpointManager,
            ILeaseManager leaseManager)
    {
    	this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, checkpointManager, leaseManager, null, null);
    }
    
    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload allows the caller to provide their own lease and checkpoint managers to replace the built-in
     * ones based on Azure Storage, and to provide an executor service.
     * 
     * @param hostName		A name for this event processor host. See method notes.
	 * @param eventHubPath 				Specifies the Event Hub to receive events from.
	 * @param consumerGroupName			The name of the consumer group to use when receiving from the Event Hub.
	 * @param eventHubConnectionString	Connection string for the Event Hub to receive from.
     * @param checkpointManager			Implementation of ICheckpointManager, to be replacement checkpoint manager.
     * @param leaseManager				Implementation of ILeaseManager, to be replacement lease manager.
     * @param executorService			User-supplied thread executor, or null to use EventProcessorHost-internal executor.
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            ICheckpointManager checkpointManager,
            ILeaseManager leaseManager,
            ExecutorService executorService,
            RetryPolicy retryPolicy)
    {
    	if ((hostName == null) || hostName.isEmpty())
    	{
    		throw new IllegalArgumentException("hostName argument must not be null or empty string");
    	}
    	
    	// eventHubPath is allowed to be null or empty if it is provided in the connection string. That will be checked later.
    	
    	if ((consumerGroupName == null) || consumerGroupName.isEmpty())
    	{
    		throw new IllegalArgumentException("consumerGroupName argument must not be null or empty");
    	}
    	
    	if ((eventHubConnectionString == null) || eventHubConnectionString.isEmpty())
    	{
    		throw new IllegalArgumentException("eventHubConnectionString argument must not be null or empty");
    	}
    	
    	// The event hub path must appear in at least one of the eventHubPath argument or the connection string.
    	// If it appears in both, then it must be the same in both. If it appears in only one, populate the other.
    	ConnectionStringBuilder providedCSB = new ConnectionStringBuilder(eventHubConnectionString); 
    	String extractedEntityPath = providedCSB.getEventHubName();
        this.eventHubConnectionString = eventHubConnectionString;
    	if ((eventHubPath != null) && !eventHubPath.isEmpty())
    	{
    		this.eventHubPath = eventHubPath;
    		if (extractedEntityPath != null)
   			{
    			if (eventHubPath.compareTo(extractedEntityPath) != 0)
	    		{
	    			throw new IllegalArgumentException("Provided EventHub path in eventHubPath parameter conflicts with the path in provided EventHub connection string");
	    		}
    			// else they are the same and that's fine
   			}
    		else
    		{
    			// There is no entity path in the connection string, so put it there.
    			ConnectionStringBuilder rebuildCSB = new ConnectionStringBuilder()
                        .setEndpoint(providedCSB.getEndpoint())
                        .setEventHubName(this.eventHubPath)
                        .setSasKeyName(providedCSB.getSasKeyName())
                        .setSasKey(providedCSB.getSasKey());
    			rebuildCSB.setOperationTimeout(providedCSB.getOperationTimeout());
    			this.eventHubConnectionString = rebuildCSB.toString();
    		}
    	}
    	else
    	{
    		if ((extractedEntityPath != null) && !extractedEntityPath.isEmpty())
    		{
    			this.eventHubPath = extractedEntityPath;
    		}
    		else
    		{
    			throw new IllegalArgumentException("Provide EventHub entity path in either eventHubPath argument or in eventHubConnectionString");
    		}
    	}
    	
    	if (checkpointManager == null)
    	{
    		throw new IllegalArgumentException("Must provide an object which implements ICheckpointManager");
    	}
    	if (leaseManager == null)
    	{
    		throw new IllegalArgumentException("Must provide an object which implements ILeaseManager");
    	}
    	// executorService argument is allowed to be null, that is the indication to use an internal threadpool.
    	
    	if (this.partitionManagerOptions == null)
    	{
    		// Normally will not be null because we're using the AzureStorage implementation.
    		// If it is null, we're using user-supplied implementation. Establish generic defaults
    		// in case the user doesn't provide an options object.
    		this.partitionManagerOptions = new PartitionManagerOptions();
    	}
    	
        this.hostName = hostName;
        this.consumerGroupName = consumerGroupName;
        this.checkpointManager = checkpointManager;
        this.leaseManager = leaseManager;
        this.retryPolicy = retryPolicy;

        if (executorService != null)
        {
            // User has supplied an ExecutorService, so use that.
            this.weOwnExecutor = false;
            this.executorService = executorService;
        }
        else
        {
            this.weOwnExecutor = true;
            this.executorService = Executors.newCachedThreadPool();
        }
        
        this.partitionManager = new PartitionManager(this);

        TRACE_LOGGER.info(LoggingUtils.withHost(this.hostName, "New EventProcessorHost created."));
    }

    /**
     * Returns processor host name.
     * 
     * If the processor host name was automatically generated, this is the only way to get it.
     * 
     * @return	the processor host name
     */
    public String getHostName() { return this.hostName; }

    /**
     * Returns the Event Hub connection string assembled by the processor host.
     * 
     * The connection string is assembled from info provider by the caller to the constructor
     * using ConnectionStringBuilder, so it's not clear that there's any value to making this
     * string accessible.
     * 
     * @return	Event Hub connection string.
     */
    public String getEventHubConnectionString() { return this.eventHubConnectionString; }

    /**
     * Returns the retry policy used by the processor host. See {@link RetryPolicy}
     *
     * @return Event Hub retry policy.
     */
    public RetryPolicy getRetryPolicy() {
        if (this.retryPolicy != null) {
            return this.retryPolicy;
        } else {
            return RetryPolicy.getDefault();
        }
    }
    
    // TEST USE ONLY
    void setPartitionManager(PartitionManager pm) { this.partitionManager = pm; }
    
    // All of these accessors are for internal use only.
    ExecutorService getExecutorService() { return this.executorService; }
    ICheckpointManager getCheckpointManager() { return this.checkpointManager; }
    ILeaseManager getLeaseManager() { return this.leaseManager; }
    PartitionManager getPartitionManager() { return this.partitionManager; }
    IEventProcessorFactory<?> getProcessorFactory() { return this.processorFactory; }
    String getEventHubPath() { return this.eventHubPath; }
    String getConsumerGroupName() { return this.consumerGroupName; }
    EventProcessorOptions getEventProcessorOptions() { return this.processorOptions; }
    
    /**
     * Returns the existing partition manager options object. Unless you are providing implementations of
     * ILeaseManager and ICheckpointMananger, to change partition manager options, call this method to get
     * the existing object and call setters on it to adjust the values.
     *
     * @return the internally-created PartitionManangerObjects object or any replacement object set with setPartitionManangerOptions
     */
    public PartitionManagerOptions getPartitionManagerOptions() { return this.partitionManagerOptions; }
    
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
    public void setPartitionManagerOptions(PartitionManagerOptions options) { this.partitionManagerOptions = options; }
    
    /**
     * Register class for event processor and start processing.
     *
     * <p>
     * This overload uses the default event processor factory, which simply creates new instances of
     * the registered event processor class, and uses all the default options.
     * <pre>
     * class EventProcessor implements IEventProcessor { ... }
     * EventProcessorHost host = new EventProcessorHost(...);
     * Future foo = host.registerEventProcessor(EventProcessor.class);
     * foo.get();
     * </pre>
     *  
     * @param eventProcessorType	Class that implements IEventProcessor.
     * @return						Future that completes when initialization is finished. If initialization fails, get() will throw. 
     */
    public <T extends IEventProcessor> Future<?> registerEventProcessor(Class<T> eventProcessorType) throws Exception
    {
        DefaultEventProcessorFactory<T> defaultFactory = new DefaultEventProcessorFactory<T>();
        defaultFactory.setEventProcessorClass(eventProcessorType);
        return registerEventProcessorFactory(defaultFactory, EventProcessorOptions.getDefaultOptions());
    }

    /**
     * Register class for event processor and start processing.
     * 
     * This overload uses the default event processor factory, which simply creates new instances of
     * the registered event processor class, but takes user-specified options.
     *  
     * @param eventProcessorType	Class that implements IEventProcessor.
     * @param processorOptions		Options for the processor host and event processor(s).
     * @return						Future that completes when initialization is finished. If initialization fails, get() will throw. 
     */
    public <T extends IEventProcessor> Future<?> registerEventProcessor(Class<T> eventProcessorType, EventProcessorOptions processorOptions) throws Exception
    {
        DefaultEventProcessorFactory<T> defaultFactory = new DefaultEventProcessorFactory<T>();
        defaultFactory.setEventProcessorClass(eventProcessorType);
        return registerEventProcessorFactory(defaultFactory, processorOptions);
    }

    /**
     * Register user-supplied event processor factory and start processing.
     * 
     * <p>
     * If creating a new event processor requires more work than just new'ing an objects, the user must
     * create an object that implements IEventProcessorFactory and pass it to this method, instead of calling
     * registerEventProcessor.
     * <p>
     * This overload uses default options for the processor host and event processor(s).
     * 
     * @param factory	User-supplied event processor factory object.
     * @return			Future that completes when initialization is finished. If initialization fails, get() will throw.
     */
    public Future<?> registerEventProcessorFactory(IEventProcessorFactory<?> factory) throws Exception
    {
        return registerEventProcessorFactory(factory, EventProcessorOptions.getDefaultOptions());
    }

    /**
     * Register user-supplied event processor factory and start processing.
     * 
     * This overload takes user-specified options.
     * 
     * @param factory			User-supplied event processor factory object.			
     * @param processorOptions	Options for the processor host and event processor(s).
     * @return					Future that completes when initialization is finished. If initialization fails, get() will throw.
     */
    public Future<?> registerEventProcessorFactory(IEventProcessorFactory<?> factory, EventProcessorOptions processorOptions) throws Exception
    {
        if (this.unregistered)
        {
            throw new IllegalStateException("Register cannot be called on an EventProcessorHost after unregister. Please create a new EventProcessorHost instance.");
        }
    	if (this.processorFactory != null)
    	{
    		throw new IllegalStateException("Register has already been called on this EventProcessorHost");
    	}
    	
        if (this.executorService.isShutdown() || this.executorService.isTerminated())
    	{
    		TRACE_LOGGER.warn(LoggingUtils.withHost(this.hostName,
                    "Calling registerEventProcessor/Factory after executor service has been shut down."));
    		throw new RejectedExecutionException("EventProcessorHost executor service has been shut down");
    	}
    	
        if (this.initializeLeaseManager)
        {
            try
            {
				((AzureStorageCheckpointLeaseManager)leaseManager).initialize(this);
			}
            catch (InvalidKeyException | URISyntaxException | StorageException e)
            {
                TRACE_LOGGER.warn(LoggingUtils.withHost(this.hostName, "Failure initializing Storage lease manager."));
            	throw new RuntimeException("Failure initializing Storage lease manager", e);
			}
        }

        TRACE_LOGGER.info(LoggingUtils.withHost(this.hostName, "Starting event processing."));

        this.processorFactory = factory;
        this.processorOptions = processorOptions;
        return this.executorService.submit(() -> this.partitionManager.initialize());
    }

    /**
     * Stop processing events.
     * 
     * Does not return until the shutdown is complete.
     * 
     */
    public void unregisterEventProcessor() throws InterruptedException, ExecutionException
    {
    	TRACE_LOGGER.info(LoggingUtils.withHost(this.hostName, "Stopping event processing"));
        this.unregistered = true;
    	
    	if (this.partitionManager != null)
    	{
	        try
	        {
	        	Future<?> stoppingPartitions = this.partitionManager.stopPartitions();
	        	if (stoppingPartitions != null)
	        	{
	        		stoppingPartitions.get();
	        	}
	            
                if (this.weOwnExecutor)
                {
                    this.executorService.awaitTermination(10, TimeUnit.MINUTES);
                }
			}
	        catch (InterruptedException | ExecutionException e)
	        {
	        	// Log the failure but nothing really to do about it.
                TRACE_LOGGER.warn(LoggingUtils.withHost(this.hostName, "Failure shutting down"), e);
	        	throw e;
			}
    	}
    }
    
    // PartitionManager calls this after all shutdown tasks have been submitted to the ExecutorService.
    void stopExecutor()
    {
        if (this.weOwnExecutor)
        {
			// It is OK to call shutdown() here even if threads are still running.
			// Shutdown() causes the executor to stop accepting new tasks, but existing tasks will
			// run to completion. The pool will terminate when all existing tasks finish.
			// By this point all new tasks generated by the shutdown have been submitted.
			this.executorService.shutdown();
        }
    }
    
    /**
     * This method is no longer required and does nothing. If the user supplies a thread pool, we will never
     * shut it down. The internal thread pool is per-instance now, so it is always safe to auto shut down.
     */
    @Deprecated
    public static void setAutoExecutorShutdown(boolean auto)
    {
    }

    /**
     * This method is no longer required and does nothing. If the user supplies a thread pool, we will never
     * shut it down. The internal thread pool is per-instance now, so it is always safe to auto shut down.
     */
    @Deprecated
    public static void forceExecutorShutdown(long secondsToWait) throws InterruptedException
    {
    }

    /**
     * Convenience method for generating unique host names, safe to pass to the EventProcessorHost constructors
     * that take a hostName argument.
     * 
     * If a prefix is supplied, the constructed name begins with that string. If the prefix argument is null or
     * an empty string, the constructed name begins with "javahost". Then a dash '-' and a UUID are appended to
     * create a unique name.
     * 
     * @param prefix	String to use as the beginning of the name. If null or empty, a default is used.
     * @return			A unique host name to pass to EventProcessorHost constructors.
     */
    public static String createHostName(String prefix)
    {
    	String usePrefix = prefix;
    	if ((usePrefix == null) || usePrefix.isEmpty())
    	{
    		usePrefix = "javahost";
    	}
    	return usePrefix + "-" + safeCreateUUID();
    }
    
    /**
     * Synchronized string UUID generation convenience method.
     * 
     * We saw null and empty strings returned from UUID.randomUUID().toString() when used from multiple
     * threads and there is no clear answer on the net about whether it is really thread-safe or not.
     * <p>
     * One of the major users of UUIDs is the built-in lease and checkpoint manager, which can be replaced by
     * user implementations. This UUID generation method is public so user implementations can use it as well and
     * avoid the problems.
     * 
     * @return A string UUID with dashes but no curly brackets.
     */
    public static String safeCreateUUID()
    {
    	synchronized (EventProcessorHost.uuidSynchronizer)
    	{
    		final UUID newUuid = UUID.randomUUID();
        	return new String(newUuid.toString());
    	}
    }
}
