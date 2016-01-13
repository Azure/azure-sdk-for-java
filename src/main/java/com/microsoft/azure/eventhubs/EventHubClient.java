package com.microsoft.azure.eventhubs;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.message.*;
import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.amqp.*;

/**
 * Anchor class - all EventHub client operations STARTS here.
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
	
	public static CompletableFuture<EventHubClient> createFromConnectionString(final String connectionString)
			throws ServiceBusException, IOException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(connectionString);
		final EventHubClient eventHubClient = new EventHubClient(connStr);
		
		return eventHubClient.createInternalSender()
				.thenApplyAsync(new Function<Void, EventHubClient>()
				{
					public EventHubClient apply(Void a)
					{
						return eventHubClient;
					}
				});
	}
	
	CompletableFuture<Void> createInternalSender() throws IllegalEntityException
	{
		return MessageSender.Create(this.underlyingFactory, UUID.randomUUID().toString(), this.eventHubName)
				.thenAcceptAsync(new Consumer<MessageSender>()
				{
					public void accept(MessageSender a) { EventHubClient.this.sender = a;}
				});
	}
	
	public final CompletableFuture<PartitionSender> createPartitionSender(final String partitionId)
		throws ServiceBusException
	{
		return PartitionSender.Create(this.underlyingFactory, this.eventHubName, partitionId);
	}
	
	// TODO: return partitionInfo
	public final String getPartitionInfo()
			throws ServiceBusException
	{
		throw new UnsupportedOperationException("TODO: Implement over http");
	}
	
	public final CompletableFuture<Void> send(EventData data) 
			throws ServiceBusException
	{
		if (data == null)
		{
			throw new IllegalArgumentException("EventData cannot be empty.");
		}
		
		return this.sender.send(data.toAmqpMessage());
	}
	
	public final CompletableFuture<Void> send(Iterable<EventData> eventDatas) 
			throws ServiceBusException
	{
		if (eventDatas == null)
		{
			throw new IllegalArgumentException("EventData cannot be null.");
		}
		
		return this.sender.send(EventDataUtil.toAmqpMessage(eventDatas), AmqpConstants.AmqpBatchMessageFormat);
	}
	
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
	
	public final CompletableFuture<Void> send(final Iterable<EventData> eventDatas, final String partitionKey) 
		throws ServiceBusException
	{
		if (eventDatas == null)
		{
			throw new IllegalArgumentException("EventData batch cannot be empty.");
		}
		
		// TODO: PartitionKey SizeLimit - boundaries check
		if (partitionKey == null)
		{
			throw new IllegalArgumentException("partitionKey cannot be null");
		}
		
		return this.sender.send(EventDataUtil.toAmqpMessage(eventDatas, partitionKey), AmqpConstants.AmqpBatchMessageFormat);
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId) 
			throws ServiceBusException
	{
		return this.createReceiver(consumerGroupName, partitionId, PartitionReceiver.StartOfStream, false);
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset) 
			throws ServiceBusException
	{
		return this.createReceiver(consumerGroupName, partitionId, startingOffset, false);
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive) 
			throws ServiceBusException
	{
		return PartitionReceiver.Create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, PartitionReceiver.NullEpoch, false, null);
	}
	
	public final CompletableFuture<PartitionReceiver> createReceiver(final String consumerGroupName, final String partitionId, final Date dateTimeUtc)
	{
		throw new UnsupportedOperationException("TODO: Implement datetime receiver");
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
		return PartitionReceiver.Create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, epoch, true, null);
	}
	
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final Date dateTimeUtc, final long epoch)
		throws ServiceBusException
	{
		// return PartitionReceiver.Create(this.underlyingFactory,  this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, epoch, isEpochReceiver, receiveHandler)
		throw new UnsupportedOperationException("TODO: Implement datetime receiver");
	}
	
	/**
	 * Use Epoch receiver to ensure that there is only *one* Receiver active per an EventHub Partition per consumer group.
	 * EventHubs Service will ensure that the Receiver with highest epoch Value owns the Partition. 
	 * This overload of CreateEpochReceiver is built to support EventProcessorHost pattern. Implement ReceiveHandler to process events.
	 * @param consumerGroupName consumer group name
	 * @param partitionId partition id of the EventHub
	 * @param startingOffset starting offset to read the Stream from. By default Start of EventHub Stream is {@link PartitionReceiver#StartOfStream}. 
	 * @param offsetInclusive should the first event to be read include the offset ?
	 * @param epoch to make sure that there is only one Receiver
	 * @param receiveHandler the implementation of {@link ReceiveHandler} which can process the received events 
	 * @return
	 * @throws IllegalEntityException
	 * @throws ServerBusyException
	 * @throws AuthorizationFailedException
	 * @throws ReceiverDisconnectedException
	 */
	public final CompletableFuture<PartitionReceiver> createEpochReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive, final long epoch, ReceiveHandler receiveHandler) 
			throws ServiceBusException
	{
		return PartitionReceiver.Create(this.underlyingFactory, this.eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, epoch, true, receiveHandler);
	}
	
	public final void close()
	{
	}
}
