package com.microsoft.azure.services.serviceBus;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.ServiceBusClient;

public class QueueManagementIntegrationTest extends IntegrationTestBase {

	private ServiceBusClient createClient() throws Exception {
		return createConfiguration().create(ServiceBusClient.class);
	}

	@Test
	public void queueCanBeCreatedAndDeleted() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("TestQueueCanBeCreatedAndDeleted");
		queue.save();
		queue.delete();

		// Assert
	}

	@Test
	public void whenQueueIsCreatedEntityStateIsAlsoUpdated() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("TestWhenQueueIsCreatedEntityStateIsAlsoUpdated");
		Long maxSizeBefore = queue.getMaxSizeInMegabytes();
		queue.save();
		Long maxSizeAfter = queue.getMaxSizeInMegabytes();
		queue.delete();

		// Assert
		assertNull(maxSizeBefore);
		assertNotNull(maxSizeAfter);
	}

	@Test
	public void notFoundQueuePathReturnsNull() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("NoSuchQueueName");

		// Assert
		Assert.assertNull(queue);
	}

	@Test
	public void existingQueuePathDoesNotReturnNull() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("Hello");
		queue.fetch();

		// Assert
		Assert.assertNotNull(queue);
		Assert.assertEquals("Hello", queue.getName());
	}
	
	@Test
	public void createQueueAndSendAndReceiveMessage() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("TestCreateQueueAndSendAndReceiveMessage");
		queue.save();
		
		queue.sendMessage(new Message("Hello World").setTimeToLive(25L));
		Message received = queue.receiveMessage(new ReceiveMessageOptions().setTimeout(2500));
		
		// Assert
		Assert.assertNotNull(received);
		Assert.assertEquals(1, (int)received.getDeliveryCount());
	}
}
