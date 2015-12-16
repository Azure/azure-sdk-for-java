package com.microsoft.azure.eventhubs;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.*;

public final class PartitionReceiver 
{
	private final String partitionId;
	
	private String startingOffset;
	private boolean offsetInclusive;
	private MessageReceiver internalReceiver; 
	private ReceiveHandler receiveHandler;
	
	static final int DefaultPrefetchCount = 300;
	
	public static final String StartOfStream = "-1";
	
	private PartitionReceiver(MessagingFactory factory, final String eventHubName, final String consumerGroupName, final String partitionId)
	{
		this.partitionId = partitionId;
	}
	
	PartitionReceiver(MessagingFactory factory, 
			final String eventHubName, 
			final String consumerGroupName, 
			final String partitionId, 
			final String startingOffset, 
			final boolean offsetInclusive) 
					throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, InterruptedException, ExecutionException {
		this(factory, eventHubName, consumerGroupName, partitionId);
		this.startingOffset = startingOffset;
		this.offsetInclusive = offsetInclusive;
		this.internalReceiver = MessageReceiver.Create(factory, "receiver0", 
				String.format("%s/ConsumerGroups/%s/Partitions/%s", eventHubName, consumerGroupName, partitionId), startingOffset, offsetInclusive, this.DefaultPrefetchCount).get();
	}
	
	PartitionReceiver(MessagingFactory factory, 
			final String eventHubName, 
			final String consumerGroupName, 
			final String partitionId, 
			final String startingOffset, 
			final boolean offsetInclusive,
			final ReceiveHandler receiveHandler) 
					throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, InterruptedException, ExecutionException {
		this(factory, eventHubName, consumerGroupName, partitionId, startingOffset, offsetInclusive);
		this.receiveHandler = receiveHandler;
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
	
	public Collection<EventData> Receive() 
			throws ServerBusyException, AuthorizationFailedException, InternalServerErrorException, InterruptedException, ExecutionException
	{
		return this.Receive(Duration.ofSeconds(60));
	}
	
	public Collection<EventData> Receive(Duration waittime)
			throws ServerBusyException, AuthorizationFailedException, InternalServerErrorException, InterruptedException, ExecutionException
	{
		if (this.receiveHandler != null) {
			throw new IllegalStateException("Receive and onReceive cannot be performed on Single instance of Receiver.");
		}
		
		Collection<Message> amqpMessages = this.internalReceiver.receive().get();
		LinkedList<EventData> events = new LinkedList<EventData>();
		for(Message message : amqpMessages) {
			events.add(new EventData(message));
		}
		
		return events;
	}
}