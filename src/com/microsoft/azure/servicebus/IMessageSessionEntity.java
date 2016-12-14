package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface IMessageSessionEntity {
	int getPrefetchCount();
	
	void setPrefetchCount(int prefetchCount);

    IMessageSession acceptMessageSession();

    IMessageSession acceptMessageSession(Duration serverWaitTime);

    IMessageSession acceptMessageSession(String sessionId);

    IMessageSession acceptMessageSession(String sessionId, Duration serverWaitTime);

    Iterable<IMessageSession> getMessageSessions();

    Iterable<IMessageSession> getMessageSessions(Instant lastUpdatedTime);

    CompletableFuture<IMessageSession> acceptMessageSessionAsync();

    CompletableFuture<IMessageSession> acceptMessageSessionAsync(Duration serverWaitTime);

    CompletableFuture<IMessageSession> acceptMessageSessionAsync(String sessionId);

    CompletableFuture<IMessageSession> acceptMessageSessionAsync(String sessionId, Duration serverWaitTime);

    CompletableFuture<Iterable<IMessageSession>> getMessageSessionsAsync();

    CompletableFuture<Iterable<IMessageSession>> getMessageSessionsAsync(Instant lastUpdatedTime);
}
