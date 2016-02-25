/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs;

import java.util.concurrent.*;
import java.util.function.*;

import com.microsoft.azure.servicebus.*;

public final class PartitionSender extends ClientEntity
{
	private final String partitionId;
	private final String eventHubName;
	private final MessagingFactory factory;
	
	private MessageSender internalSender;
		
	private PartitionSender(MessagingFactory factory, String eventHubName, String partitionId)
	{
		super(null);
		this.partitionId = partitionId;
		this.eventHubName = eventHubName;
		this.factory = factory;
	}
	
	/**
	 * Internal-Only: factory pattern to Create EventHubSender
	 */
	static CompletableFuture<PartitionSender> Create(MessagingFactory factory, String eventHubName, String partitionId) throws ServiceBusException
	{
		final PartitionSender sender = new PartitionSender(factory, eventHubName, partitionId);
		return sender.createInternalSender()
				.thenApplyAsync(new Function<Void, PartitionSender>()
				{
					public PartitionSender apply(Void a)
					{
						return sender;
					}
				});
	}
	
	private CompletableFuture<Void> createInternalSender() throws ServiceBusException
	{
		return MessageSender.create(this.factory, StringUtil.getRandomString(), 
				String.format("%s/Partitions/%s", this.eventHubName, this.partitionId))
				.thenAcceptAsync(new Consumer<MessageSender>()
				{
					public void accept(MessageSender a) { PartitionSender.this.internalSender = a;}
				});
	}

	/**
	 * Send {@link EventData} to EventHub. The sent {@link EventData} will land on the Specific partition which this {@link PartitionSender} is created to. 
	 * 
	 * <p>There are 3 ways to send to EventHubs, each exposed as a method (along with its sendBatch overload):
	 * <pre>
	 * i.   {@link #send(EventData)} or {@link #send(Iterable)}
	 * ii.  {@link #send(EventData, String)} or {@link #send(Iterable, String)}
	 * iii. {@link PartitionSender#send(EventData)} or {@link PartitionSender#send(Iterable)}
	 * </pre>
	 * <p>Use this method to Send, if:
	 * <pre>
	 * a)  There is a need to correlate the events on the receiver-end at one Partition or 
	 * b)  The client wants to take control of distribution of data across partitions.
	 * </pre>
	 * 
	 * @param data the {@link EventData} to be sent.
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} exceeds 256k bytes
	 * @throws ServiceBusException
	 * @see {@link #close()}
	 * @see {@link EventHubClient#send(EventData, String)}
	 * @see {@link EventHubClient#send(EventData)} 
	 */
	public final CompletableFuture<Void> send(EventData data) 
			throws ServiceBusException
	{
		return this.internalSender.send(data.toAmqpMessage());
	}
	
	/**
	 * Send a batch of {@link EventData} to EventHub. The sent {@link EventData} will land on the Specific partition which this {@link PartitionSender} is created to.
	 * 
	 * <p>There are 3 ways to send to EventHubs, to understand this particular type of Send refer to the overload {@link PartitionSender#send(EventData)}, which is used to send single {@link EventData}.
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
	 * @see {@link EventHubClient#send(EventData, String)}
	 * @see {@link PartitionSender#send(EventData)} 
	 */
	public final CompletableFuture<Void> send(Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
		if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas, 0))
		{
			throw new IllegalArgumentException("EventData batch cannot be empty.");
		}
		
		return this.internalSender.send(EventDataUtil.toAmqpMessages(eventDatas));
	}

	@Override
	public CompletableFuture<Void> close()
	{
		if (this.internalSender == null)
		{
			return CompletableFuture.completedFuture(null);
		}
		else
		{
			return this.internalSender.close();
		}
	}
}
