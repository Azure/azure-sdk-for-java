package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface IMessageBrowser {
	
	IMessage peek() throws InterruptedException, ServiceBusException;

    IMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException;

    Collection<IMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException;

    Collection<IMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException;

    CompletableFuture<IMessage> peekAsync();

    CompletableFuture<IMessage> peekAsync(long fromSequenceNumber);

    CompletableFuture<Collection<IMessage>> peekBatchAsync(int messageCount);

    CompletableFuture<Collection<IMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount);
}
