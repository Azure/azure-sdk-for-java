package com.microsoft.azure.services.serviceBus;


import static org.junit.Assert.*;
import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.ServiceBusService;

public class ContractBuilderTest  {
	@Test
	public void testDefaultBuilderCreatesServiceImpl() throws Exception {
		Configuration config = new Configuration();
		ServiceBusService service = config.create(ServiceBusService.class);
		
		assertNotNull(service);
		assertEquals(ServiceBusServiceImpl.class, service.getClass());
	}
}
