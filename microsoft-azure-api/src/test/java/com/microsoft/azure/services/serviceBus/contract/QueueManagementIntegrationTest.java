package com.microsoft.azure.services.serviceBus.contract;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.core.MediaType;
import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.azure.auth.wrap.WrapFilter;
import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.ServiceBusClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class QueueManagementIntegrationTest {
	private Configuration createConfiguration() {
		Configuration config = new Configuration();
		config.setProperty("serviceBus.uri",
				"https://lodejard.servicebus.windows.net");
		config.setProperty("wrapClient.uri",
				"https://lodejard-sb.accesscontrol.windows.net/WRAPv0.9");
		config.setProperty("wrapClient.scope",
				"http://lodejard.servicebus.windows.net/");
		config.setProperty("wrapClient.name", "owner");
		config.setProperty("wrapClient.password",
				"Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
		return config;
	}

	private ServiceBusClient createClient() throws Exception {
		return createConfiguration().create(ServiceBusClient.class);
	}

	private static String readResourceFile(String name) throws IOException {
		InputStream stream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(name);

		StringBuffer sb = new StringBuffer(1024);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));

		char[] chars = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(chars)) > -1) {
			sb.append(new String(chars, 0, numRead));
		}

		reader.close();

		return sb.toString();
	}

	@Test
	public void createAnyQueue() throws Exception {

		Client client = Client.create();
		client.addFilter(createConfiguration().create(WrapFilter.class));
		client.addFilter(new LoggingFilter());
		String data = readResourceFile("NewFile.xml");
		client.resource("https://lodejard.servicebus.windows.net")
				.path("Hello2").delete();
		client.resource("https://lodejard.servicebus.windows.net")
				.path("Hello2").type(MediaType.APPLICATION_ATOM_XML).put(data);

	}

	@Test
	public void queueCanBeCreatedAndDeleted() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();

		// Act
		Queue queue = client.getQueue("queueCanBeCreatedAndDeleted");
		queue.setPath("queueCanBeCreatedAndDeleted");
		queue.save();
		queue.delete();

		// Assert
	}

	@Test
	public void whenQueueIsCreatedEntityStateIsAlsoUpdated() throws Exception {
		// Arrange
		ServiceBusClient client = createClient();
		try {
			client.getQueue("whenQueueIsCreatedEntityStateIsAlsoUpdated")
					.delete();
		} catch (Exception e) {
		}

		// Act
		Queue queue = client.getQueue("whenQueueIsCreatedEntityStateIsAlsoUpdated");
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

		// client.createQueue("TestQueue02");

		// Act
		Queue queue = client.getQueue("Hello");
		queue.fetch();

		// Assert
		Assert.assertNotNull(queue);
		Assert.assertEquals("Hello", queue.getPath());
	}
}
