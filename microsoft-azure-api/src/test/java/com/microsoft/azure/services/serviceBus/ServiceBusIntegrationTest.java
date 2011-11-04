package com.microsoft.azure.services.serviceBus;

import java.util.Arrays;
import java.util.Date;

import javax.sound.sampled.ReverbType;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.Message;
import com.microsoft.azure.services.serviceBus.ReceiveMode;
import com.microsoft.azure.services.serviceBus.ServiceBusService;
import static org.junit.Assert.*;

public class ServiceBusIntegrationTest extends IntegrationTestBase {

	private Configuration config;
	private ServiceBusService service;

	static ReceiveMessageOptions RECEIVE_AND_DELETE_5_SECONDS = new ReceiveMessageOptions().setReceiveAndDelete().setTimeout(5);
	static ReceiveMessageOptions PEEK_LOCK_5_SECONDS = new ReceiveMessageOptions().setPeekLock().setTimeout(5);
	
	@Before
	public void createService() throws Exception {
		config = createConfiguration();
		service = config.create(ServiceBusService.class);
	}
	
	@Test
	public void fetchQueueAndListQueuesWorks() throws Exception {
		// Arrange

		// Act
		Queue entry = service.getQueue("TestAlpha");
		QueueList feed = service.getQueueList();
		
		// Assert
		assertNotNull(entry);
		assertNotNull(feed);
	}


	@Test
	public void createQueueWorks() throws Exception {
		// Arrange

		// Act
		Queue queue = new Queue();
		
		queue.setName("TestCreateQueueWorks");
		queue.setMaxSizeInMegabytes(1024L);
		
		Queue saved = service.createQueue(queue);
		
		// Assert
		assertNotNull(saved);
		assertNotSame(queue, saved);
		assertEquals("TestCreateQueueWorks", saved.getName());
	}
	
	@Test
	public void deleteQueueWorks() throws Exception {
		// Arrange
		service.createQueue(new Queue().setName("TestDeleteQueueWorks"));

		// Act
		service.deleteQueue("TestDeleteQueueWorks");
		
		// Assert
	}
	
	@Test
	public void sendMessageWorks() throws Exception {
		// Arrange
		Message message = new Message("sendMessageWorks");

		// Act
		service.sendMessage("TestAlpha", message);

		// Assert
	}

	@Test
	public void receiveMessageWorks() throws Exception {
		// Arrange
		String queueName = "TestReceiveMessageWorks";
		service.createQueue(new Queue().setName(queueName));
		service.sendMessage(queueName, new Message("Hello World"));

		// Act
		Message message = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);
		byte[] data = new byte[100];
		int size = message.getBody().read(data);

		// Assert
		assertEquals(11, size);
		assertArrayEquals("Hello World".getBytes(), Arrays.copyOf(data, size));
	}
	
	@Test
	public void peekLockMessageWorks() throws Exception {
		// Arrange
		String queueName = "TestPeekLockMessageWorks";
		service.createQueue(new Queue().setName(queueName));
		service.sendMessage(queueName, new Message("Hello Again"));

		// Act
		Message message = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

		// Assert
		byte[] data = new byte[100];
		int size = message.getBody().read(data);
		assertEquals(11, size);
		assertEquals("Hello Again", new String(data, 0, size));
	}
	
	@Test
	public void peekLockedMessageCanBeCompleted() throws Exception {
		// Arrange
		String queueName = "TestPeekLockedMessageCanBeCompleted";
		service.createQueue(new Queue().setName(queueName));
		service.sendMessage(queueName, new Message("Hello Again"));
		Message message = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

		// Act
		String lockToken = message.getLockToken();
		Date lockedUntil = message.getLockedUntilUtc();
		String lockLocation = message.getLockLocation();
		
		service.deleteMessage(message);

		// Assert
		assertNotNull(lockToken);
		assertNotNull(lockedUntil);
		assertNotNull(lockLocation);
	}

	@Test
	public void peekLockedMessageCanBeUnlocked() throws Exception {
		// Arrange
		String queueName = "TestPeekLockedMessageCanBeUnlocked";
		service.createQueue(new Queue().setName(queueName));
		service.sendMessage(queueName, new Message("Hello Again"));
		Message peekedMessage = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

		// Act
		String lockToken = peekedMessage.getLockToken();
		Date lockedUntil = peekedMessage.getLockedUntilUtc();

		service.unlockMessage(peekedMessage);
		Message receivedMessage = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);
		

		// Assert
		assertNotNull(lockToken);
		assertNotNull(lockedUntil);
		assertNull(receivedMessage.getLockToken());
		assertNull(receivedMessage.getLockedUntilUtc());
	}


	@Test
	public void peekLockedMessageCanBeDeleted() throws Exception {
		// Arrange
		String queueName = "TestPeekLockedMessageCanBeDeleted";
		service.createQueue(new Queue().setName(queueName));
		service.sendMessage(queueName, new Message("Hello Again"));
		Message peekedMessage = service.receiveQueueMessage(queueName, PEEK_LOCK_5_SECONDS);

		// Act
		String lockToken = peekedMessage.getLockToken();
		Date lockedUntil = peekedMessage.getLockedUntilUtc();

		service.deleteMessage(peekedMessage);
		Message receivedMessage = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);
		
		// Assert
		assertNotNull(lockToken);
		assertNotNull(lockedUntil);
		assertNull(receivedMessage.getLockToken());
		assertNull(receivedMessage.getLockedUntilUtc());
	}

	@Test
	public void contentTypePassesThrough() throws Exception {
		// Arrange
		String queueName = "TestContentTypePassesThrough";
		service.createQueue(new Queue().setName(queueName));

		// Act
		service.sendMessage(queueName, 
				new Message("<data>Hello Again</data>").setContentType("text/xml"));

		Message message = service.receiveQueueMessage(queueName, RECEIVE_AND_DELETE_5_SECONDS);

		// Assert
		assertNotNull(message);
		assertEquals("text/xml", message.getContentType());
	}
}
