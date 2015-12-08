package com.microsoft.azure.eventhubs;

import java.util.Date;

import org.apache.qpid.proton.engine.*;

import com.microsoft.azure.eventhubs.Exceptions.*;
import com.microsoft.azure.eventhubs.common.*;

/**
 * Anchor class - All EventHub client operations STARTS here.
 */
public class EventHubClient 
{
	private final MessagingFactory underlyingFactory;
	
	private EventHubClient(ConnectionStringBuilder connectionString) 
	{
		this.underlyingFactory = MessagingFactory.createFromConnectionString(connectionString.toString());
	}
	
	public static EventHubClient createFromConnectionString(final String connectionString)
			throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException
	{
		ConnectionStringBuilder connStr = new ConnectionStringBuilder(connectionString);
		connStr.Validate();
		return new EventHubClient(connStr);
	}
	
	public final PartitionSender createPartitionSender(final String partitionId)
		throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException
	{
		return new PartitionSender(partitionId);
	}
	
	// TODO: return partitionInfo
	public final String getPartitionInfo()
			throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException
	{
		return "0";
	}
	
	public final void send(EventData data) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		
	}
	
	public final void send(Iterable<EventData> data) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		
	}
	
	public final void send(EventData data, String partitionKey) 
			throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		
	}
	
	public final void send(Iterable<EventData> data, String partitionKey) 
		throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		
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
