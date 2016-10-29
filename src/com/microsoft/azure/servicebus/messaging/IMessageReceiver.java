package com.microsoft.azure.servicebus.messaging;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IMessageReceiver extends IMessageEntity{
	ReceiveMode getMode();

    void Abandon(UUID lockToken);

    void Abandon(UUID lockToken, Map<String, Object> propertiesToModify);
    
    CompletableFuture<Void> AbandonAsync(UUID lockToken);

    CompletableFuture<Void> AbandonAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void Complete(UUID lockToken);

    void CompleteBatch(Iterable<UUID> lockTokens);

    CompletableFuture<Void> CompleteAsync(UUID lockToken);

    CompletableFuture<Void> CompleteBatchAsync(Iterable<UUID> lockTokens);

    void Defer(UUID lockToken);

    void Defer(UUID lockToken, Map<String, Object> propertiesToModify);

    CompletableFuture<Void> DeferAsync(UUID lockToken);

    CompletableFuture<Void> DeferAsync(UUID lockToken, Map<String, Object> propertiesToModify);
    
    void DeadLetter(UUID lockToken);

    void DeadLetter(UUID lockToken, Map<String, Object> propertiesToModify);

    void DeadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription);

    CompletableFuture<Void> DeadLetterAsync(UUID lockToken);

    CompletableFuture<Void> DeadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    CompletableFuture<Void> DeadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription);

    BrokeredMessage Receive();
    
    BrokeredMessage Receive(Duration serverWaitTime);

    BrokeredMessage Receive(long sequenceNumber);

    Iterable<BrokeredMessage> ReceiveBatch(int messageCount);
    
    Iterable<BrokeredMessage> ReceiveBatch(int messageCount, Duration serverWaitTime);
    
    Iterable<BrokeredMessage> ReceiveBatch(Iterable<Long> sequenceNumbers);

    CompletableFuture<BrokeredMessage> ReceiveAsync();

    CompletableFuture<BrokeredMessage> ReceiveAsync(Duration serverWaitTime);

    CompletableFuture<BrokeredMessage> ReceiveAsync(long sequenceNumber);

    CompletableFuture<Iterable<BrokeredMessage>> ReceiveBatchAsync(int messageCount);

    CompletableFuture<Iterable<BrokeredMessage>> ReceiveBatchAsync(int messageCount, Duration serverWaitTime);

    CompletableFuture<Iterable<BrokeredMessage>> ReceiveBatchAsync(Iterable<Long> sequenceNumbers);
}
