package com.microsoft.azure.services.serviceBus.contract;

import static org.junit.Assert.*;

import org.junit.Test;

public class BrokerPropertiesMapperTest {
	@Test
	public void jsonStringMapsToBrokerPropertiesObject(){
		// Arrange 
		BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();
		
		// Act
		BrokerProperties properties = mapper.fromString("{}");
		
		// Assert
		assertNotNull(properties);
	} 
}
