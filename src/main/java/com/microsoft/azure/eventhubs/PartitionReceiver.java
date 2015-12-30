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
			final Long epoch,
			final boolean isEpochReceiver,
			final ReceiveHandler receiveHandler) 
					throws ReceiverDisconnectedException, EntityNotFoundException, ServerBusyException, AuthorizationFailedException {
		this.underlyingFactory = factory;
		this.eventHubName = eventHubName;
		this.consumerGroupName = consumerGroupName;
		this.partitionId = partitionId;
		this.startingOffset = startingOffset;
		this.offsetInclusive = offsetInclusive;
		this.epoch = epoch;
		this.isEpochReceiver = isEpochReceiver;
		this.receiveHandler = receiveHandler;
	}
	
	static CompletableFuture<PartitionReceiver> Create(MessagingFactory factory, 
			final String eventHubName, 
			final String consumerGroupName, 
			final String partitionId, 
			final String startingOffset, 
			final boolean offsetInclusive,
			final long epoch,
			final boolean isEpochReceiver,
			final ReceiveHandler receiveHandler) 
					throws ReceiverDisconnectedException, EntityNotFoundException, ServerBusyException, AuthorizationFailedException {
		final PartitionReceiver receiver = new PartitionReceiver(factory, eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive, epoch, isEpochReceiver, receiveHandler);
		return receiver.createInternalReceiver().thenApplyAsync(new Function<Void, PartitionReceiver>() {
			public PartitionReceiver apply(Void a){
				return receiver;
			}
		});
	}
	
	private CompletableFuture<Void> createInternalReceiver() throws EntityNotFoundException {
		return MessageReceiver.Create(this.underlyingFactory, UUID.randomUUID().toString(), 
				String.format("%s/ConsumerGroups/%s/Partitions/%s", this.eventHubName, this.consumerGroupName, this.partitionId), 
				this.startingOffset, this.offsetInclusive, PartitionReceiver.DefaultPrefetchCount, this.epoch, this.isEpochReceiver, this.receiveHandler)
				.thenAcceptAsync(new Consumer<MessageReceiver>() {
					public void accept(MessageReceiver r) { PartitionReceiver.this.internalReceiver = r;}
				});
	}
	
	/**
	 * @return The Cursor from which this Receiver started receiving from
	 */
	public final String getStartingOffset()
	{
		return this.startingOffset;
	}
	
	public final boolean getOffsetInclusive()
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
	
	public final long getEpoch() {
		throw new UnsupportedOperationException("TODO:");
	}
	
	public CompletableFuture<Collection<EventData>> receive() 
			throws ServerBusyException, AuthorizationFailedException
	{
		return this.receive(this.underlyingFactory.getOperationTimeout());
	}
	
	public CompletableFuture<Collection<EventData>> receive(Duration waittime)
			throws ServerBusyException, AuthorizationFailedException
	{
		if (this.receiveHandler != null) {
			throw new IllegalStateException("Receive and onReceive cannot be performed side-by-side on a single instance of Receiver.");
		}
		
		return this.internalReceiver.receive().thenApplyAsync(new Function<Collection<Message>, Collection<EventData>>() {
			@Override
			public Collection<EventData> apply(Collection<Message> amqpMessages) {
				return EventDataUtil.toEventDataCollection(amqpMessages);
			}			
		});		
	}
}