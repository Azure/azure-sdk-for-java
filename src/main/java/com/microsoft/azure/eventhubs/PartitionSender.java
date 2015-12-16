package com.microsoft.azure.eventhubs;

import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.*;

public final class PartitionSender
{
	private final String partitionId;
	private final String eventHubName;
	private final MessageSender internalSender;
	
	PartitionSender(MessagingFactory factory, String eventHubName, String partitionId) throws EntityNotFoundException, InterruptedException, ExecutionException {
		this.partitionId = partitionId;
		this.eventHubName = eventHubName;
		this.internalSender = MessageSender.Create(factory, "partitionSender0", 
								String.format("%s/Partitions/%s", eventHubName, partitionId)).get();
	}

	public final void send(EventData data) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException, EntityNotFoundException, InterruptedException, ExecutionException
	{
		this.internalSender.send(data.toAmqpMessage()).get();
	}
	
	public final void send(Iterable<EventData> eventDatas) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException, EntityNotFoundException
	{
		throw new UnsupportedOperationException("TODO: Implement Send Batch");
	}
}
