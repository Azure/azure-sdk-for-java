package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public interface IMessageSessionEntity {
//	int getSessionPrefetchCount();
//	
//	void setSessionPrefetchCount(int prefetchCount);
//
//    IMessageSession acceptMessageSession() throws InterruptedException, ServiceBusException;
//
//    IMessageSession acceptMessageSession(Duration serverWaitTime) throws InterruptedException, ServiceBusException;
//
//    IMessageSession acceptMessageSession(String sessionId) throws InterruptedException, ServiceBusException;
//
//    IMessageSession acceptMessageSession(String sessionId, Duration serverWaitTime) throws InterruptedException, ServiceBusException;
//
//    CompletableFuture<IMessageSession> acceptMessageSessionAsync();
//
//    CompletableFuture<IMessageSession> acceptMessageSessionAsync(Duration serverWaitTime);
//
//    CompletableFuture<IMessageSession> acceptMessageSessionAsync(String sessionId);
//
//    CompletableFuture<IMessageSession> acceptMessageSessionAsync(String sessionId, Duration serverWaitTime);
    
    Collection<IMessageSession> getMessageSessions() throws InterruptedException, ServiceBusException;

    Collection<IMessageSession> getMessageSessions(Instant lastUpdatedTime) throws InterruptedException, ServiceBusException;

    CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync();

    CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync(Instant lastUpdatedTime);
}
