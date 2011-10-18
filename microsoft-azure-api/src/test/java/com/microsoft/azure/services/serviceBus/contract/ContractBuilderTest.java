package com.microsoft.azure.services.serviceBus.contract;


import static org.junit.Assert.*;
import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;

public class ContractBuilderTest  {
	@Test
	public void testDefaultBuilderCreatesContractImpl() throws Exception {
		Configuration config = new Configuration();
		ServiceBusContract contract = config.create(ServiceBusContract.class);
		
		assertNotNull(contract);
	}
}
