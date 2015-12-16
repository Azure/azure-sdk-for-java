package com.microsoft.azure.eventhubs;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.message.*;
import com.microsoft.azure.servicebus.*;

/**
 * Anchor class - all EventHub client operations STARTS here.
 */
public class EventHubClient 
{
	public static final String DefaultConsumerGroupName = "$Default";
	
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
		if (data == null) {
			throw new IllegalArgumentException("EventData cannot be null.");
		}
		
		if (partitionKey == null) {
			throw new IllegalArgumentException("partitionKey cannot be null");
		}
		
		Message amqpMessage = data.toAmqpMessage();
		MessageAnnotations messageAnnotations = (amqpMessage.getMessageAnnotations() == null) 
						? new MessageAnnotations(new HashMap<Symbol, Object>()) 
						: amqpMessage.getMessageAnnotations();
		messageAnnotations.getValue().put(AmqpConstants.PartitionKey, partitionKey);
		this.sender.Send(data.toAmqpMessage()).get();
	}
	
	public final void send(Iterable<EventData> data, String partitionKey) 
		throws MessagingCommunicationException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, PayloadExceededException
	{
		throw new UnsupportedOperationException("TODO Implement Send Batch");
	}
	
	public final PartitionReceiver createReceiver(final String consumerGroupName, final String partitionId) 
			throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, InterruptedException, ExecutionException {
		return this.createReceiver(consumerGroupName, partitionId, PartitionReceiver.StartOfStream, false);
	}
	
	public final PartitionReceiver createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset) 
			throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, InterruptedException, ExecutionException {
		return this.createReceiver(consumerGroupName, partitionId, startingOffset, false);
	}
	
	public final PartitionReceiver createReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive) 
			throws EntityNotFoundException, ServerBusyException, InternalServerErrorException, AuthorizationFailedException, InterruptedException, ExecutionException {
		return new PartitionReceiver(this.underlyingFactory, this.sender.getSendPath(), consumerGroupName, partitionId, startingOffset, offsetInclusive);
	}
	
	public final PartitionReceiver createReceiver(final String consumerGroupName, final String partitionId, final Date dateTimeUtc) {
		throw new UnsupportedOperationException("TODO: Implement datetime receiver");
	}
	
	public final PartitionReceiver createEpochReceiver(final String consumerGroupName, final String partitionId, final long epoch) {
		return this.createEpochReceiver(consumerGroupName, partitionId, PartitionReceiver.StartOfStream, epoch);
	}
	
	public final PartitionReceiver createEpochReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, final long epoch) {
		return this.createEpochReceiver(consumerGroupName, partitionId, startingOffset, false, epoch);
	}
	
	public final PartitionReceiver createEpochReceiver(final String consumerGroupName, final String partitionId, final String startingOffset, boolean offsetInclusive, final long epoch) {
		throw new UnsupportedOperationException("TODO: submit change to protonj");
	}
	
	public final PartitionReceiver createEpochReceiver(final String consumerGroupName, final String partitionId, final Date dateTimeUtc, final long epoch) {
		throw new UnsupportedOperationException("TODO: submit change to protonj");
	}
	
	public final void close()
	{
		
	}
}
