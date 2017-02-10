package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface IMessageBrowser extends IMessageEntity {
	
	IBrokeredMessage peek() throws InterruptedException, ServiceBusException;

    IBrokeredMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException;

    Collection<IBrokeredMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException;

    Collection<IBrokeredMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException;

    CompletableFuture<IBrokeredMessage> peekAsync();

    CompletableFuture<IBrokeredMessage> peekAsync(long fromSequenceNumber);

    CompletableFuture<Collection<IBrokeredMessage>> peekBatchAsync(int messageCount);

    CompletableFuture<Collection<IBrokeredMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount);
}
