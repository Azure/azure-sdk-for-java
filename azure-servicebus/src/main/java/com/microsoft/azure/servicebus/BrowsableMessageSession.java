package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.primitives.MessageReceiver;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

final class BrowsableMessageSession extends BrokeredMessageSession
{
	private static final String INVALID_OPERATION_ERROR_MESSAGE = "Unsupported operation on a browse only session.";
	
	private final String sessionId;
	
	BrowsableMessageSession(String sessionId, MessagingFactory messagingFactory, MessageReceiver internalReceiver, String entityPath)
	{
		super(messagingFactory, internalReceiver, entityPath, ReceiveMode.PeekLock);
		this.sessionId = sessionId;
		try {
			this.initializeAsync().get();
		} catch (InterruptedException | ExecutionException | IOException e) {
			// We can ignore it, as init is a no-operation in this case
		}
	}
	
	@Override
	public String getSessionId()
	{
		return this.sessionId;
	}
	
	@Override
	public Instant getLockedUntilUtc() {
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public int getPrefetchCount()
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public void setPrefetchCount(int prefetchCount) throws ServiceBusException
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Void> setStateAsync(byte[] sessionState) {
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Void> renewLockAsync() {
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public ReceiveMode getReceiveMode() {
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Void> completeAsync(UUID lockToken)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Void> completeBatchAsync(Collection<? extends IBrokeredMessage> messages) {
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<IBrokeredMessage> receiveAsync()
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<IBrokeredMessage> receiveAsync(Duration serverWaitTime)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<IBrokeredMessage> receiveAsync(long sequenceNumber)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(int maxMessageCount)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(Collection<Long> sequenceNumbers)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Instant> renewMessageLockAsync(IBrokeredMessage message)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IBrokeredMessage> messages)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}	
}
