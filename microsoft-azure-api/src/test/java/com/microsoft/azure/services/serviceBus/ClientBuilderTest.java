package com.microsoft.azure.services.serviceBus;

import org.junit.Test;

import static org.junit.Assert.*;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContractImpl;


public class ClientBuilderTest {
	@Test
	public void testServiceBusClientCreatedWithContractImpl() throws Exception {
		Configuration config = new Configuration();

		ServiceBusClient client = config.build(ServiceBusClient.class);
		
		assertNotNull(client);
		assertNotNull(client.getContract());
		assertEquals(ServiceBusContractImpl.class, client.getContract().getClass());
	}
}
