// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

/**
 * Defines the contract for registering the callback {@link QueueClient#registerMessageHandler(IMessageHandler)} and {@link SubscriptionClient#registerMessageHandler(IMessageHandler)} for {@link QueueClient} and {@link SubscriptionClient}.
 */
public interface IMessageHandler {

    /**
     * The callback for message pump to pass received {@link Message}s.
     *
     * @param message The received {@link Message}.
     * @return CompletableFuture for the message handler.
     */
    public CompletableFuture<Void> onMessageAsync(IMessage message);

    /**
     * Receiving the exceptions that passed by pump during message processing.
     *
     * @param exception Exception received in pump.
     * @param phase     Exception phase.
     */
    public void notifyException(Throwable exception, ExceptionPhase phase);
}
