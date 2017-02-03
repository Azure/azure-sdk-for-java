package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface IMessageBrowser extends IMessageEntity {
	
	IBrokeredMessage Peek();

    IBrokeredMessage Peek(long fromSequenceNumber);

    Collection<IBrokeredMessage> PeekBatch(int messageCount);

    Collection<IBrokeredMessage> PeekBatch(long fromSequenceNumber, int messageCount);

    CompletableFuture<IBrokeredMessage> PeekAsync();

    CompletableFuture<IBrokeredMessage> PeekAsync(long fromSequenceNumber);

    CompletableFuture<Collection<IBrokeredMessage>> PeekBatchAsync(int messageCount);

    CompletableFuture<Collection<IBrokeredMessage>> PeekBatchAsync(long fromSequenceNumber, int messageCount);
}
