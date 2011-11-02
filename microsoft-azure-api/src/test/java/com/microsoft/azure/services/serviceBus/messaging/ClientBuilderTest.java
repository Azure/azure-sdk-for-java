package com.microsoft.azure.services.serviceBus.messaging;

import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.Test;

import static org.junit.Assert.*;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.ServiceBusServiceImpl;
import com.microsoft.azure.services.serviceBus.messaging.ServiceBusClient;


public class ClientBuilderTest {
	@Test
	public void testServiceBusClientCreatedWithContractImpl() throws Exception {
		// Arrange
		Configuration config = new Configuration();

		// Act
		ServiceBusClient client = config.create(ServiceBusClient.class);
		ServiceBusClient client2 = new ServiceBusClient(config);
		
		// Assert
		assertNotNull(client);
		assertNotNull(client.getContract());
		assertEquals(ServiceBusServiceImpl.class, client.getContract().getClass());

		assertNotNull(client2);
		assertNotNull(client2.getContract());
		assertEquals(ServiceBusServiceImpl.class, client2.getContract().getClass());
	}
	
	@Test
	public void testHttp404() throws Exception {
		HttpURLConnection x = (HttpURLConnection)new URL("http://github.com/no-such-file").openConnection();
		int code = x.getResponseCode();
		assertEquals(404, code);
	}
}
