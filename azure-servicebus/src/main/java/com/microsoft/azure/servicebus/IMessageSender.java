package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface IMessageSender extends IMessageEntity{
	void send(IMessage message) throws InterruptedException, ServiceBusException;

    void sendBatch(Collection<? extends IMessage> message) throws InterruptedException, ServiceBusException;

    CompletableFuture<Void> sendAsync(IMessage message);

    CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> message);
    
    CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc);
    
    CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber);
    
    long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException;
    
    void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException;
}
