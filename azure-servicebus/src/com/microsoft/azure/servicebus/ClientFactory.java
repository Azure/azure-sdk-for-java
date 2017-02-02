package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class ClientFactory {
	
	private static final ReceiveMode DEFAULTRECEIVEMODE = ReceiveMode.PeekLock;
	
	// Create sender
	public static IMessageSender createMessageSenderFromConnectionString(String amqpConnectionString) throws InterruptedException, ServiceBusException, IOException
	{		
		return Utils.completeFuture(createMessageSenderFromConnectionStringAsync(amqpConnectionString));
	}
	
	public static IMessageSender createMessageSenderFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder) throws InterruptedException, ServiceBusException, IOException
	{
		return Utils.completeFuture(createMessageSenderFromConnectionStringBuilderAsync(amqpConnectionStringBuilder));
	}
	
	public static IMessageSender createMessageSenderFromEntityPath(MessagingFactory messagingFactory, String entityPath) throws InterruptedException, ServiceBusException, IOException
	{
		return Utils.completeFuture(createMessageSenderFromFromEntityPathAsync(messagingFactory, entityPath));
	}
	
	public static CompletableFuture<IMessageSender> createMessageSenderFromConnectionStringAsync(String amqpConnectionString) throws IOException
	{
		Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
		return createMessageSenderFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString));
	}
	
	// Throwing IOException is ugly in an async method. Change it
	public static CompletableFuture<IMessageSender> createMessageSenderFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder) throws IOException
	{
		Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
		BrokeredMessageSender sender = new BrokeredMessageSender(amqpConnectionStringBuilder);
		return sender.initializeAsync().thenApply((v) -> sender);
	}
	
	public static CompletableFuture<IMessageSender> createMessageSenderFromFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath) throws IOException
	{
		Utils.assertNonNull("messagingFactory", messagingFactory);
		BrokeredMessageSender sender = new BrokeredMessageSender(messagingFactory, entityPath);
		return sender.initializeAsync().thenApply((v) -> sender);
	}
	
	
	// Create receiver
	public static IMessageReceiver createMessageReceiverFromConnectionString(String amqpConnectionString) throws InterruptedException, ServiceBusException, IOException
	{		
		return createMessageReceiverFromConnectionString(amqpConnectionString, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageReceiver createMessageReceiverFromConnectionString(String amqpConnectionString, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException, IOException
	{		
		return Utils.completeFuture(createMessageReceiverFromConnectionStringAsync(amqpConnectionString, receiveMode));
	}
	
	public static IMessageReceiver createMessageReceiverFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder) throws InterruptedException, ServiceBusException, IOException
	{
		return createMessageReceiverFromConnectionStringBuilder(amqpConnectionStringBuilder, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageReceiver createMessageReceiverFromConnectionStringBuilder(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException, IOException
	{
		return Utils.completeFuture(createMessageReceiverFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, receiveMode));
	}
	
	public static IMessageReceiver createMessageReceiverFromEntityPath(MessagingFactory messagingFactory, String entityPath) throws InterruptedException, ServiceBusException, IOException
	{
		return createMessageReceiverFromEntityPath(messagingFactory, entityPath, DEFAULTRECEIVEMODE);
	}
	
	public static IMessageReceiver createMessageReceiverFromEntityPath(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode) throws InterruptedException, ServiceBusException, IOException
	{
		return Utils.completeFuture(createMessageReceiverFromFromEntityPathAsync(messagingFactory, entityPath, receiveMode));
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringAsync(String amqpConnectionString) throws IOException
	{		
		return createMessageReceiverFromConnectionStringAsync(amqpConnectionString, DEFAULTRECEIVEMODE);
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringAsync(String amqpConnectionString, ReceiveMode receiveMode) throws IOException
	{
		Utils.assertNonNull("amqpConnectionString", amqpConnectionString);
		return createMessageReceiverFromConnectionStringBuilderAsync(new ConnectionStringBuilder(amqpConnectionString));
	}
	
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder) throws IOException
	{		
		return createMessageReceiverFromConnectionStringBuilderAsync(amqpConnectionStringBuilder, DEFAULTRECEIVEMODE);
	}
	
	// Throwing IOException is ugly in an async method. Change it
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromConnectionStringBuilderAsync(ConnectionStringBuilder amqpConnectionStringBuilder, ReceiveMode receiveMode) throws IOException
	{
		Utils.assertNonNull("amqpConnectionStringBuilder", amqpConnectionStringBuilder);
		BrokeredMessageReceiver receiver = new BrokeredMessageReceiver(amqpConnectionStringBuilder, receiveMode);
		return receiver.initializeAsync().thenApply((v) -> receiver);
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath) throws IOException
	{		
		return createMessageReceiverFromFromEntityPathAsync(messagingFactory, entityPath, DEFAULTRECEIVEMODE);
	}
	
	public static CompletableFuture<IMessageReceiver> createMessageReceiverFromFromEntityPathAsync(MessagingFactory messagingFactory, String entityPath, ReceiveMode receiveMode) throws IOException
	{
		Utils.assertNonNull("messagingFactory", messagingFactory);
		BrokeredMessageReceiver receiver = new BrokeredMessageReceiver(messagingFactory, entityPath, receiveMode);
		return receiver.initializeAsync().thenApply((v) -> receiver);
	}
}
