package com.microsoft.azure.servicebus.messaging;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface IMessageSessionEntity {
	int getPrefetchCount();
	
	void setPrefetchCount(int prefetchCount);

    IMessageSession AcceptMessageSession();

    IMessageSession AcceptMessageSession(Duration serverWaitTime);

    IMessageSession AcceptMessageSession(String sessionId);

    IMessageSession AcceptMessageSession(String sessionId, Duration serverWaitTime);

    Iterable<IMessageSession> GetMessageSessions();

    Iterable<IMessageSession> GetMessageSessions(Instant lastUpdatedTime);

    CompletableFuture<IMessageSession> AcceptMessageSessionAsync();

    CompletableFuture<IMessageSession> AcceptMessageSessionAsync(Duration serverWaitTime);

    CompletableFuture<IMessageSession> AcceptMessageSessionAsync(String sessionId);

    CompletableFuture<IMessageSession> AcceptMessageSessionAsync(String sessionId, Duration serverWaitTime);

    CompletableFuture<Iterable<IMessageSession>> GetMessageSessionsAsync();

    CompletableFuture<Iterable<IMessageSession>> GetMessageSessionsAsync(Instant lastUpdatedTime);
}
