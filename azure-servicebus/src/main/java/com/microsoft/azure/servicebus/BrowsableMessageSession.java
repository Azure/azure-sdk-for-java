package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * A session object that can only be used to browse messages and state of a server side session. It cannot be used to receive messages from the service or to set state of the session.
 *
 */
final class BrowsableMessageSession extends MessageSession
{
	private static final String INVALID_OPERATION_ERROR_MESSAGE = "Unsupported operation on a browse only session.";	
	
	BrowsableMessageSession(String sessionId, MessagingFactory messagingFactory, String entityPath)
	{
		super(messagingFactory, entityPath, sessionId, ReceiveMode.PEEKLOCK);	
//		try {
//			this.initializeAsync().get();
//		} catch (InterruptedException | ExecutionException e) {
//			
//		}
	}
	
	@Override
	protected boolean isBrowsableSession()
	{
		return true;
	}
	
	@Override
	public String getSessionId()
	{
		return this.getRequestedSessionId();
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
	public CompletableFuture<Void> renewSessionLockAsync() {
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
	public CompletableFuture<Void> completeBatchAsync(Collection<? extends IMessage> messages) {
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
	public CompletableFuture<IMessage> receiveAsync()
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<IMessage> receiveAsync(Duration serverWaitTime)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<IMessage> receiveDeferredMessageAsync(long sequenceNumber)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<IMessage>> receiveDeferredMessageBatchAsync(Collection<Long> sequenceNumbers)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Instant> renewMessageLockAsync(IMessage message)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}
	
	@Override
	public CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IMessage> messages)
	{
		throw new UnsupportedOperationException(INVALID_OPERATION_ERROR_MESSAGE);
	}	
}
