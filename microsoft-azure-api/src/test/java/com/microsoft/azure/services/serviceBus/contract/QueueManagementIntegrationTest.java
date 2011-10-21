package com.microsoft.azure.services.serviceBus.contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.Entity;
import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.ServiceBusClient;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContractImpl;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.representation.Form;



public class QueueManagementIntegrationTest {
	@Test
	public void queueCanBeCreatedAndDeleted() throws Exception {
		// Arrange
		ServiceBusClient client = new Configuration()
			.create(ServiceBusClient.class);
		ServiceBusContractImpl contract = (ServiceBusContractImpl) client.getContract();
		contract.getChannel().addFilter(new LoggingFilter());

		/*
		String message = readResourceFile("NewFile.xml");
		
		Form form = new Form();
		form.add("wrap_name", "owner");
		form.add("wrap_password", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
		form.add("wrap_scope", "http://lodejard.servicebus.windows.net/");
		
		Form wrapResponse = contract.getChannel().resource("https://lodejard-sb.accesscontrol.windows.net/")
			.path("WRAPv0.9")
			.post(Form.class, form);
		String accessToken = wrapResponse.get("wrap_access_token").get(0);
		
		
		contract.getChannel().resource("https://lodejard.servicebus.windows.net/")
		.path("Hello")
		.header("Authorization", "WRAP access_token=\"" + accessToken + "\"")
		//.type("application/atom+xml")
		.get(String.class);

		contract.getChannel().resource("https://lodejard.servicebus.windows.net/")
			.path("Hello")
			.header("Authorization", "WRAP access_token=\"" + accessToken + "\"")
			.type("application/atom+xml")
			.accept("application/atom+xml")
			.put(message);
*/		
		// Act
		Queue queue = client.getQueue("Hello");
		queue.fetch();
		queue.setPath("TestQueue01");
		queue.save();
		queue.delete();
		
		// Assert
	}
	
	public static String readResourceFile(String name) throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		
		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				
		char[] chars = new char[1024];
		int numRead = 0;
		while( (numRead = reader.read(chars)) > -1){
			sb.append(String.valueOf(chars));	
		}

		reader.close();

		return sb.toString();
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
		queue.fetch();
		
		// Assert
		Assert.assertNotNull(queue);
		Assert.assertEquals("Hello", queue.getPath());
	}
}
