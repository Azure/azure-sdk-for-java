package com.microsoft.azure.servicebus.messaging;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.ServiceBusException;

public interface IMessageSender extends IMessageEntity{
	void send(BrokeredMessage message) throws InterruptedException, ServiceBusException;

    void sendBatch(Iterable<BrokeredMessage> message) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> sendAsync(BrokeredMessage message);

    CompletableFuture<Void> sendBatchAsync(Iterable<BrokeredMessage> message);
}
