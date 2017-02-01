package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessageSender;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;

final class BrokeredMessageSender extends InitializableEntity implements IMessageSender
{
	private boolean ownsMessagingFactory;
	private ConnectionStringBuilder amqpConnectionStringBuilder = null;
	private String entityPath = null;
	private MessagingFactory messagingFactory = null;
	private MessageSender internalSender = null;
	private boolean isInitialized = false;
	
	private BrokeredMessageSender()
	{
		super(StringUtil.getRandomString(), null);
	}
	
	BrokeredMessageSender(ConnectionStringBuilder amqpConnectionStringBuilder)
	{
		this();
		
		this.amqpConnectionStringBuilder = amqpConnectionStringBuilder;
		this.entityPath = this.amqpConnectionStringBuilder.getEntityPath();
		this.ownsMessagingFactory = true;
	}
	
	BrokeredMessageSender(MessagingFactory messagingFactory, String entityPath)
	{		
		this(messagingFactory, entityPath, false);
	}
			
	private BrokeredMessageSender(MessagingFactory messagingFactory, String entityPath, boolean ownsMessagingFactory)
	{		
		this();
		
		this.messagingFactory = messagingFactory;
		this.entityPath = entityPath;
		this.ownsMessagingFactory = ownsMessagingFactory;
	}
	
	@Override
	synchronized CompletableFuture<Void> initializeAsync() throws IOException
	{
		if(this.isInitialized)
		{
			return CompletableFuture.completedFuture(null);
		}
		else
		{
			CompletableFuture<Void> factoryFuture;
			if(this.messagingFactory == null)
			{
				factoryFuture = MessagingFactory.createFromConnectionStringBuilderAsync(amqpConnectionStringBuilder).thenAccept((f) -> {BrokeredMessageSender.this.messagingFactory = f;});
			}
			else
			{
				factoryFuture = CompletableFuture.completedFuture(null);
			}
			
			return factoryFuture.thenCompose((v) ->
			{
				CompletableFuture<MessageSender> senderFuture = MessageSender.create(BrokeredMessageSender.this.messagingFactory, StringUtil.getRandomString(), BrokeredMessageSender.this.entityPath);
				return senderFuture.thenAccept((s) -> 
				{
					BrokeredMessageSender.this.internalSender = s;
					BrokeredMessageSender.this.isInitialized = true;
				});
			});
		}
	}		
	
	@Override
	public void send(IBrokeredMessage message) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.sendAsync(message));		
	}

	@Override
	public void sendBatch(Collection<? extends IBrokeredMessage> message) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.sendBatchAsync(message));
	}

	@Override
	public CompletableFuture<Void> sendAsync(IBrokeredMessage message) {		
		Message amqpMessage = MessageConverter.convertBrokeredMessageToAmqpMessage((BrokeredMessage)message);
		return this.internalSender.sendAsync(amqpMessage);
	}

	@Override
	public CompletableFuture<Void> sendBatchAsync(Collection<? extends IBrokeredMessage> messages) {
		ArrayList<Message> convertedMessages = new ArrayList<Message>();
		for(IBrokeredMessage message : messages)
		{
			convertedMessages.add(MessageConverter.convertBrokeredMessageToAmqpMessage((BrokeredMessage)message));
		}
		
		return this.internalSender.sendAsync(convertedMessages);
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		if(this.isInitialized)
		{
			return this.internalSender.closeAsync().thenComposeAsync((v) -> 
			{
				if(BrokeredMessageSender.this.ownsMessagingFactory)
				{
					return BrokeredMessageSender.this.messagingFactory.closeAsync();
				}
				else
				{
					return CompletableFuture.completedFuture(null);
				}				
			});
		}
		else
		{
			return CompletableFuture.completedFuture(null);			
		}
	}

	@Override
	public String getEntityPath() {
		return this.entityPath;
	}

	@Override
	public CompletableFuture<Long> scheduleMessageAsync(IBrokeredMessage message, Instant scheduledEnqueueTimeUtc) {
		message.setScheduledEnqueuedTimeUtc(scheduledEnqueueTimeUtc);
		Message amqpMessage = MessageConverter.convertBrokeredMessageToAmqpMessage((BrokeredMessage)message);
		return this.internalSender.scheduleMessageAsync(new Message[] {amqpMessage}, this.messagingFactory.getOperationTimeout()).thenApply(sequenceNumbers -> sequenceNumbers[0]);
	}

	@Override
	public CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber) {
		return this.internalSender.cancelScheduledMessageAsync(new Long[]{sequenceNumber}, this.messagingFactory.getOperationTimeout());
	}

	@Override
	public long scheduleMessage(IBrokeredMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(this.scheduleMessageAsync(message, scheduledEnqueueTimeUtc));
	}

	@Override
	public void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException
	{
		Utils.completeFuture(this.cancelScheduledMessageAsync(sequenceNumber));
	}
}
