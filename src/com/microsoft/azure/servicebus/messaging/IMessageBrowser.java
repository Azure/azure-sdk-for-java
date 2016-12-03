package com.microsoft.azure.servicebus.messaging;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface IMessageBrowser extends IMessageEntity {
	
	BrokeredMessage Peek();

    BrokeredMessage Peek(long fromSequenceNumber);

    Collection<BrokeredMessage> PeekBatch(int messageCount);

    Collection<BrokeredMessage> PeekBatch(long fromSequenceNumber, int messageCount);

    CompletableFuture<BrokeredMessage> PeekAsync();

    CompletableFuture<BrokeredMessage> PeekAsync(long fromSequenceNumber);

    CompletableFuture<Collection<BrokeredMessage>> PeekBatchAsync(int messageCount);

    CompletableFuture<Collection<BrokeredMessage>> PeekBatchAsync(long fromSequenceNumber, int messageCount);
}
