package com.microsoft.azure.services.serviceBus.messaging;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import com.microsoft.azure.services.serviceBus.messaging.Message;
import com.microsoft.azure.services.serviceBus.messaging.Queue;
import com.microsoft.azure.services.serviceBus.messaging.ServiceBusClient;


public class QueueManagementIntegrationTest extends IntegrationTestBase {

	private ServiceBusClient createClient() throws Exception {
		return new ServiceBusClient(createConfiguration());
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
	public void existingQueuePathDoesNotReturnNull() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("TestAlpha");
		queue.fetch();

		// Assert
		assertNotNull(queue);
		assertEquals("TestAlpha", queue.getName());
	}
	
	@Test
	public void createQueueAndSendAndReceiveMessage() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("TestCreateQueueAndSendAndReceiveMessage");
		queue.save();
		
		queue.sendMessage(new Message("Hello World"));
		Message received = queue.receiveMessage();
		
		// Assert
		assertNotNull(received);
		assertEquals(1, (int)received.getDeliveryCount());
	}

	
	@Test
	public void peekLockedMessageHasLockTokenAndLockedUntilUtc() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("TestCreateQueueAndSendAndReceiveMessage");
		queue.save();
		
		queue.sendMessage(new Message("Hello World"));
		Message received = queue.peekLockMessage();
		
		// Assert
		assertNotNull(received);
		assertNotNull(received.getLockToken());
		assertNotNull(received.getLockedUntilUtc());
	}
}
