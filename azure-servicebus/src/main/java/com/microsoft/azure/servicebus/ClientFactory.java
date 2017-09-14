// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

/**
 * Utility class for creating message senders and receivers.
 */
public final class ClientFactory {

    private static final ReceiveMode DEFAULTRECEIVEMODE = ReceiveMode.PEEKLOCK;

    private ClientFactory() {
    }

    /**
     * Create message sender from service bus connection string with <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas">Shared Access Signatures</a>
     *
     * @param amqpConnectionString the connection string
     * @return {@link IMessageSender} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the sender cannot be created
     */
    public static IMessageSender createMessageSenderFromConnectionString(String amqpConnectionString) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(createMessageSenderFromConnectionStringAsync(amqpConnectionString));
    }

    /**
     * Create message sender from ConnectionStringBuilder
     * <pre>
     *     IMessageSender messageSender = ClientFactory.createMessageSenderFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, queueName));
     * </pre>
     *
     * @param amqpConnectionStringBuilder the connection string builder
     * @return {@link IMessageSender} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the sender cannot be created
     */
    public static IMessageSender createMessageSenderFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(createMessageSenderFromConnectionStringBuilderAsync(amqpConnectionStringBuilder));
    }

    static IMessageSender createMessageSenderFromEntityPath(MessagingFactory messagingFactory, String entityPath) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(createMessageSenderFromEntityPathAsync(messagingFactory, entityPath));
    }

    /**
     * Create message sender asynchronously from connection string with <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas">Shared Access Signatures</a>
     *
     * @param amqpConnectionString the connection string
     * @return a CompletableFuture representing the pending creating of {@link IMessageSender} instance
     */
    public static CompletableFuture<IMessageSender> createMessageSenderFromConnectionStringAsync(String amqpConnectionString) {
        Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
        return createMessageSenderFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString));
    }

    /**
     * Create message sender asynchronously from ConnectionStringBuilder
     *
     * @param amqpConnectionStringBuilder the connection string builder
     * @return a CompletableFuture representing the pending creating of {@link IMessageSender} instance
     */
    public static CompletableFuture<IMessageSender> createMessageSenderFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder) {
        Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
        MessageSender sender = new MessageSender(amqpConnectionStringBuilder);
        return sender.initializeAsync().thenApply((v) -> sender);
    }

    static CompletableFuture<IMessageSender> createMessageSenderFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath) {
        Utils.assertNonNull("messagingFactory", messagingFactory);
        MessageSender sender = new MessageSender(messagingFactory, entityPath);
        return sender.initializeAsync().thenApply((v) -> sender);
    }

    /**
     * Create {@link IMessageReceiver} in default {@link ReceiveMode#PEEKLOCK} mode from service bus connection string with <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas">Shared Access Signatures</a>
     *
     * @param amqpConnectionString the connection string
     * @return {@link IMessageReceiver} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the receiver cannot be created
     */
    public static IMessageReceiver createMessageReceiverFromConnectionString(String amqpConnectionString) throws InterruptedException, ServiceBusException {
        return createMessageReceiverFromConnectionString(amqpConnectionString, DEFAULTRECEIVEMODE);
    }


    /**
     * Create {@link IMessageReceiver} in default {@link ReceiveMode#PEEKLOCK} mode from service bus connection string with <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas">Shared Access Signatures</a>
     *
     * @param amqpConnectionString the connection string
     * @param receiveMode          {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return {@link IMessageReceiver} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the receiver cannot be created
     */
    public static IMessageReceiver createMessageReceiverFromConnectionString(String amqpConnectionString, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(createMessageReceiverFromConnectionStringAsync(amqpConnectionString, receiveMode));
    }

    /**
     * Create {@link IMessageReceiver} in default {@link ReceiveMode#PEEKLOCK} mode from ConnectionStringBuilder
     * <pre>
     *     IMessageReceiver messageReceiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, queueName));
     * </pre>
     *
     * @param amqpConnectionStringBuilder {@link ConnectionStringBuilder}
     * @return The {@link IMessageReceiver} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the receiver cannot be created
     */
    public static IMessageReceiver createMessageReceiverFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder) throws InterruptedException, ServiceBusException {
        return createMessageReceiverFromConnectionStringBuilder(amqpConnectionStringBuilder, DEFAULTRECEIVEMODE);
    }

    /**
     * Create {@link IMessageReceiver} from ConnectionStringBuilder
     * <pre>
     *     IMessageReceiver messageReceiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(new ConnectionStringBuilder(connectionString, queueName), ReceiveMode.PEEKLOCK);
     * </pre>
     *
     * @param amqpConnectionStringBuilder {@link ConnectionStringBuilder}
     * @param receiveMode                 {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return The {@link IMessageReceiver} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the receiver cannot be created
     */
    public static IMessageReceiver createMessageReceiverFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(createMessageReceiverFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, receiveMode));
    }

    static IMessageReceiver createMessageReceiverFromEntityPath(MessagingFactory messagingFactory, String entityPath) throws InterruptedException, ServiceBusException {
        return createMessageReceiverFromEntityPath(messagingFactory, entityPath, DEFAULTRECEIVEMODE);
    }

    static IMessageReceiver createMessageReceiverFromEntityPath(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(createMessageReceiverFromEntityPathAsync(messagingFactory, entityPath, receiveMode));
    }

    /**
     * Create {@link IMessageReceiver} in default {@link ReceiveMode#PEEKLOCK} mode asynchronously from connection string with <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas">Shared Access Signatures</a>
     *
     * @param amqpConnectionString the connection string
     * @return a CompletableFuture representing the pending creating
     */
    public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringAsync(String amqpConnectionString) {
        return createMessageReceiverFromConnectionStringAsync(amqpConnectionString, DEFAULTRECEIVEMODE);
    }

    /**
     * Create {@link IMessageReceiver} in default {@link ReceiveMode#PEEKLOCK} mode asynchronously from connection string with <a href="https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas">Shared Access Signatures</a>
     *
     * @param amqpConnectionString the connection string
     * @param receiveMode          {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return a CompletableFuture representing the pending creating
     */
    public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringAsync(String amqpConnectionString, ReceiveMode receiveMode) {
        Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
        return createMessageReceiverFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString), receiveMode);
    }

    /**
     * Create {@link IMessageReceiver} in default {@link ReceiveMode#PEEKLOCK} mode asynchronously from ConnectionStringBuilder
     *
     * @param amqpConnectionStringBuilder the connection string builder
     * @return a CompletableFuture representing the pending creating
     */
    public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder) {
        return createMessageReceiverFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, DEFAULTRECEIVEMODE);
    }

    /**
     * Create {@link IMessageReceiver} asynchronously from ConnectionStringBuilder
     *
     * @param amqpConnectionStringBuilder the connection string builder
     * @param receiveMode                 {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return a CompletableFuture representing the pending creating
     */
    public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode) {
        Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
        MessageReceiver receiver = new MessageReceiver(amqpConnectionStringBuilder, receiveMode);
        return receiver.initializeAsync().thenApply((v) -> receiver);
    }

    static CompletableFuture<IMessageReceiver> createMessageReceiverFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath) {
        return createMessageReceiverFromEntityPathAsync(messagingFactory, entityPath, DEFAULTRECEIVEMODE);
    }

    static CompletableFuture<IMessageReceiver> createMessageReceiverFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode) {
        Utils.assertNonNull("messagingFactory", messagingFactory);
        MessageReceiver receiver = new MessageReceiver(messagingFactory, entityPath, receiveMode);
        return receiver.initializeAsync().thenApply((v) -> receiver);
    }

    /**
     * Accept a {@link IMessageSession} in default {@link ReceiveMode#PEEKLOCK} mode from service bus connection string with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionString connection string
     * @param sessionId            session id, if null, service will return the first available session, otherwise, service will return specified session
     * @return {@link IMessageSession} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the session cannot be accepted
     */
    public static IMessageSession acceptSessionFromConnectionString(String amqpConnectionString, String sessionId) throws InterruptedException, ServiceBusException {
        return acceptSessionFromConnectionString(amqpConnectionString, sessionId, DEFAULTRECEIVEMODE);
    }

    /**
     * Accept a {@link IMessageSession} from service bus connection string with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionString connection string
     * @param sessionId            session id, if null, service will return the first available session, otherwise, service will return specified session
     * @param receiveMode          {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return {@link IMessageSession} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the session cannot be accepted
     */
    public static IMessageSession acceptSessionFromConnectionString(String amqpConnectionString, String sessionId, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(acceptSessionFromConnectionStringAsync(amqpConnectionString, sessionId, receiveMode));
    }

    /**
     * Accept a {@link IMessageSession} in default {@link ReceiveMode#PEEKLOCK} mode from service bus connection string builder with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionStringBuilder the connection string builder
     * @param sessionId                   session id, if null, service will return the first available session, otherwise, service will return specified session
     * @return {@link IMessageSession} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the session cannot be accepted
     */
    public static IMessageSession acceptSessionFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId) throws InterruptedException, ServiceBusException {
        return acceptSessionFromConnectionStringBuilder(amqpConnectionStringBuilder, sessionId, DEFAULTRECEIVEMODE);
    }

    /**
     * Accept a {@link IMessageSession} from service bus connection string builder with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionStringBuilder the connection string builder
     * @param sessionId                   session id, if null, service will return the first available session, otherwise, service will return specified session
     * @param receiveMode                 {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return {@link IMessageSession} instance
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if the session cannot be accepted
     */
    public static IMessageSession acceptSessionFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(acceptSessionFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, sessionId, receiveMode));
    }

    static IMessageSession acceptSessionFromEntityPath(MessagingFactory messagingFactory, String entityPath, String sessionId) throws InterruptedException, ServiceBusException {
        return acceptSessionFromEntityPath(messagingFactory, entityPath, sessionId, DEFAULTRECEIVEMODE);
    }

    static IMessageSession acceptSessionFromEntityPath(MessagingFactory messagingFactory, String entityPath, String sessionId, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException {
        return Utils.completeFuture(acceptSessionFromEntityPathAsync(messagingFactory, entityPath, sessionId, receiveMode));
    }

    /**
     * Accept a {@link IMessageSession} in default {@link ReceiveMode#PEEKLOCK} mode asynchronously from service bus connection string with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionString the connection string
     * @param sessionId            session id, if null, service will return the first available session, otherwise, service will return specified session
     * @return a CompletableFuture representing the pending session accepting
     */
    public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringAsync(String amqpConnectionString, String sessionId) {
        return acceptSessionFromConnectionStringAsync(amqpConnectionString, sessionId, DEFAULTRECEIVEMODE);
    }

    /**
     * Accept a {@link IMessageSession} asynchronously from service bus connection string with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionString the connection string
     * @param sessionId            session id, if null, service will return the first available session, otherwise, service will return specified session
     * @param receiveMode          {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return a CompletableFuture representing the pending session accepting
     */
    public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringAsync(String amqpConnectionString, String sessionId, ReceiveMode receiveMode) {
        Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
        return acceptSessionFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString), sessionId, receiveMode);
    }

    /**
     * Accept a {@link IMessageSession} in default {@link ReceiveMode#PEEKLOCK} mode asynchronously from service bus connection string builder with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionStringBuilder the connection string builder
     * @param sessionId                   session id, if null, service will return the first available session, otherwise, service will return specified session
     * @return a CompletableFuture representing the pending session accepting
     */
    public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId) {
        return acceptSessionFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, sessionId, DEFAULTRECEIVEMODE);
    }

    /**
     * Accept a {@link IMessageSession} asynchronously from service bus connection string builder with specified session id. Session Id can be null, if null, service will return the first available session.
     *
     * @param amqpConnectionStringBuilder connection string builder
     * @param sessionId                   session id, if null, service will return the first available session, otherwise, service will return specified session
     * @param receiveMode                 {@link ReceiveMode} PeekLock or ReceiveAndDelete
     * @return a CompletableFuture representing the pending session accepting
     */
    public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId, ReceiveMode receiveMode) {
        Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
        MessageSession session = new MessageSession(amqpConnectionStringBuilder, sessionId, receiveMode);
        return session.initializeAsync().thenApply((v) -> session);
    }

    static CompletableFuture<IMessageSession> acceptSessionFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath, String sessionId) {
        return acceptSessionFromEntityPathAsync(messagingFactory, entityPath, sessionId, DEFAULTRECEIVEMODE);
    }

    static CompletableFuture<IMessageSession> acceptSessionFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath, String sessionId, ReceiveMode receiveMode) {
        Utils.assertNonNull("messagingFactory", messagingFactory);
        MessageSession session = new MessageSession(messagingFactory, entityPath, sessionId, receiveMode);
        return session.initializeAsync().thenApply((v) -> session);
    }
}
