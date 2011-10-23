package com.microsoft.azure.services.serviceBus.contract;

import org.junit.Test;
import org.w3._2005.atom.Content;
import org.w3._2005.atom.Entry;
import org.w3._2005.atom.Feed;

import com.microsoft.azure.configuration.Configuration;
import static org.junit.Assert.*;

public class ContractIntegrationTest {
	private Configuration createConfiguration() {
		Configuration config = new Configuration();
		config.setProperty("serviceBus.uri", "https://lodejard.servicebus.windows.net");
		config.setProperty("wrapClient.uri", "https://lodejard-sb.accesscontrol.windows.net/WRAPv0.9");
		config.setProperty("wrapClient.name", "owner");
		config.setProperty("wrapClient.password", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
		config.setProperty("wrapClient.scope", "http://lodejard.servicebus.windows.net/");
		return config;
	}

	@Test
	public void fetchQueueAndListQueuesWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		// Act
		Entry entry = contract.getQueue("Hello");
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
		
		entry.setTitle("createQueueWorks");
		entry.setContent(content);
		content.setType("application/xml");
		content.setQueueDescription(description);
		description.setMaxSizeInMegabytes(1024L);
		
		contract.createQueue(entry);
		
		// Assert
	}
	
	@Test
	public void deleteQueueWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		// Act
		assertTrue("won't do this - can't recreate it yet", false);
		contract.deleteQueue("Hello");
		
		// Assert
	}
	
	@Test
	public void sendMessageWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		BrokerProperties props = new BrokerProperties();

		// Act
		contract.sendMessage("Hello", props);

		// Assert
	}

	@Test
	public void receiveMessageWorks() throws Exception {
		// Arrange
		Configuration config = createConfiguration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);

		BrokerProperties props = new BrokerProperties();

		// Act
		contract.sendMessage("Hello", props);
		contract.receiveMessage("Hello", 500, ReceiveMode.RECEIVE_AND_DELETE);

		// Assert
	}
}
