package com.microsoft.azure.servicebus.messaging;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.ServiceBusException;

public interface IMessageSender extends IMessageEntity{
	void send(BrokeredMessage message) throws InterruptedException, ServiceBusException;

    void sendBatch(Collection<BrokeredMessage> message) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> sendAsync(BrokeredMessage message);

    CompletableFuture<Void> sendBatchAsync(Collection<BrokeredMessage> message);
}
