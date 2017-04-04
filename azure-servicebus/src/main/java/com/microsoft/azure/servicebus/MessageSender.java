package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.CoreMessageSender;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;

final class MessageSender extends InitializableEntity implements IMessageSender
{
	private boolean ownsMessagingFactory;
	private ConnectionStringBuilder amqpConnectionStringBuilder = null;
	private String entityPath = null;
	private MessagingFactory messagingFactory = null;
	private CoreMessageSender internalSender = null;
	private boolean isInitialized = false;
	
	private MessageSender()
	{
		super(StringUtil.getShortRandomString(), null);
	}
	
	MessageSender(ConnectionStringBuilder amqpConnectionStringBuilder)
	{
		this();
		
		this.amqpConnectionStringBuilder = amqpConnectionStringBuilder;
		this.entityPath = this.amqpConnectionStringBuilder.getEntityPath();
		this.ownsMessagingFactory = true;
	}
	
	MessageSender(MessagingFactory messagingFactory, String entityPath)
	{		
		this(messagingFactory, entityPath, false);
	}
			
	private MessageSender(MessagingFactory messagingFactory, String entityPath, boolean ownsMessagingFactory)
	{		
		this();
		
		this.messagingFactory = messagingFactory;
		this.entityPath = entityPath;
		this.ownsMessagingFactory = ownsMessagingFactory;
	}
	
	@Override
	synchronized CompletableFuture<Void> initializeAsync()
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
				factoryFuture = MessagingFactory.createFromConnectionStringBuilderAsync(amqpConnectionStringBuilder).thenAcceptAsync((f) -> {this.messagingFactory = f;});
			}
			else
			{
				factoryFuture = CompletableFuture.completedFuture(null);
			}
			
			return factoryFuture.thenComposeAsync((v) ->
			{
				CompletableFuture<CoreMessageSender> senderFuture = CoreMessageSender.create(this.messagingFactory, StringUtil.getShortRandomString(), this.entityPath);
				return senderFuture.thenAcceptAsync((s) -> 
				{
					this.internalSender = s;
					this.isInitialized = true;
				});
			});
		}
	}
	
	final CoreMessageSender getInternalSender()
	{
		return this.internalSender;
	}
	
	@Override
	public void send(IMessage message) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.sendAsync(message));		
	}

	@Override
	public void sendBatch(Collection<? extends IMessage> message) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.sendBatchAsync(message));
	}

	@Override
	public CompletableFuture<Void> sendAsync(IMessage message) {		
		org.apache.qpid.proton.message.Message amqpMessage = MessageConverter.convertBrokeredMessageToAmqpMessage((Message)message);
		return this.internalSender.sendAsync(amqpMessage);
	}

	@Override
	public CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> messages) {
		ArrayList<org.apache.qpid.proton.message.Message> convertedMessages = new ArrayList<org.apache.qpid.proton.message.Message>();
		for(IMessage message : messages)
		{
			convertedMessages.add(MessageConverter.convertBrokeredMessageToAmqpMessage((Message)message));
		}
		
		return this.internalSender.sendAsync(convertedMessages);
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		if(this.isInitialized)
		{
			return this.internalSender.closeAsync().thenComposeAsync((v) -> 
			{
				if(MessageSender.this.ownsMessagingFactory)
				{
					return MessageSender.this.messagingFactory.closeAsync();
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
	public CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc) {
		message.setScheduledEnqueuedTimeUtc(scheduledEnqueueTimeUtc);
		org.apache.qpid.proton.message.Message amqpMessage = MessageConverter.convertBrokeredMessageToAmqpMessage((Message)message);
		return this.internalSender.scheduleMessageAsync(new org.apache.qpid.proton.message.Message[] {amqpMessage}, this.messagingFactory.getOperationTimeout()).thenApply(sequenceNumbers -> sequenceNumbers[0]);
	}

	@Override
	public CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber) {
		return this.internalSender.cancelScheduledMessageAsync(new Long[]{sequenceNumber}, this.messagingFactory.getOperationTimeout());
	}

	@Override
	public long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(this.scheduleMessageAsync(message, scheduledEnqueueTimeUtc));
	}

	@Override
	public void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException
	{
		Utils.completeFuture(this.cancelScheduledMessageAsync(sequenceNumber));
	}
	
	MessagingFactory getMessagingFactory()
	{
		return this.messagingFactory;
	}
}
