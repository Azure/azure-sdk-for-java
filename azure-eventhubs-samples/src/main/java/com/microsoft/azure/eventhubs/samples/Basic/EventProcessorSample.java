/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.samples.Basic;

/*
 * Until the official release, there is no package distributed for EventProcessorHost, and hence no good
 * portable way of putting a reference to it in the samples POM. Thus, the contents of this sample are
 * commented out by default to avoid blocking or breaking anything. To use this sample, add a dependency
 * on EventProcessorHost, then uncomment.
 */

import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventprocessorhost.CloseReason;
import com.microsoft.azure.eventprocessorhost.EventProcessorHost;
import com.microsoft.azure.eventprocessorhost.EventProcessorOptions;
import com.microsoft.azure.eventprocessorhost.ExceptionReceivedEventArgs;
import com.microsoft.azure.eventprocessorhost.IEventProcessor;
import com.microsoft.azure.eventprocessorhost.PartitionContext;
import com.microsoft.azure.servicebus.ConnectionStringBuilder;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class EventProcessorSample
{
    public static void main(String args[])
    {
    	// SETUP SETUP SETUP SETUP
    	// Fill these strings in with the information of the Event Hub you wish to use. The consumer group
    	// can probably be left as-is. You will also need the connection string for an Azure Storage account,
    	// which is used to persist the lease and checkpoint data for this Event Hub.
    	String consumerGroupName = "$Default";
    	String namespaceName = "----ServiceBusNamespaceName----";
    	String eventHubName = "----EventHubName----";
    	String sasKeyName = "----SharedAccessSignatureKeyName----";
    	String sasKey = "----SharedAccessSignatureKey----";
    	String storageConnectionString = "----AzureStorageConnectionString----";
    	
    	// To conveniently construct the Event Hub connection string from the raw information, use the ConnectionStringBuilder class.
    	ConnectionStringBuilder eventHubConnectionString = new ConnectionStringBuilder(namespaceName, eventHubName, sasKeyName, sasKey);
    	
		// Create the instance of EventProcessorHost using the most basic constructor. This constructor uses Azure Storage for
		// persisting partition leases and checkpoints, with a default Storage container name made from the Event Hub name
		// and consumer group name. The host name (a string that uniquely identifies the instance of EventProcessorHost)
		// is automatically generated as well.
		EventProcessorHost host = new EventProcessorHost(eventHubName, consumerGroupName, eventHubConnectionString.toString(), storageConnectionString);
		
		// Registering an event processor class with an instance of EventProcessorHost starts event processing. The host instance
		// obtains leases on some partitions of the Event Hub, possibly stealing some from other host instances, in a way that
		// converges on an even distribution of partitions across all host instances. For each leased partition, the host instance
		// creates an instance of the provided event processor class, then receives events from that partition and passes them to
		// the event processor instance.
		//
		// There are two error notification systems in EventProcessorHost. Notification of errors tied to a particular partition,
		// such as a receiver failing, are delivered to the event processor instance for that partition via the onError method.
		// Notification of errors not tied to a particular partition, such as initialization failures, are delivered to a general
		// notification handler that is specified via an EventProcessorOptions object. You are not required to provide such a
		// notification handler, but if you don't, then you may not know that certain errors have occurred.
		System.out.println("Registering host named " + host.getHostName());
		EventProcessorOptions options = new EventProcessorOptions();
		options.setExceptionNotification(new ErrorNotificationHandler());
		try
		{
			// The Future returned by the register* APIs completes when initialization is done and
			// message pumping is about to start. It is important to call get() on the Future because
			// initialization failures will result in an ExecutionException, with the failure as the
			// inner exception, and are not otherwise reported.
			host.registerEventProcessor(EventProcessor.class, options).get();
		}
		catch (Exception e)
		{
			System.out.print("Failure while registering: ");
			if (e instanceof ExecutionException)
			{
				Throwable inner = e.getCause();
				System.out.println(inner.toString());
			}
			else
			{
				System.out.println(e.toString());
			}
		}

        System.out.println("Press enter to stop");
        try
        {
            System.in.read();
            
            // Processing of events continues until unregisterEventProcessor is called. Unregistering shuts down the
            // receivers on all currently owned leases, shuts down the instances of the event processor class, and
            // releases the leases for other instances of EventProcessorHost to claim.
            System.out.println("Calling unregister");
            host.unregisterEventProcessor();
            
            // There are two options for shutting down EventProcessorHost's internal thread pool: automatic and manual.
            // Both have their advantages and drawbacks. See the JavaDocs for setAutoExecutorShutdown and forceExecutorShutdown
            // for more details. This example uses forceExecutorShutdown because it is the safe option, at the expense of
            // another line of code.
            System.out.println("Calling forceExecutorShutdown");
            EventProcessorHost.forceExecutorShutdown(120);
        }
        catch(Exception e)
        {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    	
        System.out.println("End of sample");
    }
    
    // The general notification handler is an object that derives from Consumer<> and takes an ExceptionReceivedEventArgs object
    // as an argument. The argument provides the details of the error: the exception that occurred and the action (what EventProcessorHost
    // was doing) during which the error occurred. The complete list of actions can be found in EventProcessorHostActionStrings.
    public static class ErrorNotificationHandler implements Consumer<ExceptionReceivedEventArgs>
    {
		@Override
		public void accept(ExceptionReceivedEventArgs t)
		{
			System.out.println("SAMPLE: Host " + t.getHostname() + " received general error notification during " + t.getAction() + ": " + t.getException().toString());
		}
    }

    public static class EventProcessor implements IEventProcessor
    {
    	private int checkpointBatchingCount = 0;

    	// OnOpen is called when a new event processor instance is created by the host. In a real implementation, this
    	// is the place to do initialization so that events can be processed when they arrive, such as opening a database
    	// connection.
    	@Override
        public void onOpen(PartitionContext context) throws Exception
        {
        	System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is opening");
        }

        // OnClose is called when an event processor instance is being shut down. The reason argument indicates whether the shut down
        // is because another host has stolen the lease for this partition or due to error or host shutdown. In a real implementation,
        // this is the place to do cleanup for resources that were opened in onOpen.
    	@Override
        public void onClose(PartitionContext context, CloseReason reason) throws Exception
        {
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " is closing for reason " + reason.toString());
        }
    	
    	// onError is called when an error occurs in EventProcessorHost code that is tied to this partition, such as a receiver failure.
    	// It is NOT called for exceptions thrown out of onOpen/onClose/onEvents. EventProcessorHost is responsible for recovering from
    	// the error, if possible, or shutting the event processor down if not, in which case there will be a call to onClose. The
    	// notification provided to onError is primarily informational.
    	@Override
    	public void onError(PartitionContext context, Throwable error)
    	{
    		System.out.println("SAMPLE: Partition " + context.getPartitionId() + " onError: " + error.toString());
    	}

    	// onEvents is called when events are received on this partition of the Event Hub. The maximum number of events in a batch
    	// can be controlled via EventProcessorOptions. Also, if the "invoke processor after receive timeout" option is set to true,
    	// this method will be called with null when a receive timeout occurs.
    	@Override
        public void onEvents(PartitionContext context, Iterable<EventData> messages) throws Exception
        {
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " got message batch");
            int messageCount = 0;
            for (EventData data : messages)
            {
                System.out.println("SAMPLE (" + context.getPartitionId() + "," + data.getSystemProperties().getOffset() + "," +
                		data.getSystemProperties().getSequenceNumber() + "): " + new String(data.getBytes(), "UTF8"));
                messageCount++;
                
                // Checkpointing persists the current position in the event stream for this partition and means that the next
                // time any host opens an event processor on this event hub+consumer group+partition combination, it will start
                // receiving at the event after this one. Checkpointing is usually not a fast operation, so there is a tradeoff
                // between checkpointing frequently (to minimize the number of events that will be reprocessed after a crash, or
                // if the partition lease is stolen) and checkpointing infrequently (to reduce the impact on event processing
                // performance). Checkpointing every five events is an arbitrary choice for this sample.
                this.checkpointBatchingCount++;
                if ((checkpointBatchingCount % 5) == 0)
                {
                	System.out.println("SAMPLE: Partition " + context.getPartitionId() + " checkpointing at " +
               			data.getSystemProperties().getOffset() + "," + data.getSystemProperties().getSequenceNumber());
                	context.checkpoint(data);
                }
            }
            System.out.println("SAMPLE: Partition " + context.getPartitionId() + " batch size was " + messageCount + " for host " + context.getOwner());
        }
    }
}



