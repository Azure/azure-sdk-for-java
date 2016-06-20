/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import com.microsoft.azure.storage.StorageException;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;


public final class EventProcessorHost
{
    private final String hostName;
    private final String eventHubPath;
    private final String consumerGroupName;
    private String eventHubConnectionString;

    private ICheckpointManager checkpointManager;
    private ILeaseManager leaseManager;
    private boolean initializeLeaseManager = false; 
    private PartitionManager partitionManager;
    private IEventProcessorFactory<?> processorFactory;
    private EventProcessorOptions processorOptions;

    // Thread pool is shared among all instances of EventProcessorHost
    // weOwnExecutor exists to support user-supplied thread pools if we add that feature later.
    // weOwnExecutor is a boxed Boolean so it can be used to synchronize access to these variables.
    // executorRefCount is required because the last host must shut down the thread pool if we own it.
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static int executorRefCount = 0;
    private static Boolean weOwnExecutor = true;
    private static boolean autoShutdownExecutor = false;
    
    public final static String EVENTPROCESSORHOST_TRACE = "eventprocessorhost.trace";
	private static final Logger TRACE_LOGGER = Logger.getLogger(EventProcessorHost.EVENTPROCESSORHOST_TRACE);
	
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
     * @param eventHubPath
     * @param consumerGroupName
     * @param eventHubConnectionString
     * @param storageConnectionString
     */
    public EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final String storageConnectionString)
    {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, new AzureStorageCheckpointLeaseManager(storageConnectionString));
        this.initializeLeaseManager = true;
    }

    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload adds an argument to specify the Azure Storage container name that will be used to persist leases and checkpoints.
     * 
     * @param hostName
     * @param eventHubPath
     * @param consumerGroupName
     * @param eventHubConnectionString
     * @param storageConnectionString
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
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString,
                new AzureStorageCheckpointLeaseManager(storageConnectionString, storageContainerName));
        this.initializeLeaseManager = true;
    }
    
    // Because Java won't let you do ANYTHING before calling another constructor. In particular, you can't
    // new up an object and pass it as TWO parameters of the other constructor.
    private EventProcessorHost(
            final String hostName,
            final String eventHubPath,
            final String consumerGroupName,
            final String eventHubConnectionString,
            final AzureStorageCheckpointLeaseManager combinedManager)
    {
        this(hostName, eventHubPath, consumerGroupName, eventHubConnectionString, combinedManager, combinedManager);
    }

    /**
     * Create a new host to process events from an Event Hub.
     * 
     * This overload allows the caller to provide their own lease and checkpoint managers to replace the built-in
     * ones based on Azure Storage.
     * 
     * @param hostName
     * @param eventHubPath
     * @param consumerGroupName
     * @param eventHubConnectionString
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
    	EventProcessorHost.TRACE_LOGGER.setLevel(Level.SEVERE);
    	
        this.hostName = hostName;
        this.eventHubPath = eventHubPath;
        this.consumerGroupName = consumerGroupName;
        this.eventHubConnectionString = eventHubConnectionString;
        this.checkpointManager = checkpointManager;
        this.leaseManager = leaseManager;
        
        if (EventProcessorHost.weOwnExecutor)
        {
	        synchronized(EventProcessorHost.weOwnExecutor)
	        {
	        	EventProcessorHost.executorRefCount++;
	        }
        }

        this.partitionManager = new PartitionManager(this);
        
        logWithHost(Level.INFO, "New EventProcessorHost created");
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
    
    // TEST USE ONLY
    void setPartitionManager(PartitionManager pm) { this.partitionManager = pm; }
    
    // All of these accessors are for internal use only.
    static ExecutorService getExecutorService() { return EventProcessorHost.executorService; }
    ICheckpointManager getCheckpointManager() { return this.checkpointManager; }
    ILeaseManager getLeaseManager() { return this.leaseManager; }
    PartitionManager getPartitionManager() { return this.partitionManager; }
    IEventProcessorFactory<?> getProcessorFactory() { return this.processorFactory; }
    String getEventHubPath() { return this.eventHubPath; }
    String getConsumerGroupName() { return this.consumerGroupName; }
    EventProcessorOptions getEventProcessorOptions() { return this.processorOptions; }
    
    /**
     * Register class for event processor and start processing.
     *
     * <p>
     * This overload uses the default event processor factory, which simply creates new instances of
     * the registered event processor class, and uses all the default options.
     * <pre>
     * class EventProcessor implements IEventProcessor { ... }
     * EventProcessorHost host = new EventProcessorHost(...);
     * Future<?> foo = host.registerEventProcessor(EventProcessor.class);
     * </pre>
     *  
     * @param eventProcessorType	Class that implements IEventProcessor.
     * @return						Future that does not complete until the processor host shuts down.
     * @throws Exception 
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
     * @return						Future that does not complete until the processor host shuts down.
     * @throws Exception 
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
     * @return			Future that does not complete until the processor host shuts down.
     * @throws Exception 
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
     * @return					Future that does not complete until the processor host shuts down.
     * @throws Exception 
     */
    public Future<?> registerEventProcessorFactory(IEventProcessorFactory<?> factory, EventProcessorOptions processorOptions) throws Exception
    {
    	if (EventProcessorHost.executorService.isShutdown() || EventProcessorHost.executorService.isTerminated())
    	{
    		this.logWithHost(Level.SEVERE, "Calling registerEventProcessor/Factory after executor service has been shut down");
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
            	this.logWithHost(Level.SEVERE, "Failure initializing Storage lease manager", e);
            	throw new RuntimeException("Failure initializing Storage lease manager", e);
			}
        }
        
        logWithHost(Level.INFO, "Starting event processing");
        this.processorFactory = factory;
        this.processorOptions = processorOptions;
        return EventProcessorHost.executorService.submit(() -> this.partitionManager.initialize()); 
    }

    /**
     * Stop processing events.
     * 
     * Does not return until the shutdown is complete.
     * 
     */
    public void unregisterEventProcessor() throws InterruptedException, ExecutionException
    {
    	logWithHost(Level.INFO, "Stopping event processing");
    	
        try
        {
            this.partitionManager.stopPartitions().get();
            
	        if (EventProcessorHost.weOwnExecutor)
	        {
	        	// If there are multiple EventProcessorHosts in one process, only await the shutdown on the last one.
	        	// Otherwise the first one will block forever here.
	        	// This could race with stopExecutor() but that is harmless: it is legal to call awaitTermination()
	        	// at any time, whether executorServer.shutdown() has been called yet or not.
	        	if ((EventProcessorHost.executorRefCount <= 0) && EventProcessorHost.autoShutdownExecutor)
	        	{
	        		EventProcessorHost.executorService.awaitTermination(10, TimeUnit.MINUTES);
	        	}
	        }
		}
        catch (InterruptedException | ExecutionException e)
        {
        	// Log the failure but nothing really to do about it.
        	logWithHost(Level.SEVERE, "Failure shutting down", e);
        	throw e;
		}
    }
    
    // PartitionManager calls this after all shutdown tasks have been submitted to the ExecutorService.
    void stopExecutor()
    {
        if (EventProcessorHost.weOwnExecutor && EventProcessorHost.autoShutdownExecutor)
        {
        	synchronized(EventProcessorHost.weOwnExecutor)
        	{
        		EventProcessorHost.executorRefCount--;
        		if (EventProcessorHost.executorRefCount <= 0)
        		{
        			// It is OK to call shutdown() here even though threads are still running.
        			// Shutdown() causes the executor to stop accepting new tasks, but existing tasks will
        			// run to completion. The pool will terminate when all existing tasks finish.
        			// By this point all new tasks generated by the shutdown have been submitted.
        			EventProcessorHost.executorService.shutdown();
        		}
        	}
        }
    }

    
    /**
     * EventProcessorHost can automatically shut down its internal ExecutorService when the last host shuts down
     * due to an unregisterEventProcessor() call. However, doing so means that any EventProcessorHost instances
     * created after that will be unable to function. Set this option to true only if you are sure that you will
     * only ever call unregisterEventProcess() when the process is shutting down.
     * <p>
     * If you leave this option as the default false, then you should call forceExecutorShutdown() at the appropriate time.
     * 
     * @param auto  true for automatic shutdown, false for manual via forceExecutorShutdown()
     */
    public static void setAutoExecutorShutdown(boolean auto) { EventProcessorHost.autoShutdownExecutor = auto; }

    /**
     * If you do not want to use the automatic shutdown option, then you must call forceExecutorShutdown() during
     * process termination, after the last call to unregisterEventProcessor() has returned. Be sure that you will
     * not need to create any new EventProcessorHost instances, because calling this method means that any new
     * instances will fail when a register* method is called.
     * 
     * @param secondsToWait  How long to wait for the ExecutorService to shut down
     * @throws InterruptedException
     */
    public static void forceExecutorShutdown(long secondsToWait) throws InterruptedException
    {
    	EventProcessorHost.executorService.shutdown();
    	EventProcessorHost.executorService.awaitTermination(secondsToWait, TimeUnit.SECONDS);
    }

    
    //
    // Centralized logging.
    //
    
    void log(Level logLevel, String logMessage)
    {
  		EventProcessorHost.TRACE_LOGGER.log(logLevel, logMessage);
    	//System.out.println(LocalDateTime.now().toString() + ": " + logLevel.toString() + ": " + logMessage);
    }
    
    void logWithHost(Level logLevel, String logMessage)
    {
    	log(logLevel, "host " + this.hostName + ": " + logMessage);
    }
    
    void logWithHost(Level logLevel, String logMessage, Throwable e)
    {
    	log(logLevel, "host " + this.hostName + ": " + logMessage);
    	logWithHost(logLevel, "Caught " + e.toString());
    	StackTraceElement[] stack = e.getStackTrace();
    	for (int i = 0; i < stack.length; i++)
    	{
    		logWithHost(logLevel, stack[i].toString());
    	}
    	Throwable cause = e.getCause();
    	if ((cause != null) && (cause instanceof Exception))
    	{
    		Exception inner = (Exception)cause;
    		logWithHost(logLevel, "Inner exception " + inner.toString());
    		stack = inner.getStackTrace();
        	for (int i = 0; i < stack.length; i++)
        	{
        		logWithHost(logLevel, stack[i].toString());
        	}
    	}
    }
    
    void logWithHostAndPartition(Level logLevel, String partitionId, String logMessage)
    {
    	logWithHost(logLevel, "partition " + partitionId + ": " + logMessage);
    }
    
    void logWithHostAndPartition(Level logLevel, String partitionId, String logMessage, Throwable e)
    {
    	logWithHostAndPartition(logLevel, partitionId, logMessage);
    	logWithHostAndPartition(logLevel, partitionId, "Caught " + e.toString());
    	StackTraceElement[] stack = e.getStackTrace();
    	for (int i = 0; i < stack.length; i++)
    	{
    		logWithHostAndPartition(logLevel, partitionId, stack[i].toString());
    	}
    	Throwable cause = e.getCause();
    	if ((cause != null) && (cause instanceof Exception))
    	{
    		Exception inner = (Exception)cause;
    		logWithHostAndPartition(logLevel, partitionId, "Inner exception " + inner.toString());
    		stack = inner.getStackTrace();
        	for (int i = 0; i < stack.length; i++)
        	{
        		logWithHostAndPartition(logLevel, partitionId, stack[i].toString());
        	}
    	}
    }
    
    void logWithHostAndPartition(Level logLevel, PartitionContext context, String logMessage)
    {
    	logWithHostAndPartition(logLevel, context.getPartitionId(), logMessage);
    }
    
    void logWithHostAndPartition(Level logLevel, PartitionContext context, String logMessage, Throwable e)
    {
    	logWithHostAndPartition(logLevel, context.getPartitionId(), logMessage, e);
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
     * threads and there is no clear answer on the net about whether it is really thread-safe or not. Synchronizing
     * on a static object has made the problem go away.
     * <p>
     * One of the major users of UUIDs is the built-in lease and checkpoint manager, which can be replaced by
     * user implementations. This UUID generation method is public so user implementations can use it as well and
     * avoid the problems.
     * 
     * @return A string UUID with dashes but no curly brackets.
     */
    public static String safeCreateUUID()
    {
    	String uuid = "not generated";
    	synchronized (EventProcessorHost.uuidSynchronizer)
    	{
    		uuid = UUID.randomUUID().toString();
    	}
    	return uuid;
    }
}
