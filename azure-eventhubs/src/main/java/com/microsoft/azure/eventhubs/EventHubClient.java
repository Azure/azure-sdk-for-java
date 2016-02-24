/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
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
	
	private MessagingFactory underlyingFactory;
	private final String eventHubName;
	
	private MessageSender sender;
	
	private EventHubClient(ConnectionStringBuilder connectionString) throws IOException, IllegalEntityException
	{
		super(StringUtil.getRandomString());
		this.eventHubName = connectionString.getEntityPath();
	}
	
	public static CompletableFuture<EventHubClient> createFromConnectionString(final String connectionString,
			final boolean isReceiveOnly)
			throws ServiceBusException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(connectionString);
		final EventHubClient eventHubClient = new EventHubClient(connStr);
				
		if (isReceiveOnly)
		{
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
		else 
		{
			return MessagingFactory.createFromConnectionString(connectionString.toString())
					.thenComposeAsync(new Function<MessagingFactory, CompletableFuture<EventHubClient>>()
					{
						@Override
						public CompletableFuture<EventHubClient> apply(MessagingFactory factory)
						{
							eventHubClient.underlyingFactory = factory;
							return eventHubClient.createInternalSender()
									.thenApplyAsync(new Function<Void, EventHubClient>()
									{
										@Override
										public EventHubClient apply(Void a)
										{
											return eventHubClient;
										}
									});
						}
					});			
		}
	}
	
	/**
	 * Factory method to create an instance of {@link EventHubClient} using the supplied connectionString.
	 * In a normal scenario (when re-direct is not enabled) - one EventHubClient instance maps to one Connection to the Azure ServiceBus EventHubs service.
	 * The {@link EventHubClient} created from this method creates a Sender instance internally, which is used by the {@link #send(EventData)} methods.
	 * 
	 * @param connectionString The connection string to be used. See {@link ConnectionStringBuilder} to construct a connectionString.
	 * @throws ServiceBusException
	 */
	public static CompletableFuture<EventHubClient> createFromConnectionString(final String connectionString)
			throws ServiceBusException, IOException
	{
		return EventHubClient.createFromConnectionString(connectionString, false);
	}
	
	CompletableFuture<Void> createInternalSender()
	{
		return MessageSender.Create(this.underlyingFactory, StringUtil.getRandomString(), this.eventHubName)
				.thenAcceptAsync(new Consumer<MessageSender>()
				{
					public void accept(MessageSender a) { EventHubClient.this.sender = a;}
				});
	}
	
	/**
	 * Create an {@link PartitionSender} which can publish {@link EventData}'s directly to a specific EventHub partition
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
	 * TODO: return partitionInfo
	 * @throws ServiceBusException
	 */
	public final String getPartitionInfo()
			throws ServiceBusException
	{
		throw new UnsupportedOperationException("TODO: Implement over http");
	}
	
	/**
	 * Send {@link EventData} to EventHub.
	 * 
	 * There are 3 ways to send to EventHubs, each exposed as a method. Use this method to Send, if:
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
	 * @throws UnresolvedAddressException if there are Client to Service network connectivity issues, if the Azure DNS resolution of the ServiceBus Namespace fails (ex: namespace deleted etc.) 
	 * @see {@link #send(EventData, String)}
	 * @see {@link PartitionSender#send(EventData)} 
	 */
	public final CompletableFuture<Void> send(EventData data) 
			throws ServiceBusException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("EventData cannot be empty.");
		}
		
		return this.sender.send(data.toAmqpMessage());
	}
	
	/**
	 * Send a batch of {@link EventData} to EventHub. 
	 * 
	 * There are 3 ways to send to EventHubs, to understand this particular type of Send refer to the overload {@link #send(EventData)}, which is used to send single {@link EventData}.
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
	 * @see {@link #send(EventData, String)}
	 * @see {@link PartitionSender#send(EventData)} 
	 */
	public final CompletableFuture<Void> send(Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
		if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0))
		{
			throw new IllegalArgumentException("Empty batch of EventData cannot be sent.");
		}
		
		return this.sender.send(EventDataUtil.toAmqpMessages(eventDatas));
	}
	
	/**
	 * Send {@link EventData} with a partitionKey to EventHub. All {@link EventData}'s with a partitionKey are guaranteed to land on the same partition.
	 * 
	 * <p>
	 * There are 3 ways to send to EventHubs, each exposed as a method. Use this method to Send, if:
	 * <pre>
	 * i.  There is a need for correlation of events based on Sender instance; The sender can generate a UniqueId and set it as partitionKey - which on the received Message can be used for correlation
	 * ii. The client wants to take control of distribution of data across partitions.
	 * </pre>
	 * 
	 * @param eventData the {@link EventData} to be sent.
	 * @param partitionKey the partitionKey will be hash'ed to determine the partitionId to send the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} exceeds 256 K.bytes
	 * @throws ServiceBusException
	 * @see {@link #send(EventData)}
	 * @see {@link PartitionSender#send(EventData)} 
	 */
	public final CompletableFuture<Void> send(EventData eventData, String partitionKey) 
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
				
		return this.sender.send(eventData.toAmqpMessage(partitionKey));
	}
	
	/**
	 * Send a batch of {@link EventData} with the same partitionKey to EventHub.
	 * 
	 * There are 3 ways to send to EventHubs, to understand this particular type of Send refer to the overload {@link #send(EventData, String)}, which is the same type of Send and is used to send single {@link EventData}.
	 * 
	 * <p> Useful in the following cases:
	 * <pre>
	 * i.	Efficient send - sending a batch of {@link EventData} maximizes the overall throughput by optimally using the number of sessions created to EventHubs service.
	 * ii.	Send multiple events in One Transaction. This is the reason why all events sent in a batch needs to have same partitionKey (so that they are sent to one partition only).
	 * </pre>
	 * 
	 * @param eventDatas the batch of events to send to EventHub
	 * @param partitionKey the partitionKey will be hash'ed to determine the partitionId to send the eventData to. On the Received message this can be accessed at {@link EventData.SystemProperties#getPartitionKey()}
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData}'s exceeds 256k bytes
	 * @throws ServiceBusException
	 * @see {@link #send(EventData)}
	 * @see {@link PartitionSender#send(EventData)} 
	 */
	public final CompletableFuture<Void> send(final Collection<EventData> eventDatas, final String partitionKey) 
		throws ServiceBusException
	{
		if (eventDatas == null || eventDatas.size() == 0)
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
		
		return this.sender.send(EventDataUtil.toAmqpMessages(eventDatas, partitionKey));
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId) 
			throws ServiceBusException
	{
		return this.createReceiver(consumerGroupName, partitionId, PartitionReceiver.START_OF_STREAM, false);
	}
	
	/**
	 * Create the EventHub receiver with given partition id and start receiving from the specified starting offset.
	 * The receiver is created on a specific consumerGroup on a specific EventHub Partition.
	 * @param consumerGroupName consumer group name
	 * @param partitionId partition Id to start receiving events from.
	 * @param startingOffset offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#START_OF_STREAM}
	 * @return
	 * @throws ServiceBusException
	 */
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset) 
			throws ServiceBusException
	{
		return this.createReceiver(consumerGroupName, partitionId, startingOffset, false);
	}
	
	/**
	 * Create the EventHub receiver with given partition id and start receiving from the specified starting offset.
	 * The receiver is created on a specific consumerGroup on a specific EventHub Partition.
	 * @param consumerGroupName
	 * @param partitionId
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
	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final long epoch) 
			throws ServiceBusException
	{
		return this.createEpochReceiver(consumerGroupName, partitionId, PartitionReceiver.START_OF_STREAM, epoch);
	}
	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, final long epoch)
			throws ServiceBusException
	{
		return this.createEpochReceiver(consumerGroupName, partitionId, startingOffset, false, epoch);
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

	public void close()
	{
		// implement Async factory close
		this.underlyingFactory.close();
	}
	
	@Override
	public CompletableFuture<Void> closeAsync() {
		// implement Async factory close
		this.underlyingFactory.close();
		return CompletableFuture.completedFuture(null);
	}
}
