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
 * Anchor class for all EventHub client operations. All EventHub client operations START here.
 * 
 * @see to create an instance of EventHubClient refer {@link EventHubClient#createFromConnectionString(String)}. 
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
	 * Factory method to create an instance of {@link EventHubClient} using the supplied connectionString.
	 * In a normal scenario (when re-direct is not enabled) - one EventHubClient instance maps to one Connection to the Azure ServiceBus EventHubs service.
	 * 
	 * <p>The {@link EventHubClient} created from this method creates a Sender instance internally, which is used by the {@link #send(EventData)} methods.
	 * 
	 * @param connectionString The connection string to be used. See {@link ConnectionStringBuilder} to construct a connectionString.
	 * @throws ServiceBusException
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
	 * Send {@link EventData} to EventHub. The sent {@link EventData} will land on any arbitrarily chosen EventHubs partition. 
	 * 
	 * <p>There are 3 ways to send to EventHubs, each exposed as a method (along with its sendBatch overload):
	 * <pre>
	 * i.   {@link #send(EventData)} or {@link #send(Iterable)}
	 * ii.  {@link #send(EventData, String)} or {@link #send(Iterable, String)}
	 * iii. {@link PartitionSender#send(EventData)} or {@link PartitionSender#send(Iterable)}
	 * </pre>
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
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} exceeds 256k bytes
	 * @throws ServiceBusException
	 * @see {@link #send(EventData, String)}
	 * @see {@link PartitionSender#send(EventData)} 
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
	 * 
	 * @param eventDatas batch of events to send to EventHub
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} collection exceeds 256k bytes
	 * @throws ServiceBusException
	 * @see {@link #close()}
	 * @see {@link #send(EventData, String)}
	 * @see {@link PartitionSender#send(EventData)} 
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
	 * Send an '{@link EventData} with a partitionKey' to EventHub. All {@link EventData}'s with a partitionKey are guaranteed to land on the same partition.
	 * Sending 
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
	 * 
	 * <p>Multiple PartitionKey's could be mapped to one Partition. EventHubs service uses a proprietary Hash algorithm to map the PartitionKey to a PartitionId.
	 * Using this type of Send (Sending using a specific partitionKey), could sometimes result in partitions which are not evenly distributed. 
	 * 
	 * @param eventData the {@link EventData} to be sent.
	 * @param partitionKey the partitionKey will be hash'ed to determine the 'EventHubs partitionId' to deliver the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}.
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} exceeds 256 K.bytes
	 * @throws ServiceBusException
	 * @see {@link #close()}
	 * @see {@link #send(EventData)}
	 * @see {@link PartitionSender#send(EventData)} 
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
	 * @param partitionKey the partitionKey will be hash'ed to determine the 'EventHubs partitionId' to deliver the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}.
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData}'s exceeds 256k bytes
	 * @throws ServiceBusException
	 * @see {@link #send(EventData)}
	 * @see {@link PartitionSender#send(EventData)} 
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
	 * Create a {@link PartitionSender} which can publish {@link EventData}'s directly to a specific EventHub partition (sender type iii. in the below list).
	 * <p>
	 * There are 3 patterns/ways to send to EventHubs:
	 * <pre>
	 * i.   {@link #send(EventData)} or {@link #send(Iterable)}
	 * ii.  {@link #send(EventData, String)} or {@link #send(Iterable, String)}
	 * iii. {@link PartitionSender#send(EventData)} or {@link PartitionSender#send(Iterable)}
	 * </pre>
	 *   
	 * @param partitionId partitionId of EventHub to send the {@link EventData}'s to
	 * @return
	 * @throws ServiceBusException
	 */
	public final CompletableFuture<PartitionSender> createPartitionSender(final String partitionId)
		throws ServiceBusException
	{
		return PartitionSender.Create(this.underlyingFactory, this.eventHubName, partitionId);
	}
	
	/**
	 * Create the EventHub receiver with given partition id and start receiving from the specified starting offset.
	 * The receiver is created on a specific consumerGroup on a specific EventHub Partition.
	 * 
	 * <p>There can be a Maximum of 5 receiver's in parallel per ConsumerGroup per Partition. 
	 * Having many receivers reading at faraway offsets will have significant performance Impact.   
	 * 
	 * @param consumerGroupName consumer group name
	 * @param partitionId partition Id to start receiving events from.
	 * @param startingOffset offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @param offsetInclusive if set to true, the startingOffset is treated as an inclusive offset - meaning the first event returned is the one that has the starting offset. Normally first event returned is the event after the starting offset.
	 * @return
	 * @throws ServiceBusException
	 */
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive) 
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, null, PartitionReceiver.NULL_EPOCH, false);
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final Instant dateTime)
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, null, false, dateTime, PartitionReceiver.NULL_EPOCH, false);
	}
	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive, final long epoch)
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, null, epoch, true);
	}
	
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
