package com.microsoft.azure.servicebus.messaging;

import java.util.concurrent.CompletableFuture;

public interface IMessageBrowser extends IMessageEntity {
	
	BrokeredMessage Peek();

    BrokeredMessage Peek(long fromSequenceNumber);

    Iterable<BrokeredMessage> PeekBatch(int messageCount);

    Iterable<BrokeredMessage> PeekBatch(long fromSequenceNumber, int messageCount);

    CompletableFuture<BrokeredMessage> PeekAsync();

    CompletableFuture<BrokeredMessage> PeekAsync(long fromSequenceNumber);

    CompletableFuture<Iterable<BrokeredMessage>> PeekBatchAsync(int messageCount);

    CompletableFuture<Iterable<BrokeredMessage>> PeekBatchAsync(long fromSequenceNumber, int messageCount);
}
