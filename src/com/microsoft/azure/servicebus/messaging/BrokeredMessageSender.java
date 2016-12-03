package com.microsoft.azure.servicebus.messaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.MessageSender;
import com.microsoft.azure.servicebus.MessagingFactory;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.microsoft.azure.servicebus.StringUtil;

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
	public void send(BrokeredMessage message) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.sendAsync(message));		
	}

	@Override
	public void sendBatch(Collection<BrokeredMessage> message) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.sendBatchAsync(message));
	}

	@Override
	public CompletableFuture<Void> sendAsync(BrokeredMessage message) {
		Message amqpMessage = MessageConverter.convertBrokeredMessageToAmqpMessage(message);
		return this.internalSender.send(amqpMessage);
	}

	@Override
	public CompletableFuture<Void> sendBatchAsync(Collection<BrokeredMessage> messages) {
		ArrayList<Message> convertedMessages = new ArrayList<Message>();
		for(BrokeredMessage message : messages)
		{
			convertedMessages.add(MessageConverter.convertBrokeredMessageToAmqpMessage(message));
		}
		
		return this.internalSender.send(convertedMessages);
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		if(this.isInitialized)
		{
			return this.internalSender.close().thenComposeAsync((v) -> 
			{
				if(BrokeredMessageSender.this.ownsMessagingFactory)
				{
					return BrokeredMessageSender.this.messagingFactory.close();
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

}
