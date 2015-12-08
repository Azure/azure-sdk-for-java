package com.microsoft.azure.eventhubs;

import com.microsoft.azure.servicebus.*;

public final class PartitionSender
{
	private final String partitionId;
	private final String path;
	
	PartitionSender(String path, String partitionId){
		this.partitionId = partitionId;
		this.path = path;
	}
	
	PartitionSender(String path) {
		this(path, null);
	}
	
	public final void Send(EventData data) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException, EntityNotFoundException
	{
		
	}
	
	public final void Send(Iterable<EventData> eventDatas) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException, EntityNotFoundException
	{
		
	}
}
