package com.microsoft.azure.services.serviceBus.contract;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.w3._2005.atom.Content;
import org.w3._2005.atom.Entry;
import org.w3._2005.atom.Feed;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.IntegrationTestBase;

import static org.junit.Assert.*;

public class ContractIntegrationTest extends IntegrationTestBase {

	
	@Test
	public void fetchQueueAndListQueuesWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		// Act
		Entry entry = contract.getQueue("TestAlpha");
		Feed feed = contract.getQueues();
		
		// Assert
		assertNotNull(entry);
		assertNotNull(feed);
	}

	@Test
	public void createQueueWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		// Act
		Entry entry = new Entry();
		Content content = new Content();
		QueueDescription description = new QueueDescription();
		
		entry.setTitle("TestCreateQueueWorks");
		entry.setContent(content);
		content.setType("application/xml");
		content.setQueueDescription(description);
		description.setMaxSizeInMegabytes(1024L);
		
		Entry entry2 = contract.createQueue(entry);
		
		// Assert
	}
	
	@Test
	public void deleteQueueWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		// Act
		Entry entry = new Entry();
		Content content = new Content();
		QueueDescription description = new QueueDescription();
		
		entry.setTitle("TestDeleteQueueWorks");
		entry.setContent(content);
		content.setType("application/xml");
		content.setQueueDescription(description);
		contract.createQueue(entry);

		contract.deleteQueue("TestDeleteQueueWorks");
		
		// Assert
	}
	
	@Test
	public void sendMessageWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		BrokerProperties props = new BrokerProperties();

		// Act
		contract.sendMessage("TestAlpha", props, new ByteArrayInputStream("Hello World".getBytes()));

		// Assert
	}

	@Test
	public void receiveMessageWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		BrokerProperties props = new BrokerProperties();
		contract.sendMessage("TestAlpha", props, new ByteArrayInputStream("Hello World".getBytes()));

		// Act
		MessageResult message = contract.receiveMessage("TestAlpha", 500, ReceiveMode.RECEIVE_AND_DELETE);

		// Assert
		byte[] data = new byte[100];
		int size = message.getBody().read(data);
		assertEquals(11, size);
		assertArrayEquals("Hello World".getBytes(), data);
	}
	
	@Test
	public void peekLockMessageWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		BrokerProperties props = new BrokerProperties();

		// Act
		contract.sendMessage("TestAlpha", props, new ByteArrayInputStream("Hello World".getBytes()));
		MessageResult message = contract.receiveMessage("TestAlpha", 500, ReceiveMode.PEEK_LOCK);

		// Assert
		byte[] data = new byte[100];
		int size = message.getBody().read(data);
		assertEquals(11, size);
		assertEquals("Hello World", new String(data, 0, size));
	}
}
