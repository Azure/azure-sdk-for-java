package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessageReceiver;
import com.microsoft.azure.servicebus.primitives.MessageWithDeliveryTag;
import com.microsoft.azure.servicebus.primitives.MessageWithLockToken;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.SettleModePair;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.Timer;
import com.microsoft.azure.servicebus.primitives.TimerType;
import com.microsoft.azure.servicebus.primitives.Util;


// TODO As part of receive, don't return messages whose lock is already expired. Can happen because of delay between prefetch and actual receive from client.
class BrokeredMessageReceiver extends InitializableEntity implements IMessageReceiver
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
	private final ConcurrentHashMap<UUID, Instant> requestResponseLockTokensToLockTimesMap;
	
	private BrokeredMessageReceiver(ReceiveMode receiveMode)
	{
		super(StringUtil.getRandomString(), null);
		this.receiveMode = receiveMode;
		this.requestResponseLockTokensToLockTimesMap = new ConcurrentHashMap<>();
		this.schedulePruningRequestResponseLockTokens();
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
	public void abandon(UUID lockToken) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.abandonAsync(lockToken));		
	}

	@Override
	public void abandon(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.abandonAsync(lockToken, propertiesToModify));		
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken) {
		return this.abandonAsync(lockToken, null);
	}

	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		this.ensurePeekLockReceiveMode();
		
		return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
			if(requestResponseLocked)
			{
				return this.internalReceiver.abandonMessageAsync(lockToken, propertiesToModify).thenRun(() -> BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
			}
			else
			{
				return this.internalReceiver.abandonMessageAsync(Util.convertUUIDToDotNetBytes(lockToken), propertiesToModify);
			}
		});
	}

	@Override
	public void complete(UUID lockToken) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.completeAsync(lockToken));
	}

	@Override
	public void completeBatch(Collection<? extends IBrokeredMessage> messages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Void> completeAsync(UUID lockToken) {
		this.ensurePeekLockReceiveMode();
		return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
			if(requestResponseLocked)
			{
				return this.internalReceiver.completeMessageAsync(lockToken).thenRun(() -> BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
			}
			else
			{
				return this.internalReceiver.completeMessageAsync(Util.convertUUIDToDotNetBytes(lockToken));
			}
		});
	}

	@Override
	public CompletableFuture<Void> completeBatchAsync(Collection<? extends IBrokeredMessage> messages) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void defer(UUID lockToken) throws InterruptedException, ServiceBusException
	{
		Utils.completeFuture(this.deferAsync(lockToken));		
	}

	@Override
	public void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException 
	{
		Utils.completeFuture(this.deferAsync(lockToken, propertiesToModify));		
	}

	@Override
	public CompletableFuture<Void> deferAsync(UUID lockToken)
	{
		return this.deferAsync(lockToken, null);
	}

	@Override
	public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify)
	{
		this.ensurePeekLockReceiveMode();
		
		return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
			if(requestResponseLocked)
			{
				return this.internalReceiver.deferMessageAsync(lockToken, propertiesToModify).thenRun(() -> BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
			}
			else
			{
				return this.internalReceiver.deferMessageAsync(Util.convertUUIDToDotNetBytes(lockToken), propertiesToModify);
			}
		});
	}

	@Override
	public void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.deadLetterAsync(lockToken));
	}

	@Override
	public void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.deadLetterAsync(lockToken, propertiesToModify));		
	}

	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription));		
	}
	
	@Override
	public void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException {
		Utils.completeFuture(this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify));
	}	

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken) {
		return this.deadLetterAsync(lockToken, null, null, null);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify) {
		return this.deadLetterAsync(lockToken, null, null, propertiesToModify);
	}

	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription)
	{
		return this.deadLetterAsync(lockToken, deadLetterReason, deadLetterErrorDescription, null);
	}
	
	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) {
		this.ensurePeekLockReceiveMode();		
		
		return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
			if(requestResponseLocked)
			{
				return this.internalReceiver.deadLetterMessageAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify).thenRun(() -> BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
			}
			else
			{
				return this.internalReceiver.deadLetterMessageAsync(Util.convertUUIDToDotNetBytes(lockToken), deadLetterReason, deadLetterErrorDescription, propertiesToModify);
			}
		});
	}

	@Override
	public IBrokeredMessage receive() throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveAsync());
	}

	@Override
	public IBrokeredMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException{
		return Utils.completeFuture(this.receiveAsync(serverWaitTime));
	}

	@Override
	public IBrokeredMessage receive(long sequenceNumber) throws InterruptedException, ServiceBusException{
		return Utils.completeFuture(this.receiveAsync(sequenceNumber));
	}

	@Override
	public Collection<IBrokeredMessage> receiveBatch(int maxMessageCount) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveBatchAsync(maxMessageCount));
	}

	@Override
	public Collection<IBrokeredMessage> receiveBatch(int maxMessageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveBatchAsync(maxMessageCount, serverWaitTime));
	}

	@Override
	public Collection<IBrokeredMessage> receiveBatch(Collection<Long> sequenceNumbers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<IBrokeredMessage> receiveAsync() {
		return this.internalReceiver.receiveAsync(1).thenApply(c -> 
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
	public CompletableFuture<IBrokeredMessage> receiveAsync(Duration serverWaitTime) {
		return this.internalReceiver.receiveAsync(1, serverWaitTime).thenApply(c -> 
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
	public CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(int maxMessageCount) {
		return this.internalReceiver.receiveAsync(maxMessageCount).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return convertAmqpMessagesWithDeliveryTagsToBrokeredMessages(c);
		});
	}

	@Override
	public CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime) {
		return this.internalReceiver.receiveAsync(maxMessageCount, serverWaitTime).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return convertAmqpMessagesWithDeliveryTagsToBrokeredMessages(c);
		});
	}
	
	@Override
	public CompletableFuture<IBrokeredMessage> receiveAsync(long sequenceNumber) {
		ArrayList<Long> list = new ArrayList<>();
		list.add(sequenceNumber);
		return  this.receiveBatchAsync(list).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return c.toArray(new BrokeredMessage[0])[0];
		});
	}

	@Override
	public CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(Collection<Long> sequenceNumbers) {
		return this.internalReceiver.receiveBySequenceNumbersAsync(sequenceNumbers.toArray(new Long[0])).thenApply(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else
				return convertAmqpMessagesWithLockTokensToBrokeredMessages(c);
		});
	}

	@Override
	protected CompletableFuture<Void> onClose() {
		if(this.isInitialized)
		{
			return this.internalReceiver.closeAsync().thenComposeAsync((v) -> 
			{
				if(BrokeredMessageReceiver.this.ownsMessagingFactory)
				{
					return BrokeredMessageReceiver.this.messagingFactory.closeAsync();
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
	
	private Collection<IBrokeredMessage> convertAmqpMessagesWithDeliveryTagsToBrokeredMessages(Collection<MessageWithDeliveryTag> amqpMessages)
	{
		ArrayList<IBrokeredMessage> convertedMessages = new ArrayList<IBrokeredMessage>();
		for(MessageWithDeliveryTag amqpMessageWithDeliveryTag : amqpMessages)
		{
			convertedMessages.add(MessageConverter.convertAmqpMessageToBrokeredMessage(amqpMessageWithDeliveryTag));
		}
		
		return convertedMessages;
	}
	
	private Collection<IBrokeredMessage> convertAmqpMessagesWithLockTokensToBrokeredMessages(Collection<MessageWithLockToken> amqpMessages)
	{
		ArrayList<IBrokeredMessage> convertedMessages = new ArrayList<IBrokeredMessage>();
		for(MessageWithLockToken amqpMessageWithLockToken : amqpMessages)
		{
			BrokeredMessage convertedMessage = MessageConverter.convertAmqpMessageToBrokeredMessage(amqpMessageWithLockToken);
			convertedMessages.add(convertedMessage);
			if(!convertedMessage.getLockToken().equals(ClientConstants.ZEROLOCKTOKEN))
			{
				this.requestResponseLockTokensToLockTimesMap.put(convertedMessage.getLockToken(), convertedMessage.getLockedUntilUtc());
			}			
		}
		
		return convertedMessages;
	}
	
	private void ensurePeekLockReceiveMode()
	{
		if(this.receiveMode != ReceiveMode.PeekLock)
		{
			throw new UnsupportedOperationException("Operations Complete/Abandon/DeadLetter/Defer cannot be called on a receiver opened in ReceiveAndDelete mode.");
		}
	}
	
	private CompletableFuture<Boolean> checkIfValidRequestResponseLockTokenAsync(UUID lockToken)
	{
		CompletableFuture<Boolean> future = new CompletableFuture<Boolean>();
		Instant lockedUntilUtc = this.requestResponseLockTokensToLockTimesMap.get(lockToken);
		if(lockedUntilUtc == null)
		{
			future.complete(false);
		}
		else
		{			
			// Should we check for lock expiration here?
			if(lockedUntilUtc.isBefore(Instant.now()))
			{
				future.completeExceptionally(new ServiceBusException(false, "Lock already expired for the lock token."));
			}
			else
			{
				future.complete(true);
			}			
		}
		
		return future;
	}

	@Override
	public CompletableFuture<Instant> renewMessageLockAsync(IBrokeredMessage message) {
		ArrayList<IBrokeredMessage> list = new ArrayList<>();
		list.add(message);
		return this.renewMessageLockBatchAsync(list).thenApply((c) -> c.toArray(new Instant[0])[0]);
	}

//	@Override
	public CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IBrokeredMessage> messages) {	
		this.ensurePeekLockReceiveMode();
		
		UUID[] lockTokens = new UUID[messages.size()];
		int messageIndex = 0;
		for(IBrokeredMessage message : messages)
		{
			UUID lockToken = message.getLockToken();
			if(lockToken.equals(ClientConstants.ZEROLOCKTOKEN))
			{
				throw new UnsupportedOperationException("Lock of a message received in ReceiveAndDelete mode cannot be renewed.");
			}
			lockTokens[messageIndex++] = lockToken;
		}
		
		return this.internalReceiver.renewMessageLocksAsync(lockTokens, null, this.messagingFactory.getOperationTimeout()).thenApply(
				(newLockedUntilTimes) ->
					{
						// Assuming both collections are of same size and in same order (order doesn't really matter as all instants in the response are same).
						Iterator<? extends IBrokeredMessage> messageIterator = messages.iterator();
						Iterator<Instant> lockTimeIterator = newLockedUntilTimes.iterator();
						while(messageIterator.hasNext() && lockTimeIterator.hasNext())
						{
							BrokeredMessage message = (BrokeredMessage)messageIterator.next();
							Instant lockedUntilUtc = lockTimeIterator.next();
							message.setLockedUntilUtc(lockedUntilUtc);
							if(BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.containsKey(message.getLockToken()))
							{
								BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.put(message.getLockToken(), lockedUntilUtc);
							}
						}
						return newLockedUntilTimes;
					}
				);
	}

	@Override
	public Instant renewMessageLock(IBrokeredMessage message) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.renewMessageLockAsync(message));
	}

//	@Override
	public Collection<Instant> renewMessageLockBatch(Collection<? extends IBrokeredMessage> messages) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.renewMessageLockBatchAsync(messages));
	}
	
	private void schedulePruningRequestResponseLockTokens()
	{
		// Run it every 1 hour
		Timer.schedule(new Runnable(){
			public void run()
			{
				Instant systemTime = Instant.now();
				Entry<UUID, Instant>[] copyOfEntries = (Entry<UUID, Instant>[])BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.entrySet().toArray();
				for(Entry<UUID, Instant> entry : copyOfEntries)
				{
					if(entry.getValue().isBefore(systemTime))
					{
						// lock expired
						BrokeredMessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(entry.getKey());
					}
				}
			}
		}, Duration.ofSeconds(3600), TimerType.RepeatRun);
	}
}
