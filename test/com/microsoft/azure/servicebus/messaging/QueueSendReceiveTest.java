package com.microsoft.azure.servicebus.messaging;

import java.io.IOException;

import org.junit.Test;

import com.microsoft.azure.servicebus.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.ServiceBusException;

public class QueueSendReceiveTest {
	@Test
	public void testBasicQueueSend() throws InterruptedException, ServiceBusException, IOException
	{
		ConnectionStringBuilder builder = TestUtils.getConnectionStringBuilder();
		IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(builder);
		sender.send(new BrokeredMessage("AMQP first message"));
	}
}
