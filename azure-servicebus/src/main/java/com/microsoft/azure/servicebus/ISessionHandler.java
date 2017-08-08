// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

/**
 * Defines the contract for registering the session message processing callback {@link QueueClient#registerSessionHandler(ISessionHandler)} or {@link SubscriptionClient#registerSessionHandler(ISessionHandler)} for {@link QueueClient} and {@link SubscriptionClient}.
 */
public interface ISessionHandler {

    /**
     * The callback for message pump to pass received {@link Message}s.
     *
     * @param session The {@link MessageSession} of the message.
     * @param message The received {@link Message}.
     * @return CompletableFuture for the message handler.
     */
    public CompletableFuture<Void> onMessageAsync(IMessageSession session, IMessage message);

    /**
     * Called just before a session is closed by the session pump
     *
     * @param session session being closed
     * @return a future that executes the action
     */
    public CompletableFuture<Void> OnCloseSessionAsync(IMessageSession session);

    /**
     * Receiving the exceptions that passed by pump during message processing.
     *
     * @param exception Exception received in pump.
     * @param phase     Exception phase.
     */
    public void notifyException(Throwable exception, ExceptionPhase phase);
}
