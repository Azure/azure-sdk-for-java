package com.microsoft.azure.eventhubs;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.*;

/**
 * Anchor class - all EventHub client operations STARTS here.
 */
public class EventHubClient 
{
	private final MessagingFactory underlyingFactory;
	private final MessageSender sender;
	
	private EventHubClient(ConnectionStringBuilder connectionString) throws IOException, EntityNotFoundException, InterruptedException, ExecutionException
	{
		this.underlyingFactory = MessagingFactory.createFromConnectionString(connectionString.toString());
		this.sender = MessageSender.Create(this.underlyingFactory, "sender0", connectionString.getEntityPath()).get();
	}
	
	public static EventHubClient createFromConnectionString(final String connectionString)
			throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, IOException, InterruptedException, ExecutionException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(connectionString);
		return new EventHubClient(connStr);
	}
	
	public final PartitionSender createPartitionSender(final String partitionId)
		throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, InterruptedException, ExecutionException
	{
		return new PartitionSender(this.underlyingFactory, this.sender.getSendPath(), partitionId);
	}
	
	// TODO: return partitionInfo
	public final String getPartitionInfo()
			throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException
	{
		throw new UnsupportedOperationException("TODO: Implement over http");
	}
	
	public final void send(EventData data) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		if (data == null) {
			// TODO: TRACE
			throw new IllegalArgumentException("data");
		}
		
		this.sender.Send(data.toAmqpMessage());
	}
	
	public final void send(Iterable<EventData> data) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		throw new UnsupportedOperationException("TODO Implement Send Batch");
	}
	
	public final void send(EventData data, String partitionKey) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException, InterruptedException, ExecutionException
	{
		// TODO: Arguments
		data.setPartitionKey(partitionKey);
		this.sender.Send(data.toAmqpMessage()).get();
	}
	
	public final void send(Iterable<EventData> data, String partitionKey) 
		throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		throw new UnsupportedOperationException("TODO Implement Send Batch");
	}
	
	public final PartitionReceiver createReceiver(final String partitionId) {
		return new PartitionReceiver(partitionId);
	}
	
	public final PartitionReceiver createReceiver(final String partitionId, final String startingOffset) {
		return new PartitionReceiver(partitionId);
	}
	
	public final PartitionReceiver createReceiver(final String partitionId, final String startingOffset, boolean offsetInclusive) {
		return new PartitionReceiver(partitionId);
	}
	
	public final PartitionReceiver createReceiver(final String partitionId, final Date dateTimeUtc) {
		return new PartitionReceiver(partitionId);
	}
	
	public final PartitionReceiver createEpochReceiver(final String partitionId, final long epoch) {
		return new PartitionReceiver(partitionId);
	}
	
	public final PartitionReceiver createEpochReceiver(final String partitionId, final String startingOffset, final long epoch) {
		return new PartitionReceiver(partitionId);
	}
	
	public final PartitionReceiver createEpochReceiver(final String partitionId, final String startingOffset, boolean offsetInclusive, final long epoch) {
		return new PartitionReceiver(partitionId);
	}
	
	public final PartitionReceiver createEpochReceiver(final String partitionId, final Date dateTimeUtc, final long epoch) {
		return new PartitionReceiver(partitionId);
	}
	
	// TODO: is IDisposable present in java? we don't have connFactory 
	// - so is this the time to dispose the native resources ? 
	public final void close()
	{
		
	}
}
