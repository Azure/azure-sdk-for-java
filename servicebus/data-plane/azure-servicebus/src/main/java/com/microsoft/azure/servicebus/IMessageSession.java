// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Describes a Session object. IMessageSession can be used to perform operations on sessions.
 * <p>
 * Service Bus Sessions, also called 'Groups' in the AMQP 1.0 protocol, are unbounded sequences of related messages. ServiceBus guarantees ordering of messages in a session.
 * <p>
 * Any sender can create a session when submitting messages into a Topic or Queue by setting the {@link Message#sessionId} property on Message to some application defined unique identifier. At the AMQP 1.0 protocol level, this value maps to the group-id property.
 * <p>
 * Sessions come into existence when there is at least one message with the session's SessionId in the Queue or Topic subscription. Once a Session exists, there is no defined moment or gesture for when the session expires or disappears.
 */
public interface IMessageSession extends IMessageReceiver {

    /**
     * @return Gets the SessionId.
     */
    String getSessionId();

    /**
     * @return Gets the time that the session identified by {@link IMessageSession#getSessionId()} is locked until for this client.
     */
    Instant getLockedUntilUtc();

    /**
     * Renews the lock on the session specified by the {@link IMessageSession#getSessionId()}. The lock will be renewed based on the setting specified on the entity.
     * <p>
     * When you accept a session, the session is locked for this client instance by the service for a duration as specified during the Queue/Subscription creation.
     * If processing of the session requires longer than this duration, the session-lock needs to be renewed. For each renewal, the session-lock is renewed by
     * the entity's LockDuration.
     * <p>
     * Renewal of session renews all the messages in the session as well. Each individual message need not be renewed.
     *
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the renew failed.
     */
    void renewSessionLock() throws InterruptedException, ServiceBusException;

    /**
     * Renews the lock on the session specified by the {@link IMessageSession#getSessionId()}. The lock will be renewed based on the setting specified on the entity.
     *
     * @return a CompletableFuture representing the pending renew.
     * @see IMessageSession#renewSessionLock()
     */
    CompletableFuture<Void> renewSessionLockAsync();

    /**
     * Set a custom state on the session which can be later retrieved using {@link IMessageSession#getState()}.
     *
     * @param state The session state.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the set state failed.
     */
    void setState(byte[] state) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously set a custom state on the session which can be later retrieved using {@link IMessageSession#getState()}.
     *
     * @param state The session state.
     * @return a CompletableFuture representing the pending session state setting.
     * @see IMessageSession#setState
     */
    CompletableFuture<Void> setStateAsync(byte[] state);

    /**
     * Gets the session state.
     *
     * @return The session state
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if get state failed.
     */
    byte[] getState() throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously gets the session state.
     * @return a CompletableFuture representing the pending session state retrieving.
     * @see IMessageSession#getState
     */
    CompletableFuture<byte[]> getStateAsync();
}
