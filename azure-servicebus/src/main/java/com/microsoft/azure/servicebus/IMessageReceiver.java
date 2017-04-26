package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface IMessageReceiver extends IMessageEntity, IMessageBrowser{
	ReceiveMode getReceiveMode();

    void abandon(UUID lockToken) throws InterruptedException, ServiceBusException;

    void abandon(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;
    
    CompletableFuture<Void> abandonAsync(UUID lockToken);

    CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void complete(UUID lockToken) throws InterruptedException, ServiceBusException;

    void completeBatch(Collection<? extends IMessage> messages);

    CompletableFuture<Void> completeAsync(UUID lockToken);

    CompletableFuture<Void> completeBatchAsync(Collection<? extends IMessage> messages);

    void defer(UUID lockToken) throws InterruptedException, ServiceBusException;

    void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> deferAsync(UUID lockToken);

    CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException;

    void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException;
    
    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> deadLetterAsync(UUID lockToken);

    CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription);
    
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify);

    IMessage receive() throws InterruptedException, ServiceBusException;
    
    IMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException;

    IMessage receive(long sequenceNumber) throws InterruptedException, ServiceBusException;

    Collection<IMessage> receiveBatch(int maxMessageCount) throws InterruptedException, ServiceBusException;
    
    Collection<IMessage> receiveBatch(int maxMessageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException;
    
    Collection<IMessage> receiveBatch(Collection<Long> sequenceNumbers) throws InterruptedException, ServiceBusException;

    CompletableFuture<IMessage> receiveAsync();

    CompletableFuture<IMessage> receiveAsync(Duration serverWaitTime);

    CompletableFuture<IMessage> receiveAsync(long sequenceNumber);

    CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount);

    CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime);

    CompletableFuture<Collection<IMessage>> receiveBatchAsync(Collection<Long> sequenceNumbers);
    
    CompletableFuture<Instant> renewMessageLockAsync(IMessage message);
    
    //CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IBrokeredMessage> messages);
    
    Instant renewMessageLock(IMessage message) throws InterruptedException, ServiceBusException;
    
    //Collection<Instant> renewMessageLockBatch(Collection<? extends IBrokeredMessage> messages) throws InterruptedException, ServiceBusException;    
    
    int getPrefetchCount();
    
    void setPrefetchCount(int prefetchCount) throws ServiceBusException;
}
