package com.microsoft.azure.servicebus.messaging;

import java.io.IOException;
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
			
	BrokeredMessageSender(MessagingFactory messagingFactory, String entityPath, boolean ownsMessagingFactory)
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
				factoryFuture = MessagingFactory.createFromConnectionStringBuilder(amqpConnectionStringBuilder).thenAccept((f) -> {BrokeredMessageSender.this.messagingFactory = f;});
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
	
	/*
	static CompletableFuture<BrokeredMessageSender> createInstanceFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder) throws IOException
	{
		return MessagingFactory.createFromConnectionStringBuilder(amqpConnectionStringBuilder).thenComposeAsync((f) -> createInstanceFromMessagingFactoryAsync(f, true));
	}
	
	static CompletableFuture<BrokeredMessageSender> createInstanceFromMessagingFactoryAsync(MessagingFactory messagingFactory)
	{
		return createInstanceFromMessagingFactoryAsync(messagingFactory, false);
	}
	
	static CompletableFuture<BrokeredMessageSender> createInstanceFromMessagingFactoryAsync(MessagingFactory messagingFactory, boolean ownsMessagingFactory)
	{
		
	}
	*/	
		
	
	@Override
	public void send(BrokeredMessage message) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.sendAsync(message));
		
	}

	@Override
	public void sendBatch(Iterable<BrokeredMessage> message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Void> sendAsync(BrokeredMessage message) {
		Message amqpMessage = message.toAmqpMessage();
		return this.internalSender.send(amqpMessage);
	}

	@Override
	public CompletableFuture<Void> sendBatchAsync(Iterable<BrokeredMessage> message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		if(this.isInitialized)
		{
			return CompletableFuture.completedFuture(null);
		}
		else
		{
			return this.internalSender.close().thenCompose((v) -> 
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
	}

	@Override
	public String getEntityPath() {
		return this.entityPath;
	}

}
