// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.time.Instant;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Represents a message sender that sends messages to Azure Service Bus.
 *
 * @since 1.0
 */
public interface IMessageSender extends IMessageEntityClient {
    /**
     * Sends a message to the Azure Service Bus entity this sender is connected to. This method blocks until the message is sent to the entity. Calling this method is equivalent to calling
     * <code>sendAsync(message).get()</code>. For better performance, use async methods.
     *
     * @param message message to be sent to the entity
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if message couldn't be sent to the entity
     */
    void send(IMessage message) throws InterruptedException, ServiceBusException;

    /**
     * Sends a message to the Azure Service Bus entity this sender is connected to. This method blocks until the message is sent to the entity. Calling this method is equivalent to calling
     * <code>sendAsync(message).get()</code>. For better performance, use async methods.
     *
     * @param message message to be sent to the entity
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if message couldn't be sent to the entity
     */
    void send(IMessage message, TransactionContext transaction) throws InterruptedException, ServiceBusException;

    /**
     * Sends a batch of messages to the Azure Service Bus entity this sender is connected to. This method blocks until the batch is sent to the entity. Calling this method is equivalent to calling
     * <code>sendBatchAsync(messages).get()</code>. For better performance, use async methods.
     *
     * @param messages collection of messages to be sent to the entity
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the batch couldn't be sent to the entity
     */
    void sendBatch(Collection<? extends IMessage> messages) throws InterruptedException, ServiceBusException;

    /**
     * Sends a batch of messages to the Azure Service Bus entity this sender is connected to. This method blocks until the batch is sent to the entity. Calling this method is equivalent to calling
     * <code>sendBatchAsync(messages).get()</code>. For better performance, use async methods.
     *
     * @param messages collection of messages to be sent to the entity
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the batch couldn't be sent to the entity
     */
    void sendBatch(Collection<? extends IMessage> messages, TransactionContext transaction) throws InterruptedException, ServiceBusException;

    /**
     * Sends a message to the Azure Service Bus entity this sender is connected to. This is an asynchronous method returning a CompletableFuture which completes when the message is sent to the entity.
     *
     * @param message message to be sent to the entity
     * @return a CompletableFuture representing the pending send
     */
    CompletableFuture<Void> sendAsync(IMessage message);

    /**
     * Sends a message to the Azure Service Bus entity this sender is connected to. This is an asynchronous method returning a CompletableFuture which completes when the message is sent to the entity.
     *
     * @param message message to be sent to the entity
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending send
     */
    CompletableFuture<Void> sendAsync(IMessage message, TransactionContext transaction);

    /**
     * Sends a batch of messages to the Azure Service Bus entity this sender is connected to. This is an asynchronous method returning a CompletableFuture which completes when the batch is sent to the entity.
     *
     * @param messages collection of messages to be sent to the entity
     * @return a CompletableFuture representing the pending send
     */
    CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> messages);

    /**
     * Sends a batch of messages to the Azure Service Bus entity this sender is connected to. This is an asynchronous method returning a CompletableFuture which completes when the batch is sent to the entity.
     *
     * @param messages collection of messages to be sent to the entity
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending send
     */
    CompletableFuture<Void> sendBatchAsync(Collection<? extends IMessage> messages, TransactionContext transaction);

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is enqueued and made available to receivers only at the scheduled enqueue time.
     * This is an asynchronous method returning a CompletableFuture which completes when the message is sent to the entity. The CompletableFuture, on completion, returns the sequence number of the scheduled message
     * which can be used to cancel the scheduling of the message.
     *
     * @param message                 message to be sent to the entity
     * @param scheduledEnqueueTimeUtc instant at which the message should be enqueued in the entity
     * @return a CompletableFuture representing the pending send, which returns the sequence number of the scheduled message. This sequence number can be used to cancel the scheduling of the message.
     */
    CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc);

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is enqueued and made available to receivers only at the scheduled enqueue time.
     * This is an asynchronous method returning a CompletableFuture which completes when the message is sent to the entity. The CompletableFuture, on completion, returns the sequence number of the scheduled message
     * which can be used to cancel the scheduling of the message.
     *
     * @param message                 message to be sent to the entity
     * @param scheduledEnqueueTimeUtc instant at which the message should be enqueued in the entity
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending send, which returns the sequence number of the scheduled message. This sequence number can be used to cancel the scheduling of the message.
     */
    CompletableFuture<Long> scheduleMessageAsync(IMessage message, Instant scheduledEnqueueTimeUtc, TransactionContext transaction);

    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued. This is an asynchronous method returning a CompletableFuture which completes when the message is cancelled.
     *
     * @param sequenceNumber sequence number of the scheduled message
     * @return a CompletableFuture representing the pending cancellation
     */
    CompletableFuture<Void> cancelScheduledMessageAsync(long sequenceNumber);

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is enqueued and made available to receivers only at the scheduled enqueue time.
     * This method blocks until the message is sent to the entity. Calling this method is equivalent to calling <code>scheduleMessageAsync(message, scheduledEnqueueTimeUtc).get()</code>. For better performance, use async methods.
     *
     * @param message                 message to be sent to the entity
     * @param scheduledEnqueueTimeUtc instant at which the message should be enqueued in the entity
     * @return sequence number of the scheduled message
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if message couldn't be sent to the entity
     */
    long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc) throws InterruptedException, ServiceBusException;

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is enqueued and made available to receivers only at the scheduled enqueue time.
     * This method blocks until the message is sent to the entity. Calling this method is equivalent to calling <code>scheduleMessageAsync(message, scheduledEnqueueTimeUtc).get()</code>. For better performance, use async methods.
     *
     * @param message                 message to be sent to the entity
     * @param scheduledEnqueueTimeUtc instant at which the message should be enqueued in the entity
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return sequence number of the scheduled message
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if message couldn't be sent to the entity
     */
    long scheduleMessage(IMessage message, Instant scheduledEnqueueTimeUtc, TransactionContext transaction) throws InterruptedException, ServiceBusException;

    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued. This method blocks until the message is sent to the entity. Calling this method is equivalent to calling <code>cancelScheduledMessageAsync(sequenceNumber).get()</code>.
     * For better performance, use async methods.
     *
     * @param sequenceNumber sequence number of the scheduled message
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if scheduled message couldn't be cancelled
     */
    void cancelScheduledMessage(long sequenceNumber) throws InterruptedException, ServiceBusException;
}
