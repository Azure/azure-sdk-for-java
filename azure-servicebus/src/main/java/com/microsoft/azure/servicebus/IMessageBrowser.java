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

    /**
     * reads next the active message without changing the state of the receiver or the message source.
     * The first call to {@link IMessageBrowser#peek()} fetches the first active message for this receiver.
     * Each subsequent call fetches the subsequent message in the entity.
     *
     * @return {@link Message} peeked
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if peek failed
     */
    IMessage peek() throws InterruptedException, ServiceBusException;

    /**
     * Reads next the active message without changing the state of the receiver or the message source.
     *
     * @param fromSequenceNumber The sequence number from where to read the message.
     * @return {@link Message} peeked
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if peek failed
     */
    IMessage peek(long fromSequenceNumber) throws InterruptedException, ServiceBusException;

    /**
     * Reads next batch of the active messages without changing the state of the receiver or the message source.
     *
     * @param messageCount The number of messages.
     * @return Batch of {@link Message} peeked
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if peek failed
     */
    Collection<IMessage> peekBatch(int messageCount) throws InterruptedException, ServiceBusException;

    /**
     * Reads next batch of the active messages without changing the state of the receiver or the message source.
     *
     * @param fromSequenceNumber The sequence number from where to read the message.
     * @param messageCount       The number of messages.
     * @return Batch of {@link Message} peeked
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if peek failed
     */
    Collection<IMessage> peekBatch(long fromSequenceNumber, int messageCount) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously reads the active messages without changing the state of the receiver or the message source.
     *
     * @return {@link Message} peeked
     */
    CompletableFuture<IMessage> peekAsync();

    /**
     * Asynchronously reads next the active message without changing the state of the receiver or the message source.
     *
     * @param fromSequenceNumber The sequence number from where to read the message.
     * @return CompletableFuture that returns {@link Message} peeked.
     */
    CompletableFuture<IMessage> peekAsync(long fromSequenceNumber);

    /**
     * Asynchronously reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param messageCount The number of messages.
     * @return CompletableFuture that returns batch of {@link Message} peeked.
     */
    CompletableFuture<Collection<IMessage>> peekBatchAsync(int messageCount);

    /**
     * Asynchronously reads the next batch of active messages without changing the state of the receiver or the message source.
     *
     * @param fromSequenceNumber The sequence number from where to read the message.
     * @param messageCount       The number of messages.
     * @return CompletableFuture that returns batch of {@link Message} peeked.
     */
    CompletableFuture<Collection<IMessage>> peekBatchAsync(long fromSequenceNumber, int messageCount);
}
