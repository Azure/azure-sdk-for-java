package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface IMessageSender extends IMessageEntity{
	void send(IBrokeredMessage message) throws InterruptedException, ServiceBusException;

    void sendBatch(Collection<? extends IBrokeredMessage> message) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> sendAsync(IBrokeredMessage message);

    CompletableFuture<Void> sendBatchAsync(Collection<? extends IBrokeredMessage> message);
    
    CompletableFuture<Long> scheduleMessageAsync(IBrokeredMessage message, Instant scheduledEnqueueTimeUtc);
    
    CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber);
    
    long scheduleMessage(IBrokeredMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException;
    
    void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException;
}
