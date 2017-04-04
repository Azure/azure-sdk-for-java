package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

class ClientFactory {
	
	private static final ReceiveMode DEFAULTRECEIVEMODE = ReceiveMode.PeekLock;
	
	// Create sender
	public static IMessageSender createMessageSenderFromConnectionString(String amqpConnectionString) throws InterruptedException, ServiceBusException
	{		
		return Utils.completeFuture(createMessageSenderFromConnectionStringAsync(amqpConnectionString));
	}
	
	public static IMessageSender createMessageSenderFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(createMessageSenderFromConnectionStringBuilderAsync(amqpConnectionStringBuilder));
	}
	
	public static IMessageSender createMessageSenderFromEntityPath(MessagingFactory messagingFactory, String entityPath) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(createMessageSenderFromEntityPathAsync(messagingFactory, entityPath));
	}
	
	public static CompletableFuture<IMessageSender> createMessageSenderFromConnectionStringAsync(String amqpConnectionString)
	{
		Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
		return createMessageSenderFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString));
	}	
	
	public static CompletableFuture<IMessageSender> createMessageSenderFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder)
	{
		Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
		MessageSender sender = new MessageSender(amqpConnectionStringBuilder);
		return sender.initializeAsync().thenApply((v) -> sender);
	}
	
	public static CompletableFuture<IMessageSender> createMessageSenderFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath)
	{
		Utils.assertNonNull("messagingFactory", messagingFactory);
		MessageSender sender = new MessageSender(messagingFactory, entityPath);
		return sender.initializeAsync().thenApply((v) -> sender);
	}
	
	
	// Create receiver
	public static IMessageReceiver createMessageReceiverFromConnectionString(String amqpConnectionString) throws InterruptedException, ServiceBusException
	{		
		return createMessageReceiverFromConnectionString(amqpConnectionString, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageReceiver createMessageReceiverFromConnectionString(String amqpConnectionString, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{		
		return Utils.completeFuture(createMessageReceiverFromConnectionStringAsync(amqpConnectionString, receiveMode));
	}
	
	public static IMessageReceiver createMessageReceiverFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder) throws InterruptedException, ServiceBusException
	{
		return createMessageReceiverFromConnectionStringBuilder(amqpConnectionStringBuilder, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageReceiver createMessageReceiverFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(createMessageReceiverFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, receiveMode));
	}
	
	public static IMessageReceiver createMessageReceiverFromEntityPath(MessagingFactory messagingFactory, String entityPath) throws InterruptedException, ServiceBusException
	{
		return createMessageReceiverFromEntityPath(messagingFactory, entityPath, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageReceiver createMessageReceiverFromEntityPath(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(createMessageReceiverFromEntityPathAsync(messagingFactory, entityPath, receiveMode));
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringAsync(String amqpConnectionString)
	{		
		return createMessageReceiverFromConnectionStringAsync(amqpConnectionString, DEFAULTRECEIVEMODE);
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringAsync(String amqpConnectionString, ReceiveMode receiveMode)
	{
		Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
		return createMessageReceiverFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString));
	}	
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder)
	{		
		return createMessageReceiverFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, DEFAULTRECEIVEMODE);
	}

	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode)
	{
		Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
		MessageReceiver receiver = new MessageReceiver(amqpConnectionStringBuilder, receiveMode);
		return receiver.initializeAsync().thenApply((v) -> receiver);
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath)
	{		
		return createMessageReceiverFromEntityPathAsync(messagingFactory, entityPath, DEFAULTRECEIVEMODE);
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode)
	{
		Utils.assertNonNull("messagingFactory", messagingFactory);
		MessageReceiver receiver = new MessageReceiver(messagingFactory, entityPath, receiveMode);
		return receiver.initializeAsync().thenApply((v) -> receiver);
	}
	
	// Accept Session
	public static IMessageSession acceptSessionFromConnectionString(String amqpConnectionString, String sessionId) throws InterruptedException, ServiceBusException
	{		
		return acceptSessionFromConnectionString(amqpConnectionString, sessionId, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageSession acceptSessionFromConnectionString(String amqpConnectionString, String sessionId, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{		
		return Utils.completeFuture(acceptSessionFromConnectionStringAsync(amqpConnectionString, sessionId, receiveMode));
	}
	
	public static IMessageSession acceptSessionFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId) throws InterruptedException, ServiceBusException
	{
		return acceptSessionFromConnectionStringBuilder(amqpConnectionStringBuilder, sessionId, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageSession acceptSessionFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(acceptSessionFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, sessionId, receiveMode));
	}
	
	public static IMessageSession acceptSessionFromEntityPath(MessagingFactory messagingFactory, String entityPath, String sessionId) throws InterruptedException, ServiceBusException
	{
		return acceptSessionFromEntityPath(messagingFactory, entityPath, sessionId, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageSession acceptSessionFromEntityPath(MessagingFactory messagingFactory, String entityPath, String sessionId, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException
	{
		return Utils.completeFuture(acceptSessionFromEntityPathAsync(messagingFactory, entityPath, sessionId, receiveMode));
	}
	
	public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringAsync(String amqpConnectionString, String sessionId)
	{		
		return acceptSessionFromConnectionStringAsync(amqpConnectionString, sessionId, DEFAULTRECEIVEMODE);
	}
	
	public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringAsync(String amqpConnectionString, String sessionId, ReceiveMode receiveMode)
	{
		Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
		return acceptSessionFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString), sessionId);
	}	
	
	public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId)
	{		
		return acceptSessionFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, sessionId, DEFAULTRECEIVEMODE);
	}
		
	public static CompletableFuture<IMessageSession> acceptSessionFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder, String sessionId, ReceiveMode receiveMode)
	{
		Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
		MessageSession session = new MessageSession(amqpConnectionStringBuilder, sessionId, receiveMode);
		return session.initializeAsync().thenApply((v) -> session);
	}
	
	public static CompletableFuture<IMessageSession> acceptSessionFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath, String sessionId)
	{		
		return acceptSessionFromEntityPathAsync(messagingFactory, entityPath, sessionId, DEFAULTRECEIVEMODE);
	}
	
	public static CompletableFuture<IMessageSession> acceptSessionFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath, String sessionId, ReceiveMode receiveMode)
	{
		Utils.assertNonNull("messagingFactory", messagingFactory);
		MessageSession session = new MessageSession(messagingFactory, entityPath, sessionId, receiveMode);
		return session.initializeAsync().thenApply((v) -> session);
	}	
}
