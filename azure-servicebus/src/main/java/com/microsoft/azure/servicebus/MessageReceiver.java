package com.microsoft.azure.servicebus;

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
import com.microsoft.azure.servicebus.primitives.CoreMessageReceiver;
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
class MessageReceiver extends InitializableEntity implements IMessageReceiver, IMessageBrowser
{
	private static final int DEFAULT_PREFETCH_COUNT = 100;
	
	private final ReceiveMode receiveMode;
	private boolean ownsMessagingFactory;	
	private ConnectionStringBuilder amqpConnectionStringBuilder = null;
	private String entityPath = null;
	private MessagingFactory messagingFactory = null;
	private CoreMessageReceiver internalReceiver = null;
	private boolean isInitialized = false;
	private MessageBrowser browser = null;
	private int messagePrefetchCount = DEFAULT_PREFETCH_COUNT;
	
	private final ConcurrentHashMap<UUID, Instant> requestResponseLockTokensToLockTimesMap;
	
	private MessageReceiver(ReceiveMode receiveMode)
	{
		super(StringUtil.getShortRandomString(), null);
		this.receiveMode = receiveMode;
		this.requestResponseLockTokensToLockTimesMap = new ConcurrentHashMap<>();	
	}
	
	private MessageReceiver(MessagingFactory messagingFactory, String entityPath, boolean ownsMessagingFactory, ReceiveMode receiveMode)
	{		
		this(receiveMode);
		
		this.messagingFactory = messagingFactory;
		this.entityPath = entityPath;
		this.ownsMessagingFactory = ownsMessagingFactory;
	}
	
	MessageReceiver(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode)
	{
		this(receiveMode);
		
		this.amqpConnectionStringBuilder = amqpConnectionStringBuilder;
		this.entityPath = this.amqpConnectionStringBuilder.getEntityPath();
		this.ownsMessagingFactory = true;
	}
	
	MessageReceiver(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode)
	{		
		this(messagingFactory, entityPath, false, receiveMode);
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
				CompletableFuture<Void> acceptReceiverFuture;
				if(this.internalReceiver == null)
				{
					CompletableFuture<CoreMessageReceiver> receiverFuture;
					if(MessageReceiver.this.isSessionReceiver())
					{
						receiverFuture = CoreMessageReceiver.create(this.messagingFactory, StringUtil.getShortRandomString(), this.entityPath, this.getRequestedSessionId(), this.isBrowsableSession(), this.messagePrefetchCount, getSettleModePairForRecevieMode(this.receiveMode));
					}
					else
					{
						receiverFuture = CoreMessageReceiver.create(this.messagingFactory, StringUtil.getShortRandomString(), this.entityPath, this.messagePrefetchCount, getSettleModePairForRecevieMode(this.receiveMode));
					}
					
					acceptReceiverFuture = receiverFuture.thenAcceptAsync((r) -> 
					{
						this.internalReceiver = r;					
					});
				}
				else
				{
					acceptReceiverFuture = CompletableFuture.completedFuture(null);
				}				
				
				return acceptReceiverFuture.thenRunAsync(() -> 
				{					
					this.isInitialized = true;
					this.schedulePruningRequestResponseLockTokens();
					this.browser = new MessageBrowser(this);
				});
			});
		}
	}
	
	protected boolean isSessionReceiver()
	{
		return false;
	}
	
	protected boolean isBrowsableSession()
	{
		return false;
	}
	
	protected String getRequestedSessionId()
	{
		return null;
	}
	
	protected final CoreMessageReceiver getInternalReceiver()
	{
		return this.internalReceiver;
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
				return this.internalReceiver.abandonMessageAsync(lockToken, propertiesToModify).thenRun(() -> MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
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
	public void completeBatch(Collection<? extends IMessage> messages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Void> completeAsync(UUID lockToken) {
		this.ensurePeekLockReceiveMode();
		return this.checkIfValidRequestResponseLockTokenAsync(lockToken).thenCompose((requestResponseLocked) -> {
			if(requestResponseLocked)
			{
				return this.internalReceiver.completeMessageAsync(lockToken).thenRun(() -> MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
			}
			else
			{
				return this.internalReceiver.completeMessageAsync(Util.convertUUIDToDotNetBytes(lockToken));
			}
		});
	}

	@Override
	public CompletableFuture<Void> completeBatchAsync(Collection<? extends IMessage> messages) {
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
				return this.internalReceiver.deferMessageAsync(lockToken, propertiesToModify).thenRun(() -> MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
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
				return this.internalReceiver.deadLetterMessageAsync(lockToken, deadLetterReason, deadLetterErrorDescription, propertiesToModify).thenRun(() -> MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(lockToken));
			}
			else
			{
				return this.internalReceiver.deadLetterMessageAsync(Util.convertUUIDToDotNetBytes(lockToken), deadLetterReason, deadLetterErrorDescription, propertiesToModify);
			}
		});
	}

	@Override
	public IMessage receive() throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveAsync());
	}

	@Override
	public IMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException{
		return Utils.completeFuture(this.receiveAsync(serverWaitTime));
	}

	@Override
	public IMessage receive(long sequenceNumber) throws InterruptedException, ServiceBusException{
		return Utils.completeFuture(this.receiveAsync(sequenceNumber));
	}

	@Override
	public Collection<IMessage> receiveBatch(int maxMessageCount) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveBatchAsync(maxMessageCount));
	}

	@Override
	public Collection<IMessage> receiveBatch(int maxMessageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.receiveBatchAsync(maxMessageCount, serverWaitTime));
	}

	@Override
	public Collection<IMessage> receiveBatch(Collection<Long> sequenceNumbers) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<IMessage> receiveAsync() {
		return this.internalReceiver.receiveAsync(1).thenApplyAsync(c -> 
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
	public CompletableFuture<IMessage> receiveAsync(Duration serverWaitTime) {
		return this.internalReceiver.receiveAsync(1, serverWaitTime).thenApplyAsync(c -> 
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
	public CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount) {
		return this.internalReceiver.receiveAsync(maxMessageCount).thenApplyAsync(c -> 
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
	public CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime) {
		return this.internalReceiver.receiveAsync(maxMessageCount, serverWaitTime).thenApplyAsync(c -> 
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
	public CompletableFuture<IMessage> receiveAsync(long sequenceNumber) {
		ArrayList<Long> list = new ArrayList<>();
		list.add(sequenceNumber);
		return  this.receiveBatchAsync(list).thenApplyAsync(c -> 
		{	
			if(c == null)
				return null;
			else if (c.isEmpty())
				return null;
			else		
				return c.toArray(new Message[0])[0];
		});
	}

	@Override
	public CompletableFuture<Collection<IMessage>> receiveBatchAsync(Collection<Long> sequenceNumbers) {
		return this.internalReceiver.receiveBySequenceNumbersAsync(sequenceNumbers.toArray(new Long[0])).thenApplyAsync(c -> 
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
			CompletableFuture<Void> closeReceiverFuture = this.internalReceiver.closeAsync();		
			
			return closeReceiverFuture.thenComposeAsync((v) -> 
			{
				if(MessageReceiver.this.ownsMessagingFactory)
				{
					return MessageReceiver.this.messagingFactory.closeAsync();
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
	public int getMessagePrefetchCount()
	{
		return this.messagePrefetchCount;
	}

	@Override
	public void setMessagePrefetchCount(int prefetchCount) throws ServiceBusException
	{
		this.messagePrefetchCount = prefetchCount;
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
	
	private Collection<IMessage> convertAmqpMessagesWithDeliveryTagsToBrokeredMessages(Collection<MessageWithDeliveryTag> amqpMessages)
	{
		ArrayList<IMessage> convertedMessages = new ArrayList<IMessage>();
		for(MessageWithDeliveryTag amqpMessageWithDeliveryTag : amqpMessages)
		{
			convertedMessages.add(MessageConverter.convertAmqpMessageToBrokeredMessage(amqpMessageWithDeliveryTag));
		}
		
		return convertedMessages;
	}
	
	private Collection<IMessage> convertAmqpMessagesWithLockTokensToBrokeredMessages(Collection<MessageWithLockToken> amqpMessages)
	{
		ArrayList<IMessage> convertedMessages = new ArrayList<IMessage>();
		for(MessageWithLockToken amqpMessageWithLockToken : amqpMessages)
		{
			Message convertedMessage = MessageConverter.convertAmqpMessageToBrokeredMessage(amqpMessageWithLockToken);
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
	public CompletableFuture<Instant> renewMessageLockAsync(IMessage message) {
		ArrayList<IMessage> list = new ArrayList<>();
		list.add(message);
		return this.renewMessageLockBatchAsync(list).thenApply((c) -> c.toArray(new Instant[0])[0]);
	}

//	@Override
	public CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IMessage> messages) {	
		this.ensurePeekLockReceiveMode();
		
		UUID[] lockTokens = new UUID[messages.size()];
		int messageIndex = 0;
		for(IMessage message : messages)
		{
			UUID lockToken = message.getLockToken();
			if(lockToken.equals(ClientConstants.ZEROLOCKTOKEN))
			{
				throw new UnsupportedOperationException("Lock of a message received in ReceiveAndDelete mode cannot be renewed.");
			}
			lockTokens[messageIndex++] = lockToken;
		}
		
		return this.internalReceiver.renewMessageLocksAsync(lockTokens).thenApplyAsync(
				(newLockedUntilTimes) ->
					{
						// Assuming both collections are of same size and in same order (order doesn't really matter as all instants in the response are same).
						Iterator<? extends IMessage> messageIterator = messages.iterator();
						Iterator<Instant> lockTimeIterator = newLockedUntilTimes.iterator();
						while(messageIterator.hasNext() && lockTimeIterator.hasNext())
						{
							Message message = (Message)messageIterator.next();
							Instant lockedUntilUtc = lockTimeIterator.next();
							message.setLockedUntilUtc(lockedUntilUtc);
							if(this.requestResponseLockTokensToLockTimesMap.containsKey(message.getLockToken()))
							{
								this.requestResponseLockTokensToLockTimesMap.put(message.getLockToken(), lockedUntilUtc);
							}
						}
						return newLockedUntilTimes;
					}
				);
	}

	@Override
	public Instant renewMessageLock(IMessage message) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.renewMessageLockAsync(message));
	}

//	@Override
	public Collection<Instant> renewMessageLockBatch(Collection<? extends IMessage> messages) throws InterruptedException, ServiceBusException {
		return Utils.completeFuture(this.renewMessageLockBatchAsync(messages));
	}
	
	@Override
	public IMessage peek() throws InterruptedException, ServiceBusException {
		return this.browser.peek();
	}

	@Override
	public IMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException {
		return this.browser.peek(fromSequenceNumber);
	}

	@Override
	public Collection<IMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException {
		return this.browser.peekBatch(messageCount);
	}

	@Override
	public Collection<IMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException {
		return this.browser.peekBatch(fromSequenceNumber, messageCount);
	}

	@Override
	public CompletableFuture<IMessage> peekAsync() {
		return this.browser.peekAsync();
	}

	@Override
	public CompletableFuture<IMessage> peekAsync(long fromSequenceNumber) {
		return this.browser.peekAsync(fromSequenceNumber);
	}

	@Override
	public CompletableFuture<Collection<IMessage>> peekBatchAsync(int messageCount) {
		return this.browser.peekBatchAsync(messageCount);
	}

	@Override
	public CompletableFuture<Collection<IMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount) {
		return this.browser.peekBatchAsync(fromSequenceNumber, messageCount);
	}
	
	private void schedulePruningRequestResponseLockTokens()
	{
		// Run it every 1 hour
		Timer.schedule(new Runnable(){
			public void run()
			{
				Instant systemTime = Instant.now();
				Entry<UUID, Instant>[] copyOfEntries = (Entry<UUID, Instant>[])MessageReceiver.this.requestResponseLockTokensToLockTimesMap.entrySet().toArray();
				for(Entry<UUID, Instant> entry : copyOfEntries)
				{
					if(entry.getValue().isBefore(systemTime))
					{
						// lock expired
						MessageReceiver.this.requestResponseLockTokensToLockTimesMap.remove(entry.getKey());
					}
				}
			}
		}, Duration.ofSeconds(3600), TimerType.RepeatRun);
	}
	
	MessagingFactory getMessagingFactory()
	{
		return this.messagingFactory;
	}
}
