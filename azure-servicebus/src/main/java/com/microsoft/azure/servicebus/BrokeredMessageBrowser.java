package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessageBrowser;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.StringUtil;

public class BrokeredMessageBrowser extends InitializableEntity implements IMessageBrowser
{
	private ConnectionStringBuilder amqpConnectionStringBuilder = null;
	private String entityPath = null;
	private MessagingFactory messagingFactory = null;
	private boolean ownsMessagingFactory;
	private boolean isInitialized = false;
	private MessageBrowser internalBrowser = null;
	private long lastPeekedSequenceNumber = 0;
	private String sessionId = null;
	
	private BrokeredMessageBrowser()
	{
		super(StringUtil.getRandomString(), null);
	}
	
	BrokeredMessageBrowser(ConnectionStringBuilder amqpConnectionStringBuilder)
	{
		this();
		
		this.amqpConnectionStringBuilder = amqpConnectionStringBuilder;
		this.entityPath = this.amqpConnectionStringBuilder.getEntityPath();
		this.ownsMessagingFactory = true;
	}
	
	BrokeredMessageBrowser(MessagingFactory messagingFactory, String entityPath)
	{		
		this(messagingFactory, entityPath, false);
	}
			
	private BrokeredMessageBrowser(MessagingFactory messagingFactory, String entityPath, boolean ownsMessagingFactory)
	{		
		this();
		
		this.messagingFactory = messagingFactory;
		this.entityPath = entityPath;
		this.ownsMessagingFactory = ownsMessagingFactory;
	}
	
	@Override
	public String getEntityPath() {
		return this.entityPath;
	}	

	@Override
	synchronized CompletableFuture<Void> initializeAsync() throws IOException {
		if(this.isInitialized)
		{
			return CompletableFuture.completedFuture(null);
		}
		else
		{
			CompletableFuture<Void> factoryFuture;
			if(this.messagingFactory == null)
			{
				factoryFuture = MessagingFactory.createFromConnectionStringBuilderAsync(amqpConnectionStringBuilder).thenAccept((f) -> {BrokeredMessageBrowser.this.messagingFactory = f;});
			}
			else
			{
				factoryFuture = CompletableFuture.completedFuture(null);
			}
			
			return factoryFuture.thenCompose((v) ->
			{
				CompletableFuture<MessageBrowser> browserFuture = MessageBrowser.create(BrokeredMessageBrowser.this.messagingFactory, StringUtil.getRandomString(), BrokeredMessageBrowser.this.entityPath);
				return browserFuture.thenAccept((b) -> 
				{
					BrokeredMessageBrowser.this.internalBrowser = b;
					BrokeredMessageBrowser.this.isInitialized = true;
				});
			});
		}
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		if(this.isInitialized)
		{
			return this.internalBrowser.closeAsync().thenComposeAsync((v) -> 
			{
				if(BrokeredMessageBrowser.this.ownsMessagingFactory)
				{
					return BrokeredMessageBrowser.this.messagingFactory.closeAsync();
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
	public IBrokeredMessage peek() throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekAsync());
	}

	@Override
	public IBrokeredMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekAsync(fromSequenceNumber));
	}

	@Override
	public Collection<IBrokeredMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekBatchAsync(messageCount));
	}

	@Override
	public Collection<IBrokeredMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.peekBatchAsync(fromSequenceNumber, messageCount));
	}

	@Override
	public CompletableFuture<IBrokeredMessage> peekAsync() {
		return this.peekAsync(this.lastPeekedSequenceNumber + 1);
	}

	@Override
	public CompletableFuture<IBrokeredMessage> peekAsync(long fromSequenceNumber) {
		return this.peekBatchAsync(fromSequenceNumber, 1).thenApply((c) -> 
		{
			IBrokeredMessage message = null;
			Iterator<IBrokeredMessage> iterator = c.iterator();
			if(iterator.hasNext())
			{
				message = iterator.next();
				iterator.remove();
			}
			return message;
		});
	}

	@Override
	public CompletableFuture<Collection<IBrokeredMessage>> peekBatchAsync(int messageCount) {
		return this.peekBatchAsync(this.lastPeekedSequenceNumber + 1, messageCount);
	}

	@Override
	public CompletableFuture<Collection<IBrokeredMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount) {
		CompletableFuture<Collection<Message>> peekFuture = this.internalBrowser.peekMessages(fromSequenceNumber, messageCount, this.sessionId, this.messagingFactory.getOperationTimeout());
		return peekFuture.thenApply((peekedMessages) -> 
		{
			ArrayList<IBrokeredMessage> convertedMessages = new ArrayList<IBrokeredMessage>();
			if(peekedMessages != null)
			{
				long sequenceNumberOfLastMessage = 0;
				for(Message message : peekedMessages)
				{
					BrokeredMessage convertedMessage = MessageConverter.convertAmqpMessageToBrokeredMessage(message);
					sequenceNumberOfLastMessage = convertedMessage.getSequenceNumber();
					convertedMessages.add(convertedMessage);
				}
				
				if(sequenceNumberOfLastMessage > 0)
				{
					this.lastPeekedSequenceNumber = sequenceNumberOfLastMessage;
				}
			}		
			
			return convertedMessages;
		});
	}
}
