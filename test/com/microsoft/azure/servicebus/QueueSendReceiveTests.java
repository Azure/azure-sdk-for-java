package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.servicebus.BrokeredMessage;
import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IBrokeredMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public class QueueSendReceiveTests {
	private ConnectionStringBuilder builder;
	private MessagingFactory factory;
	private IMessageSender sender;
	private IMessageReceiver receiver;
	
	@Before // Fix this. something goes wrong when we do this setup.
	public void setup() throws IOException, InterruptedException, ExecutionException, ServiceBusException
	{
		this.builder = TestUtils.getConnectionStringBuilder();
		this.factory = MessagingFactory.createFromConnectionStringBuilder(builder);
		this.sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(builder);		
		
		this.drainAllMessages(builder);
		//Thread.sleep(60000);
	}
	
	@After
	public void tearDown() throws ServiceBusException
	{
		this.sender.close();
		if(this.receiver != null)
			this.receiver.close();
		this.factory.close();
	}
	
	@Test
	public void testBasicQueueSend() throws InterruptedException, ServiceBusException, IOException
	{		
		this.sender.send(new BrokeredMessage("AMQP message"));
	}
	
	@Test
	public void testBasicQueueSendBatch() throws InterruptedException, ServiceBusException, IOException
	{		
		List<BrokeredMessage> messages = new ArrayList<BrokeredMessage>();
		for(int i=0; i<10; i++)
		{
			messages.add(new BrokeredMessage("AMQP message"));
		}
		this.sender.sendBatch(messages);
	}
	
	@Test
	public void testBasicQueueReceiveAndDelete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{	
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		this.sender.send(message);		
		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		IBrokeredMessage receivedMessage = this.receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		receivedMessage = this.receiver.receive();
		Assert.assertNull("Message received again", receivedMessage);
	}
	
	@Test
	public void testBasicQueueReceiveBatchAndDelete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		int numMessages = 10;		
		List<BrokeredMessage> messages = new ArrayList<BrokeredMessage>();
		for(int i=0; i<numMessages; i++)
		{
			messages.add(new BrokeredMessage("AMQP message"));
		}
		this.sender.sendBatch(messages);
		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		int totalReceivedMessages = 0;
		Collection<IBrokeredMessage> receivedMessages = this.receiver.receiveBatch(numMessages);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			totalReceivedMessages += receivedMessages.size();
			receivedMessages = this.receiver.receiveBatch(numMessages);
		}
		
		Assert.assertEquals("All messages not received", numMessages, totalReceivedMessages);
		receivedMessages = this.receiver.receiveBatch(numMessages);
		Assert.assertNull("Messages received again", receivedMessages);
	}
	
	@Test
	public void testBasicQueueReceiveAndComplete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		this.sender.send(message);
		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.PeekLock);
		IBrokeredMessage receivedMessage = this.receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		this.receiver.complete(receivedMessage);
		receivedMessage = this.receiver.receive();
		Assert.assertNull("Message was not properly completed", receivedMessage);
	}
	
	@Test
	public void testBasicQueueReceiveAndAbandon() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		this.sender.send(message);
		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.PeekLock);
		IBrokeredMessage receivedMessage = this.receiver.receive();
		long deliveryCount = receivedMessage.getDeliveryCount();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		this.receiver.abandon(receivedMessage);
		receivedMessage = this.receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("DeliveryCount not incremented", deliveryCount+1, receivedMessage.getDeliveryCount());
	}
	
	@Test
	public void testBasicQueueReceiveAndDeadLetter() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{		
		String messageId = UUID.randomUUID().toString();
		BrokeredMessage message = new BrokeredMessage("AMQP message");
		message.setMessageId(messageId);
		this.sender.send(message);
		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.PeekLock);
		IBrokeredMessage receivedMessage = this.receiver.receive();
		Assert.assertNotNull("Message not received", receivedMessage);
		Assert.assertEquals("Message Id did not match", messageId, receivedMessage.getMessageId());
		String deadLetterReason = "java client deadletter test";
		this.receiver.deadLetter(receivedMessage, deadLetterReason, null);
		receivedMessage = this.receiver.receive();
		Assert.assertNull("Message was not properly deadlettered", receivedMessage);
	}
	
	@Test
	public void testBasicQueueReceiveBatchAndComplete() throws InterruptedException, ServiceBusException, IOException, ExecutionException
	{
		int numMessages = 10;		
		List<BrokeredMessage> messages = new ArrayList<BrokeredMessage>();
		for(int i=0; i<numMessages; i++)
		{
			messages.add(new BrokeredMessage("AMQP message"));
		}
		this.sender.sendBatch(messages);
		
		this.receiver = ClientFactory.createMessageReceiverFromEntityPath(factory, builder.getEntityPath(), ReceiveMode.PeekLock);
		int totalMessagesReceived = 0;
		Collection<IBrokeredMessage> receivedMessages = this.receiver.receiveBatch(numMessages);
		while(receivedMessages != null && receivedMessages.size() > 0)
		{
			totalMessagesReceived += receivedMessages.size();
			for(IBrokeredMessage message : receivedMessages)
			{
				//System.out.println(message.getLockToken());
				this.receiver.complete(message);
			}
			receivedMessages = this.receiver.receiveBatch(numMessages);
		}
		Assert.assertEquals("All messages not received", numMessages, totalMessagesReceived);		
		
		receivedMessages = this.receiver.receiveBatch(numMessages);
		Assert.assertNull("Messages received again", receivedMessages);
	}
	
	private void drainAllMessages(ConnectionStringBuilder builder) throws IOException, InterruptedException, ExecutionException, ServiceBusException
	{
		Duration waitTime = Duration.ofSeconds(5);
		final int batchSize = 10;		
		IMessageReceiver receiver = ClientFactory.createMessageReceiverFromEntityPath(this.factory, this.builder.getEntityPath(), ReceiveMode.ReceiveAndDelete);
		Collection<IBrokeredMessage> messages = receiver.receiveBatch(batchSize, waitTime);
		while(messages !=null && messages.size() > 0)
		{
			messages = receiver.receiveBatch(batchSize, waitTime);
		}
		
		receiver.close();
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
