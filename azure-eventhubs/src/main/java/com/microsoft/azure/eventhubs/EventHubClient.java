/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.io.*;
import java.nio.channels.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import com.microsoft.azure.servicebus.*;

/**
 * Anchor class - all EventHub client operations STARTS here.
 * @see To create an instance of EventHubClient refer to {@link EventHubClient#createFromConnectionString(String)}. 
 */
public class EventHubClient extends ClientEntity
{
	public static final String DEFAULT_CONSUMER_GROUP_NAME = "$Default";
	
	private final String eventHubName;
	private final Object senderCreateSync;
	
	private MessagingFactory underlyingFactory;
	private MessageSender sender;
	private boolean isSenderCreateStarted;
	private CompletableFuture<Void> createSender;
	
	private EventHubClient(ConnectionStringBuilder connectionString) throws IOException, IllegalEntityException
	{
		super(StringUtil.getRandomString());
		this.eventHubName = connectionString.getEntityPath();
		this.senderCreateSync = new Object();
	}
	
	/**
	 * Synchronous version of {@link #createFromConnectionString(String)}. 
	 * @param connectionString The connection string to be used. See {@link ConnectionStringBuilder} to construct a connectionString.
	 * @return EventHubClient which can be used to create Senders and Receivers to EventHub
	 * @throws ServiceBusException If Service Bus service encountered problems during connection creation. 
     * @throws IOException  If the underlying Proton-J layer encounter network errors.
	 */
	public static EventHubClient createFromConnectionStringSync(final String connectionString)
			throws ServiceBusException, IOException
	{
        try
        {
		  return createFromConnectionString(connectionString).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}

		return null;
	}
	
	/**
	 * Factory method to create an instance of {@link EventHubClient} using the supplied connectionString.
	 * In a normal scenario (when re-direct is not enabled) - one EventHubClient instance maps to one Connection to the Azure ServiceBus EventHubs service.
	 * 
	 * <p>The {@link EventHubClient} created from this method creates a Sender instance internally, which is used by the {@link #send(EventData)} methods.
	 * 
	 * @param connectionString The connection string to be used. See {@link ConnectionStringBuilder} to construct a connectionString.
	 * @return EventHubClient which can be used to create Senders and Receivers to EventHub
	 * @throws ServiceBusException If Service Bus service encountered problems during connection creation. 
     * @throws IOException  If the underlying Proton-J layer encounter network errors.
	 */
	public static CompletableFuture<EventHubClient> createFromConnectionString(final String connectionString)
			throws ServiceBusException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(connectionString);
		final EventHubClient eventHubClient = new EventHubClient(connStr);
		
		return MessagingFactory.createFromConnectionString(connectionString.toString())
				.thenApplyAsync(new Function<MessagingFactory, EventHubClient>()
				{
					@Override
					public EventHubClient apply(MessagingFactory factory)
					{
						eventHubClient.underlyingFactory = factory;
						return eventHubClient;
					}
				});
	}
	
    /**
	 * Synchronous version of {@link #send(EventData)}. 
	 * @param data the {@link EventData} to be sent.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
	 */
    public final void sendSync(final EventData data) 
			throws ServiceBusException
	{
        try
        {
            this.send(data).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
    }
    
	/**
	 * Send {@link EventData} to EventHub. The sent {@link EventData} will land on any arbitrarily chosen EventHubs partition. 
	 * 
	 * <p>There are 3 ways to send to EventHubs, each exposed as a method (along with its sendBatch overload):
	 * <ul>
	 * <li>	{@link #send(EventData)} or {@link #send(Iterable)}
	 * <li>	{@link #send(EventData, String)} or {@link #send(Iterable, String)}
	 * <li>	{@link PartitionSender#send(EventData)} or {@link PartitionSender#send(Iterable)}
	 * </ul>
	 * <p>Use this method to Send, if:
	 * <pre>
	 * a)  the send({@link EventData}) operation should be highly available and 
	 * b)  the data needs to be evenly distributed among all partitions; exception being, when a subset of partitions are unavailable
	 * </pre>
	 * 
	 * {@link #send(EventData)} send's the {@link EventData} to a Service Gateway, which in-turn will forward the {@link EventData} to one of the EventHubs' partitions. Here's the message forwarding algorithm:
	 * <pre>
	 * i.  Forward the {@link EventData}'s to EventHub partitions, by equally distributing the data among all partitions (ex: Round-robin the {@link EventData}'s to all EventHubs' partitions) 
	 * ii. If one of the EventHub partitions is unavailable for a moment, the Service Gateway will automatically detect it and forward the message to another available partition - making the Send operation highly-available.
	 * </pre>
	 * @param data the {@link EventData} to be sent.
	 * @return     a CompletableFuture that can be completed when the send operations is done..
	 * @throws PayloadSizeExceededException    if the total size of the {@link EventData} exceeds a predefined limit set by the service. Default is 256k bytes.
	 * @throws ServiceBusException             if Service Bus service encountered problems during the operation.
	 * @throws UnresolvedAddressException      if there are Client to Service network connectivity issues, if the Azure DNS resolution of the ServiceBus Namespace fails (ex: namespace deleted etc.) 
	 * @see #send(EventData, String)
	 * @see PartitionSender#send(EventData) 
	 */
	public final CompletableFuture<Void> send(final EventData data) 
			throws ServiceBusException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("EventData cannot be empty.");
		}
		
		return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>()
		{
			@Override
			public CompletableFuture<Void> apply(Void voidArg)
			{
				return EventHubClient.this.sender.send(data.toAmqpMessage());
			}
		});
	}
	
    /**
	 * Synchronous version of {@link #send(Iterable)}. 
	 * @param eventDatas batch of events to send to EventHub
	 * @throws ServiceBusException	if Service Bus service encountered problems during the operation.
	 */
    public final void sendSync(final Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
        try
        {
            this.send(eventDatas).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
    }
	
	/**
	 * Send a batch of {@link EventData} to EventHub. The sent {@link EventData} will land on any arbitrarily chosen EventHubs partition.
	 * This is the most recommended way to Send to EventHubs. 
	 * 
	 * <p>There are 3 ways to send to EventHubs, to understand this particular type of Send refer to the overload {@link #send(EventData)}, which is used to send single {@link EventData}.
	 * Use this overload versus {@link #send(EventData)}, if you need to send a batch of {@link EventData}.
	 * 
	 * <p> Sending a batch of {@link EventData}'s is useful in the following cases:
	 * <pre>
	 * i.	Efficient send - sending a batch of {@link EventData} maximizes the overall throughput by optimally using the number of sessions created to EventHubs' service.
	 * ii.	Send multiple {@link EventData}'s in a Transaction. To achieve ACID properties, the Gateway Service will forward all {@link EventData}'s in the batch to a single EventHubs' partition.
	 * </pre>
     * <p>
     * Sample code (sample uses sync version of the api but concept are identical):
     * <pre>       
     * Gson gson = new GsonBuilder().create();
     * EventHubClient client = EventHubClient.createFromConnectionStringSync("__connection__");
     *         
     * while (true)
     * {
     *     LinkedList{@literal<}EventData{@literal>} events = new LinkedList{@literal<}EventData{@literal>}();}
     *     for (int count = 1; count {@literal<} 11; count++)
     *     {
     *         PayloadEvent payload = new PayloadEvent(count);
     *         byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
     *         EventData sendEvent = new EventData(payloadBytes);
     *         Map{@literal<}String, String{@literal>} applicationProperties = new HashMap{@literal<}String, String{@literal>}();
     *         applicationProperties.put("from", "javaClient");
     *         sendEvent.setProperties(applicationProperties);
     *         events.add(sendEvent);
     *     }
     *         
     *     client.sendSync(events);
     *     System.out.println(String.format("Sent Batch... Size: %s", events.size()));
     * }
     * </pre>
	 * 
	 * @param eventDatas batch of events to send to EventHub
	 * @return     a CompletableFuture that can be completed when the send operations is done..
	 * @throws PayloadSizeExceededException    if the total size of the {@link EventData} exceeds a pre-defined limit set by the service. Default is 256k bytes.
	 * @throws ServiceBusException             if Service Bus service encountered problems during the operation.
	 * @throws UnresolvedAddressException      if there are Client to Service network connectivity issues, if the Azure DNS resolution of the ServiceBus Namespace fails (ex: namespace deleted etc.)
	 * @see #send(EventData, String)
	 * @see PartitionSender#send(EventData) 
	 */
	public final CompletableFuture<Void> send(final Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
		if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0))
		{
			throw new IllegalArgumentException("Empty batch of EventData cannot be sent.");
		}
		
		return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>()
		{
			@Override
			public CompletableFuture<Void> apply(Void voidArg)
			{
				return EventHubClient.this.sender.send(EventDataUtil.toAmqpMessages(eventDatas));
			}
		});
	}
	
    /**
	 * Synchronous version of {@link #send(EventData, String)}. 
	 * @param eventData the {@link EventData} to be sent.
	 * @param partitionKey the partitionKey will be hash'ed to determine the partitionId to send the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}
	 * @throws ServiceBusException	if Service Bus service encountered problems during the operation.
	 */
    public final void sendSync(final EventData eventData, final String partitionKey) 
			throws ServiceBusException
	{
        try
        {
            this.send(eventData, partitionKey).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
    }
	
	/**
	 * Send an '{@link EventData} with a partitionKey' to EventHub. All {@link EventData}'s with a partitionKey are guaranteed to land on the same partition.
	 * This send pattern emphasize data correlation over general availability and latency.
	 * <p>
	 * There are 3 ways to send to EventHubs, each exposed as a method (along with its sendBatch overload):
	 * <pre>
	 * i.   {@link #send(EventData)} or {@link #send(Iterable)}
	 * ii.  {@link #send(EventData, String)} or {@link #send(Iterable, String)}
	 * iii. {@link PartitionSender#send(EventData)} or {@link PartitionSender#send(Iterable)}
	 * </pre>
	 * 
	 * Use this type of Send, if:
	 * <pre>
	 * i.  There is a need for correlation of events based on Sender instance; The sender can generate a UniqueId and set it as partitionKey - which on the received Message can be used for correlation
	 * ii. The client wants to take control of distribution of data across partitions.
	 * </pre>
	 * <p>
	 * Multiple PartitionKey's could be mapped to one Partition. EventHubs service uses a proprietary Hash algorithm to map the PartitionKey to a PartitionId.
	 * Using this type of Send (Sending using a specific partitionKey), could sometimes result in partitions which are not evenly distributed. 
	 * 
	 * @param eventData the {@link EventData} to be sent.
	 * @param partitionKey the partitionKey will be hash'ed to determine the partitionId to send the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}
	 * @return     a CompletableFuture that can be completed when the send operations is done..
	 * @throws PayloadSizeExceededException    if the total size of the {@link EventData} exceeds a pre-defined limit set by the service. Default is 256k bytes.
	 * @throws ServiceBusException             if Service Bus service encountered problems during the operation.
	 * @throws UnresolvedAddressException      if there are Client to Service network connectivity issues, if the Azure DNS resolution of the ServiceBus Namespace fails (ex: namespace deleted etc.)
	 * @see #send(EventData)
	 * @see PartitionSender#send(EventData)
	 */
	public final CompletableFuture<Void> send(final EventData eventData, final String partitionKey) 
			throws ServiceBusException
	{
		if (eventData == null)
		{
			throw new IllegalArgumentException("EventData cannot be null.");
		}
		
		if (partitionKey == null)
		{
			throw new IllegalArgumentException("partitionKey cannot be null");
		}
				
		return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>()
		{
			@Override
			public CompletableFuture<Void> apply(Void voidArg)
			{
				return EventHubClient.this.sender.send(eventData.toAmqpMessage(partitionKey));
			}
		});
	}
	
    /**
	 * Synchronous version of {@link #send(Iterable, String)}. 
	 * @param eventDatas the batch of events to send to EventHub
	 * @param partitionKey the partitionKey will be hash'ed to determine the partitionId to send the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}
	 * @throws ServiceBusException             if Service Bus service encountered problems during the operation.
	 */
    public final void sendSync(final Iterable<EventData> eventDatas, final String partitionKey) 
			throws ServiceBusException
	{
        try
        {
            this.send(eventDatas, partitionKey).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
    }
	
	/**
	 * Send a 'batch of {@link EventData} with the same partitionKey' to EventHub. All {@link EventData}'s with a partitionKey are guaranteed to land on the same partition.
	 * Multiple PartitionKey's will be mapped to one Partition.
	 * 
	 * <p>There are 3 ways to send to EventHubs, to understand this particular type of Send refer to the overload {@link #send(EventData, String)}, which is the same type of Send and is used to send single {@link EventData}.
	 * 
	 * <p>Sending a batch of {@link EventData}'s is useful in the following cases:
	 * <pre>
	 * i.	Efficient send - sending a batch of {@link EventData} maximizes the overall throughput by optimally using the number of sessions created to EventHubs service.
	 * ii.	Send multiple events in One Transaction. This is the reason why all events sent in a batch needs to have same partitionKey (so that they are sent to one partition only).
	 * </pre>
	 * 
	 * @param eventDatas the batch of events to send to EventHub
	 * @param partitionKey the partitionKey will be hash'ed to determine the partitionId to send the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}
	 * @return     a CompletableFuture that can be completed when the send operations is done..
	 * @throws PayloadSizeExceededException    if the total size of the {@link EventData} exceeds a pre-defined limit set by the service. Default is 256k bytes.
	 * @throws ServiceBusException             if Service Bus service encountered problems during the operation.
	 * @throws UnresolvedAddressException      if there are Client to Service network connectivity issues, if the Azure DNS resolution of the ServiceBus Namespace fails (ex: namespace deleted etc.)
	 * @see #send(EventData)
	 * @see PartitionSender#send(EventData) 
	 */
	public final CompletableFuture<Void> send(final Iterable<EventData> eventDatas, final String partitionKey)
	{
		if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0))
		{
			throw new IllegalArgumentException("Empty batch of EventData cannot be sent.");
		}
		
		if (partitionKey == null)
		{
			throw new IllegalArgumentException("partitionKey cannot be null");
		}
		
		if (partitionKey.length() > ClientConstants.MAX_PARTITION_KEY_LENGTH)
		{
			throw new IllegalArgumentException(
					String.format(Locale.US, "PartitionKey exceeds the maximum allowed length of partitionKey: {0}", ClientConstants.MAX_PARTITION_KEY_LENGTH));
		}
		
		return this.createInternalSender().thenComposeAsync(new Function<Void, CompletableFuture<Void>>()
		{
			@Override
			public CompletableFuture<Void> apply(Void voidArg)
			{
				return EventHubClient.this.sender.send(EventDataUtil.toAmqpMessages(eventDatas, partitionKey));
			}
		});
	}
    
    /**
	 * Synchronous version of {@link #createPartitionSender(String)}. 
	 * @param partitionId  partitionId of EventHub to send the {@link EventData}'s to
	 * @throws ServiceBusException if Service Bus service encountered problems during connection creation. 
     */
	public final PartitionSender createPartitionSenderSync(final String partitionId)
		throws ServiceBusException, IllegalArgumentException
	{
        try
        {
		  return this.createPartitionSender(partitionId).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}

		return null;
	}
	
	/**
	 * Create a {@link PartitionSender} which can publish {@link EventData}'s directly to a specific EventHub partition (sender type iii. in the below list).
	 * <p>
	 * There are 3 patterns/ways to send to EventHubs:
	 * <pre>
	 * i.   {@link #send(EventData)} or {@link #send(Iterable)}
	 * ii.  {@link #send(EventData, String)} or {@link #send(Iterable, String)}
	 * iii. {@link PartitionSender#send(EventData)} or {@link PartitionSender#send(Iterable)}
	 * </pre>
     *
	 * @param partitionId  partitionId of EventHub to send the {@link EventData}'s to
	 * @return             a CompletableFuture that would result in a PartitionSender when it is completed.
	 * @throws ServiceBusException if Service Bus service encountered problems during connection creation. 
     * @see PartitionSender 
	 */
	public final CompletableFuture<PartitionSender> createPartitionSender(final String partitionId)
		throws ServiceBusException
	{
		return PartitionSender.Create(this.underlyingFactory, this.eventHubName, partitionId);
	}
	
    /**
	 * Synchronous version of {@link #createReceiver(String, String, String)}. 
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
     * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @return                     PartitionReceiver instance which can be used for receiving {@link EventData}.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     */
    public final PartitionReceiver createReceiverSync(final String consumerGroupName, final String partitionId, final String startingOffset) 
			throws ServiceBusException
	{
        try
        {
            return this.createReceiver(consumerGroupName, partitionId, startingOffset).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
        
		return null;
    }
	
	/**
	 * The receiver is created for a specific EventHub partition from the specific consumer group.
	 * 
	 * <p>NOTE: There can be a maximum number of receivers that can run in parallel per ConsumerGroup per Partition. 
	 * The limit is enforced by the Event Hub service - current limit is 5 receivers in parallel. Having multiple receivers 
	 * reading from offsets that are far apart on the same consumer group / partition combo will have significant performance Impact.   
	 * 
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
     * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @return                     a CompletableFuture that would result in a PartitionReceiver isntance when it is completed.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     * @see PartitionReceiver
	 */
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset) 
			throws ServiceBusException
	{
		return this.createReceiver(consumerGroupName, partitionId, startingOffset, false);
	}
	
    /**
	 * Synchronous version of {@link #createReceiver(String, String, String, boolean)}. 
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
     * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @param offsetInclusive      if set to true, the startingOffset is treated as an inclusive offset - meaning the first event returned is the one that has the starting offset. Normally first event returned is the event after the starting offset.
	 * @return                     PartitionReceiver instance which can be used for receiving {@link EventData}.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     */
    public final PartitionReceiver createReceiverSync(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive) 
			throws ServiceBusException
	{
        try
        {
            return this.createReceiver(consumerGroupName, partitionId, startingOffset, offsetInclusive).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
        
		return null;
    }
	
	/**
	 * Create the EventHub receiver with given partition id and start receiving from the specified starting offset.
	 * The receiver is created for a specific EventHub Partition from the specific consumer group.
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
     * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @param offsetInclusive      if set to true, the startingOffset is treated as an inclusive offset - meaning the first event returned is the one that has the starting offset. Normally first event returned is the event after the starting offset.
	 * @return                     a CompletableFuture that would result in a PartitionReceiver instance when it is completed.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     * @see PartitionReceiver
	 */
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive) 
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, null, PartitionReceiver.NULL_EPOCH, false);
	}
	
    /**
	 * Synchronous version of {@link #createReceiver(String, String, Instant)}. 
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
	 * @param dateTime             the date time instant that receive operations will start receive events from. Events received will have {@link EventData#SystemProperties#getEnqueuedTime()} later than this Instant.
	 * @return                     PartitionReceiver instance which can be used for receiving {@link EventData}.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     */
    public final PartitionReceiver createReceiverSync(final String consumerGroupName, final String partitionId, final Instant dateTime) 
			throws ServiceBusException
	{
        try
        {
            return this.createReceiver(consumerGroupName, partitionId, dateTime).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
        
		return null;
    }
	
	/**
	 * Create the EventHub receiver with given partition id and start receiving from the specified starting offset.
	 * The receiver is created for a specific EventHub Partition from the specific consumer group.
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
	 * @param dateTime             the date time instant that receive operations will start receive events from. Events received will have {@link EventData#SystemProperties#getEnqueuedTime()} later than this Instant.
	 * @return                     a CompletableFuture that would result in a PartitionReceiver when it is completed.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     * @see PartitionReceiver
	 */	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final Instant dateTime)
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, null, false, dateTime, PartitionReceiver.NULL_EPOCH, false);
	}
	
    /**
	 * Synchronous version of {@link #createEpochReceiver(String, String, String, long)}. 
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
	 * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @param epoch                an unique identifier (epoch value) that the service uses, to enforce partition/lease ownership. 
	 * @return                     PartitionReceiver instance which can be used for receiving {@link EventData}.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     */
    public final PartitionReceiver createEpochReceiverSync(final String consumerGroupName, final String partitionId, final String startingOffset, final long epoch) 
			throws ServiceBusException
	{
        try
        {
            return this.createEpochReceiver(consumerGroupName, partitionId, startingOffset, epoch).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
        
		return null;
    }
	
	/**
	 * Create a Epoch based EventHub receiver with given partition id and start receiving from the beginning of the partition stream.
	 * The receiver is created for a specific EventHub Partition from the specific consumer group.
     * <p> 
     * It is important to pay attention to the following when creating epoch based receiver:
     * <ul>
     * <li> Ownership enforcement - Once you created an epoch based receiver, you cannot create a non-epoch receiver to the same consumerGroup-Partition combo until all receivers to the combo are closed.
     * <li> Ownership stealing - If a receiver with higher epoch value is created for a consumerGroup-Partition combo, any older epoch receiver to that combo will be force closed.
     * <li> Any receiver closed due to lost of ownership to a consumerGroup-Partition combo will get ReceiverDisconnectedException for all operations from that receiver.
     * </ul>
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
     * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @param epoch                an unique identifier (epoch value) that the service uses, to enforce partition/lease ownership. 
	 * @return                     a CompletableFuture that would result in a PartitionReceiver when it is completed.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     * @see PartitionReceiver
     * @see ReceiverDisconnectedException
	 */	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, final long epoch)
			throws ServiceBusException
	{
		return this.createEpochReceiver(consumerGroupName, partitionId, startingOffset, false, epoch);
	}
	
    /**
	 * Synchronous version of {@link #createEpochReceiver(String, String, String, boolean, long)}. 
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
     * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}     
	 * @param offsetInclusive      if set to true, the startingOffset is treated as an inclusive offset - meaning the first event returned is the one that has the starting offset. Normally first event returned is the event after the starting offset.
	 * @param epoch                an unique identifier (epoch value) that the service uses, to enforce partition/lease ownership. 
	 * @return                     PartitionReceiver instance which can be used for receiving {@link EventData}.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     */
    public final PartitionReceiver createEpochReceiverSync(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive, final long epoch) 
			throws ServiceBusException
	{
        try
        {
            return this.createEpochReceiver(consumerGroupName, partitionId, startingOffset, offsetInclusive, epoch).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
        
		return null;
    }
	
	/**
	 * Create a Epoch based EventHub receiver with given partition id and start receiving from the beginning of the partition stream.
	 * The receiver is created for a specific EventHub Partition from the specific consumer group.
     * <p> 
     * It is important to pay attention to the following when creating epoch based receiver:
     * <ul>
     * <li> Ownership enforcement - Once you created an epoch based receiver, you cannot create a non-epoch receiver to the same consumerGroup-Partition combo until all receivers to the combo are closed.
     * <li> Ownership stealing - If a receiver with higher epoch value is created for a consumerGroup-Partition combo, any older epoch receiver to that combo will be force closed.
     * <li> Any receiver closed due to lost of ownership to a consumerGroup-Partition combo will get ReceiverDisconnectedException for all operations from that receiver.
     * </ul>
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
     * @param startingOffset       the offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}     
	 * @param offsetInclusive      if set to true, the startingOffset is treated as an inclusive offset - meaning the first event returned is the one that has the starting offset. Normally first event returned is the event after the starting offset.
	 * @param epoch                an unique identifier (epoch value) that the service uses, to enforce partition/lease ownership. 
	 * @return                     a CompletableFuture that would result in a PartitionReceiver when it is completed.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     * @see PartitionReceiver
     * @see ReceiverDisconnectedException
	 */	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive, final long epoch)
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, null, epoch, true);
	}
	
    /**
	 * Synchronous version of {@link #createEpochReceiver(String, String, Instant, long)}. 
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
	 * @param dateTime             the date time instant that receive operations will start receive events from. Events received will have {@link EventData#SystemProperties#getEnqueuedTime()} later than this Instant.
	 * @param epoch                an unique identifier (epoch value) that the service uses, to enforce partition/lease ownership. 
	 * @return                     PartitionReceiver instance which can be used for receiving {@link EventData}.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     */
    public final PartitionReceiver createEpochReceiverSync(final String consumerGroupName, final String partitionId, final Instant dateTime, final long epoch) 
			throws ServiceBusException
	{
        try
        {
            return this.createEpochReceiver(consumerGroupName, partitionId, dateTime, epoch).get();
        }
		catch (InterruptedException|ExecutionException exception)
		{
            if (exception instanceof InterruptedException)
            {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }
            
			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}
				
				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}
				                
				throw new ServiceBusException(true, throwable);
			}
		}
        
		return null;
    }
	
	/**
	 * Create a Epoch based EventHub receiver with given partition id and start receiving from the beginning of the partition stream.
	 * The receiver is created for a specific EventHub Partition from the specific consumer group.
     * <p> 
     * It is important to pay attention to the following when creating epoch based receiver:
     * <ul>
     * <li> Ownership enforcement - Once you created an epoch based receiver, you cannot create a non-epoch receiver to the same consumerGroup-Partition combo until all receivers to the combo are closed.
     * <li> Ownership stealing - If a receiver with higher epoch value is created for a consumerGroup-Partition combo, any older epoch receiver to that combo will be force closed.
     * <li> Any receiver closed due to lost of ownership to a consumerGroup-Partition combo will get ReceiverDisconnectedException for all operations from that receiver.
     * </ul>
	 * @param consumerGroupName    the consumer group name that this receiver should be grouped under.
	 * @param partitionId          the partition Id that the receiver belongs to. All data received will be from this partition only.
	 * @param dateTime             the date time instant that receive operations will start receive events from. Events received will have {@link EventData#SystemProperties#getEnqueuedTime()} later than this Instant.
	 * @param epoch                an unique identifier (epoch value) that the service uses, to enforce partition/lease ownership. 
	 * @return                     a CompletableFuture that would result in a PartitionReceiver when it is completed.
	 * @throws ServiceBusException if Service Bus service encountered problems during the operation.
     * @see PartitionReceiver
     * @see ReceiverDisconnectedException
	 */	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final Instant dateTime, final long epoch)
		throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory,  this.eventHubName, consumerGroupName, partitionId, null, false, dateTime, epoch, true);
	}
	
	@Override
	public CompletableFuture<Void> close()
	{
        if (this.underlyingFactory != null)
		{
			return this.underlyingFactory.close();
		}
		
		return CompletableFuture.completedFuture(null);
	}
	
	private CompletableFuture<Void> createInternalSender()
	{
		if (!this.isSenderCreateStarted)
		{
			synchronized (this.senderCreateSync)
			{
				if (!this.isSenderCreateStarted)
				{
					this.isSenderCreateStarted = true;
					this.createSender = MessageSender.create(this.underlyingFactory, StringUtil.getRandomString(), this.eventHubName)
							.thenAcceptAsync(new Consumer<MessageSender>()
							{
								public void accept(MessageSender a) { EventHubClient.this.sender = a;}
							});
				}
			}
		}
		
		return this.createSender;
	}
}
