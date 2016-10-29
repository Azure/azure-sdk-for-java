package com.microsoft.azure.servicebus.messaging;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.MessagingFactory;
import com.microsoft.azure.servicebus.ServiceBusException;

public class ClientFactory {	
	
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
}
