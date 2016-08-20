package com.microsoft.azure.servicebus.messaging;

import java.util.concurrent.CompletableFuture;

public interface IMessageSender {
	void Send(BrokeredMessage message);

    void SendBatch(Iterable<BrokeredMessage> message);

    CompletableFuture<Void> SendAsync(BrokeredMessage message);

    CompletableFuture<Void> SendBatchAsync(Iterable<BrokeredMessage> message);
}
