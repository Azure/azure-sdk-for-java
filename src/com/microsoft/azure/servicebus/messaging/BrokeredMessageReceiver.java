package com.microsoft.azure.servicebus.messaging;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;

import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.MessageReceiver;
import com.microsoft.azure.servicebus.MessageWithDeliveryTag;
import com.microsoft.azure.servicebus.MessagingFactory;
import com.microsoft.azure.servicebus.ServiceBusException;
import com.microsoft.azure.servicebus.SettleModePair;
import com.microsoft.azure.servicebus.StringUtil;


// TODO As part of receive, don't return messages whose lock is already expired. Can happen because of delay between prefetch and actual receive from client.
public class BrokeredMessageReceiver extends InitializableEntity implements IMessageReceiver
{
	private static final int DEFAULT_PREFETCH_COUNT = 100;
	
	private final ReceiveMode receiveMode;
	private boolean ownsMessagingFactory;
	private ConnectionStringBuilder amqpConnectionStringBuilder = null;
	private String entityPath = null;
	private MessagingFactory messagingFactory = null;
	private MessageReceiver internalReceiver = null;
	private boolean isInitialized = false;
	private int prefetchCount = DEFAULT_PREFETCH_COUNT;
	
	private BrokeredMessageReceiver(ReceiveMode receiveMode)
	{
		super(StringUtil.getRandomString(), null);
		this.receiveMode = receiveMode;
	}
	
	BrokeredMessageReceiver(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode)
	{
		this(receiveMode);
		
		this.amqpConnectionStringBuilder = amqpConnectionStringBuilder;
		this.entityPath = this.amqpConnectionStringBuilder.getEntityPath();
		this.ownsMessagingFactory = true;
	}
	
	BrokeredMessageReceiver(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode)
	{		
		this(messagingFactory, entityPath, false, receiveMode);
	}
			
	private BrokeredMessageReceiver(MessagingFactory messagingFactory, String entityPath, boolean ownsMessagingFactory, ReceiveMode receiveMode)
	{		
		this(receiveMode);
		
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
				factoryFuture = MessagingFactory.createFromConnectionStringBuilderAsync(amqpConnectionStringBuilder).thenAccept((f) -> {BrokeredMessageReceiver.this.messagingFactory = f;});
			}
			else
			{
				factoryFuture = CompletableFuture.completedFuture(null);
			}
			
			return factoryFuture.thenCompose((v) ->
			{
				CompletableFuture<MessageReceiver> receiverFuture = MessageReceiver.create(BrokeredMessageReceiver.this.messagingFactory, StringUtil.getRandomString(), BrokeredMessageReceiver.this.entityPath, this.prefetchCount, getSettleModePairForRecevieMode(this.receiveMode));
				return receiverFuture.thenAccept((r) -> 
				{
					BrokeredMessageReceiver.this.internalReceiver = r;
					BrokeredMessageReceiver.this.isInitialized = true;
				});
			});
		}
	}
	
	@Override
	public String getEntityPath() {
		return this.entityPath;
	}

	@Override
	public ReceiveMode getReceiveMode() {
		return this.receiveMode;
	}

	@Override
	public void abandon(UUID lockToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abandon(UUID lockToken, Map<String, Object> propertiesToModify) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void complete(UUID lockToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completeBatch(Collection<UUID> lockTokens) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Void> completeAsync(UUID lockToken) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> completeBatchAsync(Collection<UUID> lockTokens) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void defer(UUID lockToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void defer(UUID lockToken, Map<String, Object> propertiesToModify) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Void> deferAsync(UUID lockToken) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deadLetter(UUID lockToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason,
			String deadLetterErrorDescription) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BrokeredMessage receive() throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveAsync());
	}

	@Override
	public BrokeredMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException{
		return Utils.completeFuture(this.receiveAsync(serverWaitTime));
	}

	@Override
	public BrokeredMessage receive(long sequenceNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<BrokeredMessage> receiveBatch(int messageCount) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveBatchAsync(messageCount));
	}

	@Override
	public Collection<BrokeredMessage> receiveBatch(int messageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveBatchAsync(messageCount, serverWaitTime));
	}

	@Override
	public Collection<BrokeredMessage> receiveBatch(Collection<Long> sequenceNumbers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<BrokeredMessage> receiveAsync() {
		return this.internalReceiver.receive(1).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return MessageConverter.convertAmqpMessageToBrokeredMessage(c.toArray(new MessageWithDeliveryTag[0])[0]);
		});
	}

	@Override
	public CompletableFuture<BrokeredMessage> receiveAsync(Duration serverWaitTime) {
		return this.internalReceiver.receive(1, serverWaitTime).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return MessageConverter.convertAmqpMessageToBrokeredMessage(c.toArray(new MessageWithDeliveryTag[0])[0]);
		});
	}

	@Override
	public CompletableFuture<BrokeredMessage> receiveAsync(long sequenceNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Collection<BrokeredMessage>> receiveBatchAsync(int messageCount) {
		return this.internalReceiver.receive(messageCount).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return convertAmqpMessagesToBrokeredMessages(c);
		});
	}

	@Override
	public CompletableFuture<Collection<BrokeredMessage>> receiveBatchAsync(int messageCount, Duration serverWaitTime) {
		return this.internalReceiver.receive(messageCount, serverWaitTime).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return convertAmqpMessagesToBrokeredMessages(c);
		});
	}

	@Override
	public CompletableFuture<Collection<BrokeredMessage>> receiveBatchAsync(Collection<Long> sequenceNumbers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		if(this.isInitialized)
		{
			return this.internalReceiver.close().thenComposeAsync((v) -> 
			{
				if(BrokeredMessageReceiver.this.ownsMessagingFactory)
				{
					return BrokeredMessageReceiver.this.messagingFactory.close();
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
	public int getPrefetchCount()
	{
		return this.prefetchCount;
	}

	@Override
	public void setPrefetchCount(int prefetchCount) throws ServiceBusException
	{
		this.prefetchCount = prefetchCount;
		if(this.isInitialized)
		{
			this.internalReceiver.setPrefetchCount(prefetchCount);
		}
	}
	
	private static SettleModePair getSettleModePairForRecevieMode(ReceiveMode receiveMode)
	{
		if(receiveMode == ReceiveMode.ReceiveAndDelete)
		{
			return new SettleModePair(SenderSettleMode.SETTLED, ReceiverSettleMode.FIRST);
		}
		else
		{
			return new SettleModePair(SenderSettleMode.UNSETTLED, ReceiverSettleMode.SECOND);
		}
	}
	
	private Collection<BrokeredMessage> convertAmqpMessagesToBrokeredMessages(Collection<MessageWithDeliveryTag> amqpMessages)
	{
		ArrayList<BrokeredMessage> convertedMessages = new ArrayList<BrokeredMessage>();
		for(MessageWithDeliveryTag amqpMessageWithDeliveryTag : amqpMessages)
		{
			convertedMessages.add(MessageConverter.convertAmqpMessageToBrokeredMessage(amqpMessageWithDeliveryTag));
		}
		
		return convertedMessages;
	}

}
