package com.microsoft.azure.eventhubs;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.*;

public final class PartitionReceiver
{
	private final String partitionId;
	
	private String startingOffset;
	private boolean offsetInclusive;
	private Instant startingDateTime;
	private MessageReceiver internalReceiver; 
	private ReceiveHandler receiveHandler;
	private final MessagingFactory underlyingFactory;
	private final String eventHubName;
	private final String consumerGroupName;
	private Long epoch;
	private boolean isEpochReceiver;
	
	static final int DefaultPrefetchCount = 300;
	static final long NullEpoch = 0;
	
	public static final String StartOfStream = "-1";
		
	private PartitionReceiver(MessagingFactory factory, 
			final String eventHubName, 
			final String consumerGroupName, 
			final String partitionId, 
			final String startingOffset, 
			final boolean offsetInclusive,
			final Instant dateTime,
			final Long epoch,
			final boolean isEpochReceiver,
			final ReceiveHandler receiveHandler) 
					throws ServiceBusException
	{
		this.underlyingFactory = factory;
		this.eventHubName = eventHubName;
		this.consumerGroupName = consumerGroupName;
		this.partitionId = partitionId;
		this.startingOffset = startingOffset;
		this.offsetInclusive = offsetInclusive;
		this.startingDateTime = dateTime;
		this.epoch = epoch;
		this.isEpochReceiver = isEpochReceiver;
		this.receiveHandler = receiveHandler;
	}
	
	static CompletableFuture<PartitionReceiver> create(MessagingFactory factory, 
			final String eventHubName, 
			final String consumerGroupName, 
			final String partitionId, 
			final String startingOffset, 
			final boolean offsetInclusive,
			final Instant dateTime,
			final long epoch,
			final boolean isEpochReceiver,
			final ReceiveHandler receiveHandler) 
					throws ServiceBusException
	{
		final PartitionReceiver receiver = new PartitionReceiver(factory, eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, dateTime, epoch, isEpochReceiver, receiveHandler);
		return receiver.createInternalReceiver().thenApplyAsync(new Function<Void, PartitionReceiver>()
		{
			public PartitionReceiver apply(Void a)
			{
				return receiver;
			}
		});
	}
	
	private CompletableFuture<Void> createInternalReceiver() throws ServiceBusException
	{
		return MessageReceiver.create(this.underlyingFactory, UUID.randomUUID().toString(), 
				String.format("%s/ConsumerGroups/%s/Partitions/%s", this.eventHubName, this.consumerGroupName, this.partitionId), 
				this.startingOffset, this.offsetInclusive, this.startingDateTime, PartitionReceiver.DefaultPrefetchCount, this.epoch, this.isEpochReceiver, this.receiveHandler)
				.thenAcceptAsync(new Consumer<MessageReceiver>()
				{
					public void accept(MessageReceiver r) { PartitionReceiver.this.internalReceiver = r;}
				});
	}
	
	/**
	 * @return The Cursor from which this Receiver started receiving from
	 */
	final String getStartingOffset()
	{
		return this.startingOffset;
	}
	
	final boolean getOffsetInclusive()
	{
		return this.offsetInclusive;
	}
	
	/**
	 * @return The Partition from which this Receiver is fetching data
	 */
	public final String getPartitionId()
	{
		return this.partitionId;
	}
	
	public final int getPrefetchCount()
	{
		return this.internalReceiver.getPrefetchCount();
	}
	
	public final long getEpoch()
	{
		return this.epoch;
	}
	
	/** 
	 * Receive a batch of {@link EventData}'s from an EventHub partition
	 * @return Batch of {@link EventData}'s from the partition on which this receiver is created. returns 'null' if no {@link EventData} is present.
	 * @throws ServerBusyException
	 * @throws AuthorizationFailedException
	 * @throws InternalServerException
	 */
	public CompletableFuture<Iterable<EventData>> receive() 
			throws ServiceBusException
	{
		if (this.receiveHandler != null)
		{
			throw new IllegalStateException("Receive and onReceive cannot be performed side-by-side on a single instance of Receiver.");
		}
		
		return this.internalReceiver.receive().thenApplyAsync(new Function<Collection<Message>, Iterable<EventData>>()
		{
			@Override
			public Iterable<EventData> apply(Collection<Message> amqpMessages)
			{
				LinkedList<EventData> events = EventDataUtil.toEventDataCollection(amqpMessages);
				if (events != null)
				{
					EventData lastEvent = events.getLast();
					if (lastEvent != null)
					{
						PartitionReceiver.this.internalReceiver.setLastReceivedOffset(lastEvent.getSystemProperties().getOffset());
					}
				}
				
				return events;
			}			
		});		
	}

	public void close()
	{
		if (this.internalReceiver != null)
			this.internalReceiver.close();		
	}
}