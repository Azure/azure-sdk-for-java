/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.azure.eventhubs.EventHubClient;
import com.microsoft.azure.eventhubs.EventHubRuntimeInformation;
import com.microsoft.azure.eventhubs.IllegalEntityException;
import com.microsoft.azure.eventhubs.EventHubException;
import com.microsoft.azure.eventhubs.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PartitionManager
{
	// Protected instead of private for testability
    protected final HostContext hostContext;
    protected Pump pump;
    protected volatile String partitionIds[] = null;
    
    final private Object scanFutureSynchronizer = new Object(); 
    private ScheduledFuture<?> scanFuture = null;

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(PartitionManager.class);

    PartitionManager(HostContext hostContext)
    {
        this.hostContext = hostContext;
    }
    
    CompletableFuture<Void> cachePartitionIds()
    {
    	CompletableFuture<Void> retval = null;
    	
    	if (this.partitionIds != null)
    	{
    		retval = CompletableFuture.completedFuture(null);
    	}
    	else
        {
    		// This try-catch is necessary because EventHubClient.createFromConnectionString can directly throw
    		// EventHubException or IOException, in addition to whatever failures may occur when the result of
    		// the CompletableFuture is evaluated.
    		try
    		{
    			// Stage 0: get EventHubClient for the event hub
				retval = EventHubClient.createFromConnectionString(this.hostContext.getEventHubConnectionString(), this.hostContext.getRetryPolicy(), this.hostContext.getExecutor())
				// Stage 1: use the client to get runtime info for the event hub 
				.thenComposeAsync((ehClient) -> ehClient.getRuntimeInformation(), this.hostContext.getExecutor())
				// Stage 2: extract the partition ids from the runtime info or throw on null (timeout)
				.thenAcceptAsync((EventHubRuntimeInformation ehInfo) ->
				{
					if (ehInfo != null)
					{
						this.partitionIds = ehInfo.getPartitionIds();

						TRACE_LOGGER.info(this.hostContext.withHost("Eventhub " + this.hostContext.getEventHubPath() + " count of partitions: " + ehInfo.getPartitionCount()));
						for (String id : this.partitionIds)
						{
							TRACE_LOGGER.info(this.hostContext.withHost("Found partition with id: " + id));
						}
					}
					else
					{
						throw new CompletionException(new TimeoutException("getRuntimeInformation returned null"));
					}
				}, this.hostContext.getExecutor())
				// Stage 3: RUN REGARDLESS OF EXCEPTIONS -- if there was an error, wrap it in IllegalEntityException and throw
				.whenCompleteAsync((empty, e) ->
				{
					if (e != null)
					{
						Throwable notifyWith = e;
						if (e instanceof CompletionException)
						{
							notifyWith = e.getCause();
						}
						throw new CompletionException(new IllegalEntityException("Failure getting partition ids for event hub", notifyWith));
					}
				}, this.hostContext.getExecutor());
			}
    		catch (EventHubException | IOException e)
    		{
    			retval = new CompletableFuture<Void>();
    			retval.completeExceptionally(new IllegalEntityException("Failure getting partition ids for event hub", e));
			}
        }
        
        return retval;
    }

    // Testability hook: allows a test subclass to insert dummy pump.
    Pump createPumpTestHook()
    {
        return new Pump(this.hostContext);
    }

    // Testability hook: called after stores are initialized.
    void onInitializeCompleteTestHook()
    {
    }

    // Testability hook: called at the end of the main loop after all partition checks/stealing is complete.
    void onPartitionCheckCompleteTestHook()
    {
    }
    
    CompletableFuture<Void> stopPartitions()
    {
    	// Stop the lease scanner.
    	synchronized (this.scanFutureSynchronizer)
    	{
    		this.scanFuture.cancel(true);
    	}

    	// Stop any partition pumps that are running.
    	TRACE_LOGGER.info(this.hostContext.withHost("Shutting down all pumps"));
    	CompletableFuture<?>[] pumpRemovals = this.pump.removeAllPumps(CloseReason.Shutdown);
    	return CompletableFuture.allOf(pumpRemovals).whenCompleteAsync((empty, e) ->
    	{
    		if (e != null)
    		{
    			Throwable notifyWith = LoggingUtils.unwrapException(e, null);
    			TRACE_LOGGER.warn(this.hostContext.withHost("Failure during shutdown"), notifyWith);
    			if (notifyWith instanceof Exception)
    			{
    				this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), (Exception) notifyWith,
    						EventProcessorHostActionStrings.PARTITION_MANAGER_CLEANUP);

    			}
    		}
	        TRACE_LOGGER.info(this.hostContext.withHost("Partition manager exiting"));
    	}, this.hostContext.getExecutor());
    }
    
    public CompletableFuture<Void> initialize()
    {
    	this.pump = createPumpTestHook();
    	
    	// Stage 0: get partition ids and cache
    	return cachePartitionIds()
    	// Stage 1: initialize stores, if stage 0 succeeded
    	.thenComposeAsync((unused) -> initializeStores(), this.hostContext.getExecutor())
    	// Stage 2: RUN REGARDLESS OF EXCEPTIONS -- trace errors
    	.whenCompleteAsync((empty, e) ->
    	{
    		if (e != null)
    		{
    			StringBuilder outAction = new StringBuilder();
    			Throwable notifyWith = LoggingUtils.unwrapException(e, outAction);
    			if (outAction.length() > 0)
    			{
    	    		TRACE_LOGGER.warn(this.hostContext.withHost(
    	                    "Exception while initializing stores (" + outAction.toString() + "), not starting partition manager"), notifyWith);
    			}
    			else
    			{
    	    		TRACE_LOGGER.warn(this.hostContext.withHost("Exception while initializing stores, not starting partition manager"), notifyWith);
    			}
    		}
    	}, this.hostContext.getExecutor())
    	// Stage 3: schedule scan, which will find partitions and start pumps, if previous stages succeeded
    	.thenRunAsync(() ->
    	{
			// Schedule the first scan right away.
    		synchronized (this.scanFutureSynchronizer)
    		{
    			this.scanFuture = this.hostContext.getExecutor().schedule(() -> scan(), 0, TimeUnit.SECONDS);
    		}
	    	
			onInitializeCompleteTestHook();
    	}, this.hostContext.getExecutor());
    }
    
    private CompletableFuture<?> initializeStores()
    {
        ILeaseManager leaseManager = this.hostContext.getLeaseManager();
        ICheckpointManager checkpointManager = this.hostContext.getCheckpointManager();
        
        // Stages 0 to N: create lease store if it doesn't exist
        CompletableFuture<?> initializeStoresFuture = buildRetries(CompletableFuture.completedFuture(null),
        		() -> leaseManager.createLeaseStoreIfNotExists(), null, "Failure creating lease store for this Event Hub, retrying",
				"Out of retries creating lease store for this Event Hub", EventProcessorHostActionStrings.CREATING_LEASE_STORE, 5);
        
        // Stages N+1 to M: create checkpoint store if it doesn't exist
        initializeStoresFuture = buildRetries(initializeStoresFuture, () -> checkpointManager.createCheckpointStoreIfNotExists(), null,
				"Failure creating checkpoint store for this Event Hub, retrying", "Out of retries creating checkpoint store for this Event Hub",
				EventProcessorHostActionStrings.CREATING_CHECKPOINT_STORE, 5);
        
        // Stages M to whatever: by now, either the stores exist or one of them completed exceptionally and
        // all these stages will be skipped
        for (String id : this.partitionIds)
        {
        	final String iterationId = id;
        	// Stages X to X+N: create lease for partition <iterationId>
        	initializeStoresFuture = buildRetries(initializeStoresFuture, () -> leaseManager.createLeaseIfNotExists(iterationId), iterationId,
        			"Failure creating lease for partition, retrying", "Out of retries creating lease for partition", EventProcessorHostActionStrings.CREATING_LEASE, 5);
        	// Stages X+N+1 to X+N+M: create checkpoint holder for partition <iterationId>
        	initializeStoresFuture = buildRetries(initializeStoresFuture, () -> checkpointManager.createCheckpointIfNotExists(iterationId), iterationId,
        			"Failure creating checkpoint for partition, retrying", "Out of retries creating checkpoint blob for partition",
        			EventProcessorHostActionStrings.CREATING_CHECKPOINT, 5);
        }

        return initializeStoresFuture;
    }
    
    // CompletableFuture will be completed exceptionally if it runs out of retries.
    // If the lambda succeeds, then it will not be invoked again by following stages.
    private CompletableFuture<?> buildRetries(CompletableFuture<?> buildOnto, Callable<CompletableFuture<?>> lambda, String partitionId, String retryMessage,
    		String finalFailureMessage, String action, int maxRetries)
    {
    	// Stage 0: first attempt
    	CompletableFuture<?> retryChain = buildOnto.thenComposeAsync((unused) ->
    	{
    		CompletableFuture<?> newresult = CompletableFuture.completedFuture(null);
    		try
    		{
				newresult = lambda.call();
			}
    		catch (Exception e1)
    		{
    			throw new CompletionException(e1);
			}
    		return newresult;
    	}, this.hostContext.getExecutor());
    	
    	for (int i = 1; i < maxRetries; i++)
    	{
    		retryChain = retryChain
    		// Stages 1, 3, 5, etc: trace errors but stop exception propagation in order to keep going
    		// Either return null if we don't have a valid result, or pass the result along to the next stage.
    		.handleAsync((r,e) ->
    		{
    			if (e != null)
    			{
        			if (partitionId != null)
        			{
        				TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(partitionId, retryMessage), LoggingUtils.unwrapException(e, null));
        			}
        			else
        			{
        				TRACE_LOGGER.warn(this.hostContext.withHost(retryMessage), LoggingUtils.unwrapException(e, null));
        			}
    			}
    			return (e == null) ? r : null; // stop propagation of exceptions
    		}, this.hostContext.getExecutor())
    		// Stages 2, 4, 6, etc: if we already have a valid result, pass it along. Otherwise, make another attempt.
    		// Once we have a valid result there will be no more attempts or exceptions.
    		.thenComposeAsync((oldresult) ->
    		{
    			CompletableFuture<?> newresult = CompletableFuture.completedFuture(oldresult);
    			if (oldresult == null)
    			{
	    			try
	    			{
						newresult = lambda.call();
					}
	    			catch (Exception e1)
	    			{
	    				throw new CompletionException(e1);
					}
    			}
    			return newresult;
    		}, this.hostContext.getExecutor());
    	}
    	// Stage final: trace the exception with the final message, or pass along the valid result.
    	retryChain = retryChain.handleAsync((r,e) ->
    	{
    		if (e != null)
    		{
        		if (partitionId != null)
        		{
        			TRACE_LOGGER.warn(this.hostContext.withHostAndPartition(partitionId, finalFailureMessage));
        		}
        		else
        		{
        			TRACE_LOGGER.warn(this.hostContext.withHost(finalFailureMessage));
        		}
        		throw LoggingUtils.wrapException(new RuntimeException(finalFailureMessage), action);
    		}
    		return (e == null) ? r : null;
    	}, this.hostContext.getExecutor());
    	
    	return retryChain;
    }
    
    private class BoolWrapper
    {
    	public BoolWrapper(boolean init) { this.value = init; }
    	public boolean value;
    }
    
    // Return Void so it can be called from a lambda.
    // throwOnFailure is true 
    private Void scan()
    {
    	TRACE_LOGGER.info(this.hostContext.withHost("Starting lease scan"));

    	// DO NOT check whether this.scanFuture is cancelled. The first execution of this method is scheduled
    	// with 0 delay and can occur before this.scanFuture is set to the result of the schedule() call.

        // These are final so they can be used in the lambdas below.
        final AtomicInteger ourLeasesCount = new AtomicInteger();
        final ConcurrentHashMap<String, Lease> leasesOwnedByOthers = new ConcurrentHashMap<String, Lease>();
        final BoolWrapper resultsAreComplete = new BoolWrapper(true);

        // Stage A: get the list of all leases
        CompletableFuture<Lease> leaseToStealFuture = this.hostContext.getLeaseManager().getAllLeases()
        // Stage B: check the state of each lease in parallel, acquiring those which are expired
        .thenApplyAsync((leaseList) ->
        {
        	ArrayList<CompletableFuture<Lease>> transformedLeases = new ArrayList<CompletableFuture<Lease>>();
        	for (Lease l : leaseList)
        	{
        		final Lease workingLease = l;
        		
        		// Stage B.0: is the lease expired?
        		CompletableFuture<Lease> oneResult = workingLease.isExpired()
        		// Stage B.1: if it is expired, attempt to acquire it.
        		.thenComposeAsync((expired) ->
        		{
        			return expired ? this.hostContext.getLeaseManager().acquireLease(workingLease) : CompletableFuture.completedFuture(false);
        		}, this.hostContext.getExecutor())
        		// Stage B.2: if it was acquired, start a pump and do the counting.
        		.thenApplyAsync((acquired) ->
				{
	        		if (acquired)
	        		{
	        			this.pump.addPump(workingLease);
	        		}
	        		if (workingLease.isOwnedBy(this.hostContext.getHostName()))
	        		{
	        			ourLeasesCount.getAndIncrement(); // count leases owned by this host
	        		}
	        		else
	        		{
	        			leasesOwnedByOthers.put(workingLease.getPartitionId(), workingLease); // save leases owned by other hosts
	        		}
	        		return workingLease;
				}, this.hostContext.getExecutor())
            	// Stage B.3: ALWAYS RUN REGARDLESS OF EXCEPTIONS -- log/notify if exception occurred
            	.whenCompleteAsync((lease, e) ->
            	{
            		if (e != null)
            		{
            			resultsAreComplete.value = false;
                		Exception notifyWith = (Exception)LoggingUtils.unwrapException(e, null);
                		TRACE_LOGGER.warn(this.hostContext.withHost("Failure getting/acquiring lease, skipping"), notifyWith);
                		this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), notifyWith,
                				EventProcessorHostActionStrings.CHECKING_LEASES, ExceptionReceivedEventArgs.NO_ASSOCIATED_PARTITION);
            		}
            	}, this.hostContext.getExecutor());

        		transformedLeases.add(oneResult);
        	}
        	return transformedLeases;
        }, this.hostContext.getExecutor())
        // Stage C: get a future that waits for all the results
        .thenComposeAsync((transformedLeases) ->
        {
            CompletableFuture<?>[] dummy = new CompletableFuture<?>[transformedLeases.size()];
            return CompletableFuture.allOf(transformedLeases.toArray(dummy));
        }, this.hostContext.getExecutor())
        // Stage D: consume the counting done by the per-lease stage to decide whether and what lease to steal
        .thenApplyAsync((empty) ->
        {
        	TRACE_LOGGER.info(this.hostContext.withHost("Lease scan steal check"));
        	
            // Grab more leases if available and needed for load balancing, but only if all leases were checked OK.
            // Don't try to steal if numbers are in doubt due to errors in the previous stage. 
        	Lease stealThisLease = null;
            if ((leasesOwnedByOthers.size() > 0) && resultsAreComplete.value)
            {
	            stealThisLease = whichLeaseToSteal(leasesOwnedByOthers.values(), ourLeasesCount.get());
            }
        	return stealThisLease;
        }, this.hostContext.getExecutor());
        
        // Stage E: if D identified a candidate for stealing, attempt to steal it. Return true on successful stealing, false in all other cases
        leaseToStealFuture.thenComposeAsync((stealThisLease) ->
        {
        	return (stealThisLease != null) ? this.hostContext.getLeaseManager().acquireLease(stealThisLease) : CompletableFuture.completedFuture(false);
        }, this.hostContext.getExecutor())
        // Stage F: consume results from E and D. Start a pump if a lease was stolen.
        .thenCombineAsync(leaseToStealFuture, (stealSucceeded, lease) ->
        {
            if (stealSucceeded)
            {
        		TRACE_LOGGER.info(this.hostContext.withHostAndPartition(lease, "Stole lease"));
        		this.pump.addPump(lease);
            }
            return lease;
        }, this.hostContext.getExecutor())
        // Stage G: ALWAYS RUN REGARDLESS OF EXCEPTIONS -- log/notify, schedule next scan
        .whenCompleteAsync((lease, e) ->
        {
        	if (e != null)
        	{
    			Exception notifyWith = (Exception)LoggingUtils.unwrapException(e, null);
    			if (lease != null)
    			{
	    			TRACE_LOGGER.warn(this.hostContext.withHost("Exception stealing lease for partition " + lease.getPartitionId()), notifyWith);
	    			this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), notifyWith,
	    					EventProcessorHostActionStrings.STEALING_LEASE, lease.getPartitionId());
    			}
    			else
    			{
	    			TRACE_LOGGER.warn(this.hostContext.withHost("Exception stealing lease"), notifyWith);
	    			this.hostContext.getEventProcessorOptions().notifyOfException(this.hostContext.getHostName(), notifyWith,
	    					EventProcessorHostActionStrings.STEALING_LEASE, ExceptionReceivedEventArgs.NO_ASSOCIATED_PARTITION);
    			}
        	}
        	
            onPartitionCheckCompleteTestHook();
            
        	// Schedule the next scan unless the future has been cancelled.
            synchronized (this.scanFutureSynchronizer)
            {
	        	if (!this.scanFuture.isCancelled())
	        	{
	        		int seconds = this.hostContext.getPartitionManagerOptions().getLeaseRenewIntervalInSeconds();
	    	    	this.scanFuture = this.hostContext.getExecutor().schedule(() -> scan(), seconds, TimeUnit.SECONDS);
	    	    	TRACE_LOGGER.info(this.hostContext.withHost("Scheduling lease scanner in " + seconds));
	        	}
            }
        }, this.hostContext.getExecutor());

    	return null;
    }

    private Lease whichLeaseToSteal(Collection<Lease> stealableLeases, int haveLeaseCount)
    {
    	HashMap<String, Integer> countsByOwner = countLeasesByOwner(stealableLeases);
    	String biggestOwner = findBiggestOwner(countsByOwner);
    	int biggestCount = countsByOwner.get(biggestOwner); // HASHMAP
    	Lease stealThisLease = null;
    	
    	// If the number of leases is a multiple of the number of hosts, then the desired configuration is
    	// that all hosts own the name number of leases, and the difference between the "biggest" owner and
    	// any other is 0.
    	//
    	// If the number of leases is not a multiple of the number of hosts, then the most even configuration
    	// possible is for some hosts to have (leases/hosts) leases and others to have ((leases/hosts) + 1).
    	// For example, for 16 partitions distributed over five hosts, the distribution would be 4, 3, 3, 3, 3,
    	// or any of the possible reorderings.
    	//
    	// In either case, if the difference between this host and the biggest owner is 2 or more, then the
    	// system is not in the most evenly-distributed configuration, so steal one lease from the biggest.
    	// If there is a tie for biggest, findBiggestOwner() picks whichever appears first in the list because
    	// it doesn't really matter which "biggest" is trimmed down.
    	//
    	// Stealing one at a time prevents flapping because it reduces the difference between the biggest and
    	// this host by two at a time. If the starting difference is two or greater, then the difference cannot
    	// end up below 0. This host may become tied for biggest, but it cannot become larger than the host that
    	// it is stealing from.
    	
    	if ((biggestCount - haveLeaseCount) >= 2)
    	{
    		for (Lease l : stealableLeases)
    		{
    			if (l.isOwnedBy(biggestOwner))
    			{
    				stealThisLease = l;
    				TRACE_LOGGER.info(this.hostContext.withHost("Proposed to steal lease for partition " + l.getPartitionId() + " from " + biggestOwner));
  					break;
    			}
    		}
    	}
    	return stealThisLease;
    }
    
    private String findBiggestOwner(HashMap<String, Integer> countsByOwner)
    {
    	int biggestCount = 0;
    	String biggestOwner = null;
    	for (String owner : countsByOwner.keySet())
    	{
    		if (countsByOwner.get(owner) > biggestCount) // HASHMAP
    		{
    			biggestCount = countsByOwner.get(owner); // HASHMAP
    			biggestOwner = owner;
    		}
    	}
    	return biggestOwner;
    }
    
    private HashMap<String, Integer> countLeasesByOwner(Iterable<Lease> leases)
    {
    	HashMap<String, Integer> counts = new HashMap<String, Integer>();
    	for (Lease l : leases)
    	{
    		if (counts.containsKey(l.getOwner()))
    		{
    			Integer oldCount = counts.get(l.getOwner()); // HASHMAP
    			counts.put(l.getOwner(), oldCount + 1);
    		}
    		else
    		{
    			counts.put(l.getOwner(), 1);
    		}
    	}
    	for (String owner : counts.keySet())
    	{
    		TRACE_LOGGER.info(this.hostContext.withHost("host " + owner + " owns " + counts.get(owner) + " leases")); // HASHMAP
    	}
    	TRACE_LOGGER.info(this.hostContext.withHost("total hosts in sorted list: " + counts.size()));
    	
    	return counts;
    }
}
