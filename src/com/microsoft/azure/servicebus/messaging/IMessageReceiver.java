package com.microsoft.azure.servicebus.messaging;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.ServiceBusException;

public interface IMessageReceiver extends IMessageEntity{
	ReceiveMode getReceiveMode();

    void abandon(UUID lockToken);

    void abandon(UUID lockToken, Map<String, Object> propertiesToModify);
    
    CompletableFuture<Void> abandonAsync(UUID lockToken);

    CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void complete(UUID lockToken);

    void completeBatch(Collection<UUID> lockTokens);

    CompletableFuture<Void> completeAsync(UUID lockToken);

    CompletableFuture<Void> completeBatchAsync(Collection<UUID> lockTokens);

    void defer(UUID lockToken);

    void defer(UUID lockToken, Map<String, Object> propertiesToModify);

    CompletableFuture<Void> deferAsync(UUID lockToken);

    CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void deadLetter(UUID lockToken);

    void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify);

    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription);

    CompletableFuture<Void> deadLetterAsync(UUID lockToken);

    CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription);

    BrokeredMessage receive() throws InterruptedException, ServiceBusException;
    
    BrokeredMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException;

    BrokeredMessage receive(long sequenceNumber) throws InterruptedException, ServiceBusException;

    Collection<BrokeredMessage> receiveBatch(int messageCount) throws InterruptedException, ServiceBusException;
    
    Collection<BrokeredMessage> receiveBatch(int messageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException;
    
    Collection<BrokeredMessage> receiveBatch(Collection<Long> sequenceNumbers) throws InterruptedException, ServiceBusException;

    CompletableFuture<BrokeredMessage> receiveAsync();

    CompletableFuture<BrokeredMessage> receiveAsync(Duration serverWaitTime);

    CompletableFuture<BrokeredMessage> receiveAsync(long sequenceNumber);

    CompletableFuture<Collection<BrokeredMessage>> receiveBatchAsync(int messageCount);

    CompletableFuture<Collection<BrokeredMessage>> receiveBatchAsync(int messageCount, Duration serverWaitTime);

    CompletableFuture<Collection<BrokeredMessage>> receiveBatchAsync(Collection<Long> sequenceNumbers);
    
    int getPrefetchCount();
    
    void setPrefetchCount(int prefetchCount) throws ServiceBusException;
}
