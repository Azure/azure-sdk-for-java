// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Defines message receiver interface. The MessageReceiver can be used to receive messages from Queues and Subscriptions and acknowledge them.
 */
public interface IMessageReceiver extends IMessageEntityClient, IMessageBrowser {

    /**
     * Get current receiver's {@link ReceiveMode}.
     *
     * @return {@link ReceiveMode}
     */
    ReceiveMode getReceiveMode();

    /**
     * Abandon {@link Message} with lock token. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void abandon(UUID lockToken) throws InterruptedException, ServiceBusException;

    /**
     * Abandon {@link Message} with lock token and updated message property. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void abandon(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously abandon {@link Message} with lock token. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @return a CompletableFuture representing the pending abandon.
     */
    CompletableFuture<Void> abandonAsync(UUID lockToken);

    /**
     * Asynchronously abandon {@link Message} with lock token and updated message property. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @return a CompletableFuture representing the pending abandon.
     */
    CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    /**
     * Completes a {@link Message} using its lock token. This will delete the message from the service.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void complete(UUID lockToken) throws InterruptedException, ServiceBusException;

    //void completeBatch(Collection<? extends IMessage> messages);

    /**
     * Asynchronously completes a {@link Message} using its lock token. This will delete the message from the service.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @return a CompletableFuture representing the pending complete.
     */
    CompletableFuture<Void> completeAsync(UUID lockToken);

    // CompletableFuture<Void> completeBatchAsync(Collection<? extends IMessage> messages);

    /**
     * Defers a {@link Message} using its lock token. This will move message into deferred subqueue.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if defer failed
     */
    void defer(UUID lockToken) throws InterruptedException, ServiceBusException;

    /**
     * Defers a {@link Message} using its lock token with modified message property. This will move message into deferred subqueue.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if defer failed
     */
    void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously defers a {@link Message} using its lock token. This will move message into deferred subqueue.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @return a CompletableFuture representing the pending defer.
     */
    CompletableFuture<Void> deferAsync(UUID lockToken);

    /**
     * Asynchronously defers a {@link Message} using its lock token with modified message propert. This will move message into deferred subqueue.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @return a CompletableFuture representing the pending defer.
     */
    CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    /**
     * Moves a {@link Message} to the deadletter sub-queue.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException;

    /**
     * Moves a {@link Message} to the deadletter sub-queue with modified message properties.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    /**
     * Moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription) throws InterruptedException, ServiceBusException;

    /**
     * Moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description and modified properties.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param propertiesToModify         Message properties to modify.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with deadletter.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken);

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with modified properties.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription);

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description and modified properties.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param propertiesToModify         Message properties to modify.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify);

    /**
     * Receives a {@link Message} with default server wait time.
     *
     * @return The received {@link Message} or null if there is no message.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if receive failed
     */
    IMessage receive() throws InterruptedException, ServiceBusException;

    /**
     * Receives a {@link Message} with specified server wait time.
     *
     * @param serverWaitTime The server wait time
     * @return The received {@link Message} or null if there is no message.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if receive failed
     */
    IMessage receive(Duration serverWaitTime) throws InterruptedException, ServiceBusException;

    /**
     * Receives a deferred {@link Message}. Deferred messages can only be received by using sequence number.
     *
     * @param sequenceNumber The {@link Message#getSequenceNumber()}.
     * @return The received {@link Message} or null if there is no message for given sequence number.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if receive failed
     */
    IMessage receiveDeferredMessage(long sequenceNumber) throws InterruptedException, ServiceBusException;

    /**
     * Receives a maximum of  maxMessageCount {@link Message} from Azure Service Bus.
     *
     * @param maxMessageCount The maximum number of messages that will be received.
     * @return List of messages received. Returns null if no message is found.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if receive failed
     */
    Collection<IMessage> receiveBatch(int maxMessageCount) throws InterruptedException, ServiceBusException;

    /**
     * Receives a maximum of  maxMessageCount {@link Message} from Azure Service Bus with server wait time.
     *
     * @param maxMessageCount The maximum number of messages that will be received.
     * @param serverWaitTime  The time the client waits for receiving a message before it times out.
     * @return List of messages received. Returns null if no message is found.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if receive failed
     */
    Collection<IMessage> receiveBatch(int maxMessageCount, Duration serverWaitTime) throws InterruptedException, ServiceBusException;

    /**
     * Receives a batch of deferred {@link Message}.
     *
     * @param sequenceNumbers The sequence numbers of desired deferred messages.
     * @return List of messages received. Returns null if no message is found.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if receive failed
     */
    Collection<IMessage> receiveDeferredMessageBatch(Collection<Long> sequenceNumbers) throws InterruptedException, ServiceBusException;

    /**
     * Receives a {@link Message} from Azure Service Bus.
     *
     * @return The message received. Returns null if no message is found
     */
    CompletableFuture<IMessage> receiveAsync();

    /**
     * Receives a {@link Message} from Azure Service Bus with server wait time.
     *
     * @param serverWaitTime The time the client waits for receiving a message before it times out.
     * @return The message received. Returns null if no message is found
     */
    CompletableFuture<IMessage> receiveAsync(Duration serverWaitTime);

    /**
     * Asynchronously receives a specific deferred {@link Message} identified by sequence number.
     *
     * @param sequenceNumber The sequence number of the message that will be received.
     * @return a CompletableFuture representing the pending receive.
     */
    CompletableFuture<IMessage> receiveDeferredMessageAsync(long sequenceNumber);

    /**
     * Asynchronously receives a maximum of maxMessageCount {@link Message} from the entity.
     *
     * @param maxMessageCount The maximum number of messages that will be received.
     * @return a CompletableFuture representing the pending receive.
     */
    CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount);

    /**
     * Asynchronously receives a maximum of  maxMessageCount {@link Message} from Azure Service Bus with server wait time.
     *
     * @param maxMessageCount The maximum number of messages that will be received.
     * @param serverWaitTime  The time the client waits for receiving a message before it times out.
     * @return a CompletableFuture representing the pending receive.
     */
    CompletableFuture<Collection<IMessage>> receiveBatchAsync(int maxMessageCount, Duration serverWaitTime);

    /**
     * Asynchronously receives a set of deferred {@link Message} from the entity.
     *
     * @param sequenceNumbers The sequence numbers of the message that will be received.
     * @return a CompletableFuture representing the pending receive.
     */
    CompletableFuture<Collection<IMessage>> receiveDeferredMessageBatchAsync(Collection<Long> sequenceNumbers);

    /**
     * Asynchronously renews the lock on the message specified by the lock token. The lock will be renewed based on the setting specified on the entity.
     *
     * @param message The {@link Message} to be renewed
     * @return a CompletableFuture representing the pending renew.
     */
    CompletableFuture<Instant> renewMessageLockAsync(IMessage message);

    //CompletableFuture<Collection<Instant>> renewMessageLockBatchAsync(Collection<? extends IBrokeredMessage> messages);

    /**
     * Renews the lock on the message specified by the lock token. The lock will be renewed based on the setting specified on the entity.
     * When a message is received in {@link ReceiveMode#PEEKLOCK} mode, the message is locked on the server for this
     * receiver instance for a duration as specified during the Queue/Subscription creation (LockDuration).
     * If processing of the message requires longer than this duration, the lock needs to be renewed. For each renewal, the lock is renewed by
     * the entity's LockDuration.
     *
     * @param message The {@link Message} to be renewed
     * @return The new locked until UTC time.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if renew failed
     */
    Instant renewMessageLock(IMessage message) throws InterruptedException, ServiceBusException;

    //Collection<Instant> renewMessageLockBatch(Collection<? extends IBrokeredMessage> messages) throws InterruptedException, ServiceBusException;    

    /**
     * Get the prefetch value set.
     *
     * @return The set prefetch count value.
     */
    int getPrefetchCount();

    /**
     * Set the prefetch count of the receiver. Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and before the application asks for one using Receive.
     * Setting a non-zero value prefetches PrefetchCount number of messages.
     * Setting the value to zero turns prefetch off. For RECEIVEANDDELETE mode, the default value is 0. For PEEKLOCK mode, the default value is 100.
     * <p>
     * The value cannot be set until the receiver is created.
     *
     * @param prefetchCount The desired prefetch count.
     * @throws ServiceBusException if sets the value failed
     */
    void setPrefetchCount(int prefetchCount) throws ServiceBusException;
}
