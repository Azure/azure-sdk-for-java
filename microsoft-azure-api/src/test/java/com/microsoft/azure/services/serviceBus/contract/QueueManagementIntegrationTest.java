package com.microsoft.azure.services.serviceBus.contract;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.Entity;
import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.ServiceBusClient;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContractImpl;
import com.sun.jersey.api.client.filter.LoggingFilter;



public class QueueManagementIntegrationTest {
	@Test
	public void queueCanBeCreatedAndDeleted() throws Exception {
		// Arrange
		ServiceBusClient client = new Configuration()
			.create(ServiceBusClient.class);
		ServiceBusContractImpl contract = (ServiceBusContractImpl) client.getContract();
		contract.getChannel().addFilter(new LoggingFilter());
		
		// Act
		Queue queue = client.getQueue("TestQueue01");
		queue.delete();
		
		// Assert
	}
	
	@Test
	public void notFoundQueuePathReturnsNull() throws Exception {
		// Arrange
		Configuration cfg = new Configuration();

		ServiceBusClient client = cfg.create(ServiceBusClient.class);
		
		
		ServiceBusContractImpl contract = (ServiceBusContractImpl) client.getContract();
		contract.getChannel().addFilter(new LoggingFilter());
		
		// Act
		Queue queue = client.getQueue("NoSuchQueueName");
		
		// Assert
		Assert.assertNull(queue);
	}
	

	@Test
	public void existingQueuePathDoesNotReturnNull() throws Exception {
		// Arrange
		ServiceBusClient client = new Configuration()
			.create(ServiceBusClient.class);
		
		ServiceBusContractImpl contract = (ServiceBusContractImpl) client.getContract();
		contract.getChannel().addFilter(new LoggingFilter());
		//contract.getChannel().getProviders().
		
		//client.createQueue("TestQueue02");
		
		// Act
		Queue queue = client.getQueue("Hello");
		
		// Assert
		Assert.assertNotNull(queue);
		Assert.assertEquals("TestQueue02", queue.getPath());
	}
}
