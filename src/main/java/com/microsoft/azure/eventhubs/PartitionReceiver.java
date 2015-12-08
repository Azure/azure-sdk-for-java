package com.microsoft.azure.eventhubs;

import java.time.*;
import java.util.LinkedList;

import com.microsoft.azure.servicebus.*;

public final class PartitionReceiver 
{
	private final String partitionId;
	static final int DefaultPrefetchCount = 300;
	
	PartitionReceiver(String partitionId)
	{
		this.partitionId = partitionId;
	}
	
	/**
	 * @return The Cursor from which this Receiver started receiving from
	 */
	public final String getStartingCursor()
	{
		// TODO: change to actually initialized start of Cursor
		return "-1";
	}
	
	/**
	 * @return The Partition from which this Receiver is fetching data
	 */
	public final String getPartitionId()
	{
		return this.partitionId;
	}
	
	public final String getPrefetchCount()
	{
		return "300";
	}
	
	public final long getEpoch() {
		return 12444;
	}
	
	public EventData Receive() 
			throws ServerBusyException, AuthorizationFailedException, InternalServerErrorException
	{
		return new EventData();
	}
	
	public Iterable<ReceivedEventData> ReceiveBatch() 
			throws ServerBusyException, AuthorizationFailedException, InternalServerErrorException
	{
		return new LinkedList<ReceivedEventData>();
	}
	
	public ReceivedEventData Receive(Duration waittime)
			throws ServerBusyException, AuthorizationFailedException, InternalServerErrorException
	{
		return new ReceivedEventData(new EventData());
	}
	
	public Iterable<ReceivedEventData> ReceiveBatch(Duration waittime)
			throws ServerBusyException, AuthorizationFailedException, InternalServerErrorException
	{
		return new LinkedList<ReceivedEventData>();
	}
}