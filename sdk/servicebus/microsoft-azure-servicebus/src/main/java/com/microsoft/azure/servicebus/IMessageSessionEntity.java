// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Represents a session full client entity.
 */
public interface IMessageSessionEntity {
//    int getSessionPrefetchCount();
//
//    void setSessionPrefetchCount(int prefetchCount);
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

    /**
     * Gets the message sessions, enabling you to browse sessions on queues.
     * Only sessions with active messages in the queue are returned.
     * The sessions on the deadletter queue or sessions having a SessionState as 'active' and no messages are not returned with this call.
     *
     * @return A collection of sessions.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     * @throws ServiceBusException  if get sessions failed.
     */
    Collection<IMessageSession> getMessageSessions() throws InterruptedException, ServiceBusException;

    /**
     * Retrieves all message sessions whose session state was updated since lastUpdatedTime.
     * Only sessions with active messages in the queue are returned.
     * The sessions on the deadletter queue or sessions having a SessionState as 'active' and no messages are not returned with this call.
     *
     * @param lastUpdatedTime The time the session was last updated.
     * @return A collection of sessions.
     * @throws InterruptedException if the current thread was interrupted while waiting.
     * @throws ServiceBusException  if get sessions failed.
     */
    Collection<IMessageSession> getMessageSessions(Instant lastUpdatedTime) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously gets the message sessions, enabling you to browse sessions on queues.
     * Only sessions with active messages in the queue are returned.
     * The sessions on the deadletter queue or sessions having a SessionState as 'active' and no messages are not returned with this call.
     *
     * @return a CompletableFuture representing the pending operation to get sessions.
     * @see IMessageSessionEntity#getMessageSessions
     */
    CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync();

    /**
     * Asynchronously retrieves all message sessions whose session state was updated since lastUpdatedTime.
     * Only sessions with active messages in the queue are returned.
     * The sessions on the deadletter queue or sessions having a SessionState as 'active' and no messages are not returned with this call.
     *
     * @param lastUpdatedTime The time the session was last updated.
     * @return a CompletableFuture representing the pending operation to get sessions.
     * @see IMessageSessionEntity#getMessageSessions(Instant)
     */
    CompletableFuture<Collection<IMessageSession>> getMessageSessionsAsync(Instant lastUpdatedTime);
}
