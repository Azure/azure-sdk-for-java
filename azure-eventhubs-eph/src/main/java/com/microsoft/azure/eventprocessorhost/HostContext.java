/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.eventprocessorhost;

import java.util.concurrent.ScheduledExecutorService;

import com.microsoft.azure.eventhubs.RetryPolicy;

final class HostContext
{
	final private ScheduledExecutorService executor;

	// Ideally we wouldn't need the host, but there are certain things which can be dynamically changed
	// by the user via APIs on the host and which need to be exposed on the HostContext. Passing the
	// call through is easier and safer than trying to keep two copies in sync.
	final private EventProcessorHost host;
	final private String hostName;
	
	final private String eventHubPath;
	final private String consumerGroupName;
	final private String eventHubConnectionString;
	final private RetryPolicy retryPolicy;
	
	final private ILeaseManager leaseManager;
	final private ICheckpointManager checkpointManager;
	
	// Cannot be final because it is not available at HostContext construction time.
	private EventProcessorOptions eventProcessorOptions = null;
	
	// Cannot be final because it is not available at HostContext construction time.
    private IEventProcessorFactory<?> processorFactory = null;

	
	HostContext(ScheduledExecutorService executor,
			EventProcessorHost host, String hostName,
			String eventHubPath, String consumerGroupName, String eventHubConnectionString, RetryPolicy retryPolicy,
			ILeaseManager leaseManager, ICheckpointManager checkpointManager)
	{
		this.executor = executor;
		
		this.host = host;
		this.hostName = hostName;
		
		this.eventHubPath = eventHubPath;
		this.consumerGroupName = consumerGroupName;
		this.eventHubConnectionString = eventHubConnectionString;
		this.retryPolicy = retryPolicy;
		
		this.leaseManager = leaseManager;
		this.checkpointManager = checkpointManager;
	}
	
	ScheduledExecutorService getExecutor() { return this.executor; }
	
	String getHostName() { return this.hostName; }
	
	String getEventHubPath() { return this.eventHubPath; }
	
	String getConsumerGroupName() { return this.consumerGroupName; }
	
	String getEventHubConnectionString() { return this.eventHubConnectionString; }
	
	RetryPolicy getRetryPolicy() { return this.retryPolicy; }
	
	ILeaseManager getLeaseManager() { return this.leaseManager; }
	
	ICheckpointManager getCheckpointManager() { return this.checkpointManager; }
	
	PartitionManagerOptions getPartitionManagerOptions() { return this.host.getPartitionManagerOptions(); }
	
	
	void setEventProcessorOptions(EventProcessorOptions epo) { this.eventProcessorOptions = epo; }
	
	EventProcessorOptions getEventProcessorOptions() { return this.eventProcessorOptions; }
	
	void setEventProcessorFactory(IEventProcessorFactory<?> pf) { this.processorFactory = pf; }
	
	IEventProcessorFactory<?> getEventProcessorFactory() { return this.processorFactory; }
	
	
    String withHost(String logMessage)
    {
        return "host " + this.hostName + ": " + logMessage;
    }

    String withHostAndPartition(String partitionId, String logMessage)
    {
        return withHost(partitionId + ": " + logMessage);
    }

    String withHostAndPartition(PartitionContext context, String logMessage)
    {
    	return withHostAndPartition(context.getPartitionId(), logMessage);
    }
    
    String withHostAndPartition(Lease lease, String logMessage)
    {
    	return withHostAndPartition(lease.getPartitionId(), logMessage);
    }
}
