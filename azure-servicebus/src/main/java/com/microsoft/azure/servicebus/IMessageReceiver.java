package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface IMessageReceiver extends IMessageEntity{
	ReceiveMode getReceiveMode();

    void abandon(IBrokeredMessage message) throws InterruptedException, ServiceBusException;

    void abandon(IBrokeredMessage message, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;
    
    CompletableFuture<Void> abandonAsync(IBrokeredMessage message);

    CompletableFuture<Void> abandonAsync(IBrokeredMessage message, Map<String, Object> propertiesToModify);
    
    void complete(IBrokeredMessage message) throws InterruptedException, ServiceBusException;

    void completeBatch(Collection<? extends IBrokeredMessage> messages);

    CompletableFuture<Void> completeAsync(IBrokeredMessage message);

    CompletableFuture<Void> completeBatchAsync(Collection<? extends IBrokeredMessage> messages);

    void defer(IBrokeredMessage message) throws InterruptedException, ServiceBusException;

    void defer(IBrokeredMessage message, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> deferAsync(IBrokeredMessage message);

    CompletableFuture<Void> deferAsync(IBrokeredMessage message, Map<String, Object> propertiesToModify);
    
    void deadLetter(IBrokeredMessage message) throws InterruptedException, ServiceBusException;

    void deadLetter(IBrokeredMessage message, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    void deadLetter(IBrokeredMessage message, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException;
    
    void deadLetter(IBrokeredMessage message, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> deadLetterAsync(IBrokeredMessage message);

    CompletableFuture<Void> deadLetterAsync(IBrokeredMessage message, Map<String, Object> propertiesToModify);

    CompletableFuture<Void> deadLetterAsync(IBrokeredMessage message, String deadLetterReason, String deadLetterErrorDescription);
    
    CompletableFuture<Void> deadLetterAsync(IBrokeredMessage message, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify);

    IBrokeredMessage receive() throws InterruptedException, ServiceBusException;
    
    IBrokeredMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException;

    IBrokeredMessage receive(long sequenceNumber) throws InterruptedException, ServiceBusException;

    Collection<IBrokeredMessage> receiveBatch(int maxMessageCount) throws InterruptedException, ServiceBusException;
    
    Collection<IBrokeredMessage> receiveBatch(int maxMessageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException;
    
    Collection<IBrokeredMessage> receiveBatch(Collection<Long> sequenceNumbers) throws InterruptedException, ServiceBusException;

    CompletableFuture<IBrokeredMessage> receiveAsync();

    CompletableFuture<IBrokeredMessage> receiveAsync(Duration serverWaitTime);

    CompletableFuture<IBrokeredMessage> receiveAsync(long sequenceNumber);

    CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(int maxMessageCount);

    CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime);

    CompletableFuture<Collection<IBrokeredMessage>> receiveBatchAsync(Collection<Long> sequenceNumbers);
    
    CompletableFuture<Instant> renewMessageLockAsync(IBrokeredMessage message);
    
    //CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IBrokeredMessage> messages);
    
    Instant renewMessageLock(IBrokeredMessage message) throws InterruptedException, ServiceBusException;
    
    //Collection<Instant> renewMessageLockBatch(Collection<? extends IBrokeredMessage> messages) throws InterruptedException, ServiceBusException;    
    
    int getPrefetchCount();
    
    void setPrefetchCount(int prefetchCount) throws ServiceBusException;
}
