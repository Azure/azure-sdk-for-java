package com.microsoft.azure.services.serviceBus;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.azure.services.serviceBus.implementation.BrokerProperties;
import com.microsoft.azure.services.serviceBus.implementation.BrokerPropertiesMapper;

public class BrokerPropertiesMapperTest {
	@Test
	public void jsonStringMapsToBrokerPropertiesObject(){
		// Arrange 
		BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();
		
		// Act
		BrokerProperties properties = mapper.fromString("{\"DeliveryCount\":5,\"MessageId\":\"something\"}");
		
		// Assert
		assertNotNull(properties);
		assertEquals(new Integer(5), properties.getDeliveryCount());
		assertEquals("something", properties.getMessageId());
	} 

	@Test
	public void nonDefaultPropertiesMapToJsonString(){
		// Arrange 
		BrokerPropertiesMapper mapper = new BrokerPropertiesMapper();
		// Act
		BrokerProperties properties = new BrokerProperties();
		properties.setMessageId("foo");
		properties.setDeliveryCount(7);
		String json = mapper.toString(properties);
		
		// Assert
		assertNotNull(json);
		assertEquals("{\"DeliveryCount\":7,\"MessageId\":\"foo\"}", json);
	} 
}
