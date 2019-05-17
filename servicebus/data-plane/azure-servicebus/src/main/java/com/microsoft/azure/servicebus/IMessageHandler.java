// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
    CompletableFuture<Void> onMessageAsync(IMessage message);

    /**
     * Receiving the exceptions that passed by pump during message processing.
     *
     * @param exception Exception received in pump.
     * @param phase     Exception phase.
     */
    void notifyException(Throwable exception, ExceptionPhase phase);
}
