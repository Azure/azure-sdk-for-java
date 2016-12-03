package com.microsoft.azure.servicebus.messaging;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.MessagingFactory;
import com.microsoft.azure.servicebus.ServiceBusException;

public class QueueSendReceiveTests {
	@Before // Fix this. something goes wrong when we do this setup.
	public void setup() throws IOException, InterruptedException, ExecutionException, ServiceBusException
	{
		ConnectionStringBuilder builder = TestUtils.getConnectionStringBuilder();
		this.drainAllMessages(builder);
		//Thread.sleep(60000);
	}
	
	@Test
	public void testBasicQueueSend() throws InterruptedException, ServiceBusException, IOException
	{
		ConnectionStringBuilder builder = TestUtils.getConnectionStringBuilder();
		IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(builder);
		sender.send(new BrokeredMessage("AMQP message"));
	}	
	
	@Test
	public void testBasicQueueReceiveAndDelete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		ConnectionStringBuilder builder = TestUtils.getConnectionStringBuilder();
		MessagingFactory factory = MessagingFactory.createFromConnectionStringBuilder(builder);
		IMessageSender sender = ClientFactory.createMessageSenderFromEntityPath(factory, builder.getEntityPath());
		sender.send(new BrokeredMessage("AMQP message"));
		
		IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		BrokeredMessage message = receiver.receive();
		Assert.assertNotNull("Message not received", message);
		System.out.println(message.getMessageId());		
	}
	
	@Test
	public void testBasicQueueReceiveAndComplete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		ConnectionStringBuilder builder = TestUtils.getConnectionStringBuilder();
		MessagingFactory factory = MessagingFactory.createFromConnectionStringBuilder(builder);
		IMessageSender sender = ClientFactory.createMessageSenderFromEntityPath(factory, builder.getEntityPath());
		sender.send(new BrokeredMessage("AMQP message"));
		
//		IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.PeekLock);
//		BrokeredMessage message = receiver.receive();
//		Assert.assertNotNull("Message not received", message);
//		System.out.println(message.getMessageId());
		Thread.sleep(60000);
		
	}
	
	private void drainAllMessages(ConnectionStringBuilder builder) throws IOException, InterruptedException, ExecutionException, ServiceBusException
	{
		Duration waitTime = Duration.ofSeconds(5);
		final int batchSize = 10;
		MessagingFactory factory = MessagingFactory.createFromConnectionStringBuilder(builder);
		IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		Collection<BrokeredMessage> messages = receiver.receiveBatch(10, waitTime);
		while(messages !=null && messages.size() > 0)
		{
			messages = receiver.receiveBatch(10, waitTime);
		}
	}
	
	// Test send batch
	// Test send and expect timeout
	
	// receive with timeout
	// receive batch
	// receive batch with timeout
	// timed out receive should return null
	// Send message with Id, receive and verify Id
	// Send message with various properties like ttl.., receive it and verify them
	
}
