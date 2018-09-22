// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.jar.JarException;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Represents the pump which is underneath the clients that handles message processing.
 */
interface IMessageAndSessionPump {

    /**
     * Receive messages continuously from the entity. Registers a message handler and begins a new thread to receive messages.
     * IMessageHandler methods are executed on java.util.concurrent.commonPool()
     *
     * @param handler The {@link IMessageHandler} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     * @deprecated Use {@link #registerMessageHandler(IMessageHandler, ExecutorService)}
     */
	@Deprecated
    public void registerMessageHandler(IMessageHandler handler) throws InterruptedException, ServiceBusException;
    
    /**
     * Receive messages continuously from the entity. Registers a message handler and begins a new thread to receive messages.
     * IMessageHandler methods are executed on the passed executor service.
     *
     * @param handler The {@link IMessageHandler} instance
     * @param executorService ExecutorService which is used to execute {@link IMessageHandler} methods. If there are 
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     */
    public void registerMessageHandler(IMessageHandler handler, ExecutorService executorService) throws InterruptedException, ServiceBusException;

    /**
     * Receive messages continuously from the entity. Registers a message handler and begins a new thread to receive messages.
     * IMessageHandler methods are executed on java.util.concurrent.commonPool()
     *
     * @param handler        The {@link IMessageHandler} instance
     * @param handlerOptions {@link MessageHandlerOptions}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     */
    @Deprecated
    public void registerMessageHandler(IMessageHandler handler, MessageHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException;
    
    /**
     * Receive messages continuously from the entity. Registers a message handler and begins a new thread to receive messages.
     * IMessageHandler methods are executed on the passed executor service.
     *
     * @param handler        The {@link IMessageHandler} instance
     * @param handlerOptions {@link MessageHandlerOptions}
     * @param executorService ExecutorService which is used to execute {@link IMessageHandler} methods
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     */
    public void registerMessageHandler(IMessageHandler handler, MessageHandlerOptions handlerOptions, ExecutorService executorService) throws InterruptedException, ServiceBusException;

    /**
     * Receive session messages continuously from the queue. Registers a message handler and begins a new thread to receive session-messages.
     * ISessionHandler methods are executed on java.util.concurrent.commonPool()
     * 
     * @param handler The {@link ISessionHandler} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     */
    @Deprecated
    public void registerSessionHandler(ISessionHandler handler) throws InterruptedException, ServiceBusException;
    
    /**
     * Receive session messages continuously from the queue. Registers a message handler and begins a new thread to receive session-messages.
     * ISessionHandler methods are executed on the passed executor service.
     *
     * @param handler The {@link ISessionHandler} instance
     * @param executorService ExecutorService which is used to execute {@link ISessionHandler} methods
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     */
    public void registerSessionHandler(ISessionHandler handler, ExecutorService executorService) throws InterruptedException, ServiceBusException;

    /**
     * Receive session messages continuously from the queue. Registers a message handler and begins a new thread to receive session-messages.
     * ISessionHandler methods are executed on java.util.concurrent.commonPool()
     *
     * @param handler        The {@link ISessionHandler} instance
     * @param handlerOptions {@link SessionHandlerOptions}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     */
    @Deprecated
    public void registerSessionHandler(ISessionHandler handler, SessionHandlerOptions handlerOptions) throws InterruptedException, ServiceBusException;
    
    /**
     * Receive session messages continuously from the queue. Registers a message handler and begins a new thread to receive session-messages.
     * ISessionHandler methods are executed on the passed executor service.
     *
     * @param handler        The {@link ISessionHandler} instance
     * @param handlerOptions {@link SessionHandlerOptions}
     * @param executorService ExecutorService which is used to execute {@link ISessionHandler} methods
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if register failed
     */
    public void registerSessionHandler(ISessionHandler handler, SessionHandlerOptions handlerOptions, ExecutorService executorService) throws InterruptedException, ServiceBusException;

    /**
     * Abandon {@link Message} with lock token. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void abandon(UUID lockToken) throws InterruptedException, ServiceBusException;

    /**
     * Abandon {@link Message} with lock token. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void abandon(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException;

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
     * Abandon {@link Message} with lock token and updated message property. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void abandon(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously abandon {@link Message} with lock token. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @return a CompletableFuture representing the pending abandon.
     */
    CompletableFuture<Void> abandonAsync(UUID lockToken);

    /**
     * Asynchronously abandon {@link Message} with lock token. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending abandon.
     */
    CompletableFuture<Void> abandonAsync(UUID lockToken, TransactionContext transaction);

    /**
     * Asynchronously abandon {@link Message} with lock token and updated message property. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @return a CompletableFuture representing the pending abandon.
     */
    CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    /**
     * Asynchronously abandon {@link Message} with lock token and updated message property. This will make the message available again for processing. Abandoning a message will increase the delivery count on the message.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending abandon.
     */
    CompletableFuture<Void> abandonAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction);

    /**
     * Completes a {@link Message} using its lock token. This will delete the message from the service.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void complete(UUID lockToken) throws InterruptedException, ServiceBusException;

    /**
     * Completes a {@link Message} using its lock token. This will delete the message from the service.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if abandon failed
     */
    void complete(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously completes a {@link Message} using its lock token. This will delete the message from the service.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @return a CompletableFuture representing the pending complete.
     */
    CompletableFuture<Void> completeAsync(UUID lockToken);

    /**
     * Asynchronously completes a {@link Message} using its lock token. This will delete the message from the service.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending complete.
     */
    CompletableFuture<Void> completeAsync(UUID lockToken, TransactionContext transaction);

//    void defer(UUID lockToken) throws InterruptedException, ServiceBusException;
//
//    void defer(UUID lockToken, Map<String, Object> propertiesToModify) throws InterruptedException, ServiceBusException;
//
//    /**
//     * Asynchronously defers a {@link Message} using its lock token. This will move message into deferred subqueue.
//     *
//     * @param lockToken Message lock token {@link Message#getLockToken()}
//     * @return a CompletableFuture representing the pending defer.
//     */
//    CompletableFuture<Void> deferAsync(UUID lockToken);
//
//    /**
//     * Asynchronously defers a {@link Message} using its lock token with modified message propert. This will move message into deferred subqueue.
//     *
//     * @param lockToken          Message lock token {@link Message#getLockToken()}
//     * @param propertiesToModify Message properties to modify.
//     * @return a CompletableFuture representing the pending defer.
//     */
//    CompletableFuture<Void> deferAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    /**
     * Moves a {@link Message} to the deadletter sub-queue.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken) throws InterruptedException, ServiceBusException;

    /**
     * Moves a {@link Message} to the deadletter sub-queue.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken, TransactionContext transaction) throws InterruptedException, ServiceBusException;

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
     * Moves a {@link Message} to the deadletter sub-queue with modified message properties.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException;

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
     * Moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction) throws InterruptedException, ServiceBusException;

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
     * Moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description and modified properties.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param propertiesToModify         Message properties to modify.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if deadletter failed
     */
    void deadLetter(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify, TransactionContext transaction) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with deadletter.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken);

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with deadletter.
     *
     * @param lockToken Message lock token {@link Message#getLockToken()}
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, TransactionContext transaction);

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with modified properties.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify);

    /**
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with modified properties.
     *
     * @param lockToken          Message lock token {@link Message#getLockToken()}
     * @param propertiesToModify Message properties to modify.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, Map<String, Object> propertiesToModify, TransactionContext transaction);

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
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, TransactionContext transaction);

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
     * Asynchronously moves a {@link Message} to the deadletter sub-queue with deadletter reason and error description and modified properties.
     *
     * @param lockToken                  Message lock token {@link Message#getLockToken()}
     * @param deadLetterReason           The deadletter reason.
     * @param deadLetterErrorDescription The deadletter error description.
     * @param propertiesToModify         Message properties to modify.
     * @param transaction {@link TransactionContext} which this operation should enlist to.
     * @return a CompletableFuture representing the pending deadletter.
     */
    CompletableFuture<Void> deadLetterAsync(UUID lockToken, String deadLetterReason, String deadLetterErrorDescription, Map<String, Object> propertiesToModify, TransactionContext transaction);

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
