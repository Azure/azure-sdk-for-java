package com.microsoft.azure.eventhubs;

import java.io.*;
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
	public static final String DefaultConsumerGroupName = "$Default";
	
	private final MessagingFactory underlyingFactory;
	private final String eventHubName;
	
	private MessageSender sender;
	
	private EventHubClient(ConnectionStringBuilder connectionString) throws IOException, IllegalEntityException
	{
		super(UUID.randomUUID().toString());
		this.underlyingFactory = MessagingFactory.createFromConnectionString(connectionString.toString());
		this.eventHubName = connectionString.getEntityPath();
	}
	
	/**
	 * Factory method to create an instance of {@link EventHubClient} using the supplied connectionString.
	 * In a normal scenario (when re-direct is not enabled) - one EventHubClient instance maps to one Connection to the Azure ServiceBus EventHubs service.
	 * @param connectionString The connection string to be used. See {@link ConnectionStringBuilder} to construct a connectionString.
	 * @throws ServiceBusException
	 */
	public static CompletableFuture<EventHubClient> createFromConnectionString(final String connectionString,
			final boolean receiveOnly)
			throws ServiceBusException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(connectionString);
		final EventHubClient eventHubClient = new EventHubClient(connStr);
		
		if (receiveOnly)
		{
			return CompletableFuture.completedFuture(eventHubClient);
		}
		else 
		{
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
	}
	
	public static CompletableFuture<EventHubClient> createFromConnectionString(final String connectionString)
			throws ServiceBusException, IOException
	{
		return EventHubClient.createFromConnectionString(connectionString, false);
	}
	
	CompletableFuture<Void> createInternalSender() throws IllegalEntityException
	{
		return MessageSender.Create(this.underlyingFactory, UUID.randomUUID().toString(), this.eventHubName)
				.thenAcceptAsync(new Consumer<MessageSender>()
				{
					public void accept(MessageSender a) { EventHubClient.this.sender = a;}
				});
	}
	
	/**
	 * Create a sender which can publish {@link EventData} directly to an EventHub partition
	 * @param partitionId
	 * @return
	 * @throws ServiceBusException
	 */
	public final CompletableFuture<EventHubSender> createPartitionSender(final String partitionId)
		throws ServiceBusException
	{
		return EventHubSender.Create(this.underlyingFactory, this.eventHubName, partitionId);
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
	 * Send {@link EventData} to eventHub.
	 * @param data the {@link EventData} to be sent.
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} exceeds 256k bytes
	 * @throws ServiceBusException
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
	 * Send a batch of {@link EventData}
	 * @param eventDatas
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} collection exceeds 256k bytes
	 * @throws ServiceBusException
	 */
	public final CompletableFuture<Void> send(Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
		if (eventDatas == null || IteratorUtil.sizeEquals(eventDatas.iterator(), 0))
		{
			throw new IllegalArgumentException("Empty batch of EventData cannot be sent.");
		}
		
		return this.sender.send(EventDataUtil.toAmqpMessages(eventDatas), null);
	}
	
	/**
	 * Send {@link EventData} with a partitionKey to EventHub. All {@link EventData}'s with a partitionKey are guaranteed to land on the same partition.
	 * @param eventData
	 * @param partitionKey
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} exceeds 256k bytes
	 * @throws ServiceBusException
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
	 * All {@link EventData}'s with a partitionKey are guaranteed to land on the same partition.
	 * <p> Useful in the following cases:
	 * <pre>
	 * i.	Efficient send - sending a batch of {@link EventData} maximizes the overall throughput by optimally using the number of sessions created to EventHubs service.
	 * ii.	Send multiple events in a Transaction. This is the reason why all events sent in a batch needs to have same partitionKey (so that they are sent to one partition only).
	 * </pre>
	 * @param eventDatas
	 * @param partitionKey
	 * @return
	 * @throws PayloadSizeExceededException if the total size of the {@link EventData} collection exceeds 256k bytes
	 * @throws ServiceBusException
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
		
		if (partitionKey.length() > ClientConstants.MaxPartitionKeyLength)
		{
			throw new IllegalArgumentException(
					String.format(Locale.US, "PartitionKey exceeds the maximum allowed length of partitionKey: {0}", ClientConstants.MaxPartitionKeyLength));
		}
		
		return this.sender.send(EventDataUtil.toAmqpMessages(eventDatas, partitionKey), partitionKey);
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId) 
			throws ServiceBusException
	{
		return this.createReceiver(consumerGroupName, partitionId, PartitionReceiver.StartOfStream, false);
	}
	
	/**
	 * Create the EventHub receiver with given partition id and start receiving from the specified starting offset.
	 * The receiver is created on a specific consumerGroup on a specific EventHub Partition.
	 * @param consumerGroupName consumer group name
	 * @param partitionId partition Id to start receiving events from.
	 * @param startingOffset offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#StartOfStream}
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
	 * @param startingOffset offset to start receiving the events from. To receive from start of the stream use: {@link PartitionReceiver#StartOfStream}
	 * @param offsetInclusive if set to true, the startingOffset is treated as an inclusive offset - meaning the first event returned is the one that has the starting offset. Normally first event returned is the event after the starting offset.
	 * @return
	 * @throws ServiceBusException
	 */
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive) 
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, null, PartitionReceiver.NullEpoch, false);
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final Instant dateTime)
			throws ServiceBusException
	{
		return PartitionReceiver.create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, null, false, dateTime, PartitionReceiver.NullEpoch, false);
	}
	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final long epoch) 
			throws ServiceBusException
	{
		return this.createEpochReceiver(consumerGroupName, partitionId, PartitionReceiver.StartOfStream, epoch);
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
		return null;
	}
}
