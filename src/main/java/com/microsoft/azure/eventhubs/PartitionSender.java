package com.microsoft.azure.eventhubs;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.*;

import com.microsoft.azure.servicebus.*;

public final class PartitionSender
{
	private final String partitionId;
	private final String eventHubName;
	private final MessagingFactory factory;
	
	private MessageSender internalSender;
		
	private PartitionSender(MessagingFactory factory, String eventHubName, String partitionId) {
		this.partitionId = partitionId;
		this.eventHubName = eventHubName;
		this.factory = factory;
	}
	
	/**
	 * Internal-Only: factory pattern to Create EventHubSender
	 */
	static CompletableFuture<PartitionSender> Create(MessagingFactory factory, String eventHubName, String partitionId) throws EntityNotFoundException {
		final PartitionSender sender = new PartitionSender(factory, eventHubName, partitionId);
		return sender.createInternalSender()
				.thenApplyAsync(new Function<Void, PartitionSender>() {
					public PartitionSender apply(Void a) {
						return sender;
					}
				});
	}
	
	private CompletableFuture<Void> createInternalSender() throws EntityNotFoundException {
		return MessageSender.Create(this.factory, UUID.randomUUID().toString(), this.eventHubName)
				.thenAcceptAsync(new Consumer<MessageSender>() {
					public void accept(MessageSender a) { PartitionSender.this.internalSender = a;}
				});
	}

	public final CompletableFuture<Void> send(EventData data) 
			throws MessagingCommunicationException, ServerBusyException, AuthorizationFailedException, PayloadExceededException, EntityNotFoundException
	{
		return this.internalSender.send(data.toAmqpMessage());
	}
	
	public final void send(Iterable<EventData> eventDatas) 
			throws MessagingCommunicationException, ServerBusyException, AuthorizationFailedException, PayloadExceededException, EntityNotFoundException
	{
		throw new UnsupportedOperationException("TODO: Implement Send Batch");
	}
}
