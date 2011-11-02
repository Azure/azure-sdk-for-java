package com.microsoft.azure.services.serviceBus;

import java.io.ByteArrayInputStream;

import org.junit.Before;
import org.junit.Test;
import com.microsoft.azure.services.serviceBus.schema.Content;
import com.microsoft.azure.services.serviceBus.schema.Entry;
import com.microsoft.azure.services.serviceBus.schema.Feed;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.BrokerProperties;
import com.microsoft.azure.services.serviceBus.MessageResult;
import com.microsoft.azure.services.serviceBus.ReceiveMode;
import com.microsoft.azure.services.serviceBus.ServiceBusService;
import com.microsoft.azure.services.serviceBus.schema.QueueDescription;

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

		// Act
		Entry entry = new Entry();
		Content content = new Content();
		QueueDescription description = new QueueDescription();
		
		entry.setTitle("TestDeleteQueueWorks");
		entry.setContent(content);
		content.setType("application/xml");
		content.setQueueDescription(description);
//		service.createQueue(entry);

		service.deleteQueue("TestDeleteQueueWorks");
		
		// Assert
	}
	
	@Test
	public void sendMessageWorks() throws Exception {
		// Arrange

		BrokerProperties props = new BrokerProperties();

		// Act
		service.sendMessage("TestAlpha", props, new ByteArrayInputStream("Hello World".getBytes()));

		// Assert
	}

	@Test
	public void receiveMessageWorks() throws Exception {
		// Arrange

		BrokerProperties props = new BrokerProperties();
		service.sendMessage("TestAlpha", props, new ByteArrayInputStream("Hello World".getBytes()));

		// Act
		MessageResult message = service.receiveMessage("TestAlpha", 500, ReceiveMode.RECEIVE_AND_DELETE);

		// Assert
		byte[] data = new byte[100];
		int size = message.getBody().read(data);
		assertEquals(11, size);
		assertArrayEquals("Hello World".getBytes(), data);
	}
	
	@Test
	public void peekLockMessageWorks() throws Exception {
		// Arrange

		BrokerProperties props = new BrokerProperties();

		// Act
		service.sendMessage("TestAlpha", props, new ByteArrayInputStream("Hello World".getBytes()));
		MessageResult message = service.receiveMessage("TestAlpha", 500, ReceiveMode.PEEK_LOCK);

		// Assert
		byte[] data = new byte[100];
		int size = message.getBody().read(data);
		assertEquals(11, size);
		assertEquals("Hello World", new String(data, 0, size));
	}
}
