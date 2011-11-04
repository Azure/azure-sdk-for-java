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
		service.createQueue(new Queue().setName("TestReceiveMessageWorks"));
		service.sendMessage("TestReceiveMessageWorks", new Message("Hello World"));

		// Act
		Message message = service.receiveMessage("TestReceiveMessageWorks", 500, ReceiveMode.RECEIVE_AND_DELETE);
		byte[] data = new byte[100];
		int size = message.getBody().read(data);

		// Assert
		assertEquals(11, size);
		assertArrayEquals("Hello World".getBytes(), Arrays.copyOf(data, size));
	}
	
	@Test
	public void peekLockMessageWorks() throws Exception {
		// Arrange
		service.createQueue(new Queue().setName("TestPeekLockMessageWorks"));
		service.sendMessage("TestPeekLockMessageWorks", new Message("Hello Again"));

		// Act
		Message message = service.receiveMessage("TestPeekLockMessageWorks", 500, ReceiveMode.PEEK_LOCK);

		// Assert
		byte[] data = new byte[100];
		int size = message.getBody().read(data);
		assertEquals(11, size);
		assertEquals("Hello Again", new String(data, 0, size));
	}
	
	@Test
	public void peekLockedMessageCanBeCompleted() throws Exception {
		// Arrange
		service.createQueue(new Queue().setName("TestPeekLockedMessageCanBeCompleted"));
		service.sendMessage("TestPeekLockedMessageCanBeCompleted", new Message("Hello Again"));
		Message message = service.receiveMessage("TestPeekLockedMessageCanBeCompleted", 1500, ReceiveMode.PEEK_LOCK);

		// Act
		String lockToken = message.getLockToken();
		Date lockedUntil = message.getLockedUntilUtc();
		String lockLocation = message.getLockLocation();
		
		service.completeMessage(message);

		// Assert
		assertNotNull(lockToken);
		assertNotNull(lockedUntil);
		assertNotNull(lockLocation);
	}

	@Test
	public void peekLockedMessageCanBeAbandoned() throws Exception {
		// Arrange
		service.createQueue(new Queue().setName("TestPeekLockedMessageCanBeAbandoned"));
		service.sendMessage("TestPeekLockedMessageCanBeAbandoned", new Message("Hello Again"));
		Message peekedMessage = service.receiveMessage("TestPeekLockedMessageCanBeAbandoned", 1500, ReceiveMode.PEEK_LOCK);

		// Act
		String lockToken = peekedMessage.getLockToken();
		Date lockedUntil = peekedMessage.getLockedUntilUtc();

		service.abandonMessage(peekedMessage);
		Message receivedMessage = service.receiveMessage("TestPeekLockedMessageCanBeAbandoned", 1500, ReceiveMode.RECEIVE_AND_DELETE);
		

		// Assert
		assertNotNull(lockToken);
		assertNotNull(lockedUntil);
		assertNull(receivedMessage.getLockToken());
		assertNull(receivedMessage.getLockedUntilUtc());
		
	}
	
	@Test
	public void contentTypePassesThrough() throws Exception {
		// Arrange
		service.createQueue(new Queue().setName("TestContentTypePassesThrough"));

		// Act
		service.sendMessage("TestContentTypePassesThrough", 
				new Message("<data>Hello Again</data>").setContentType("text/xml"));

		Message message = service.receiveMessage("TestContentTypePassesThrough", 1500, ReceiveMode.RECEIVE_AND_DELETE);
		

		// Assert
		assertNotNull(message);
		assertEquals("text/xml", message.getContentType());
	}
}
