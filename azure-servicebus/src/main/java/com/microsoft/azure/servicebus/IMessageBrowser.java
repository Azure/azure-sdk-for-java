// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Represents a message browser that can browse messages from Azure Service Bus.
 */
public interface IMessageBrowser {
	
	IMessage peek() throws InterruptedException, ServiceBusException;

    IMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException;

    Collection<IMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException;

    Collection<IMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException;

    CompletableFuture<IMessage> peekAsync();

    CompletableFuture<IMessage> peekAsync(long fromSequenceNumber);

    CompletableFuture<Collection<IMessage>> peekBatchAsync(int messageCount);

    CompletableFuture<Collection<IMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount);
}
