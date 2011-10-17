package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.configuration.builder.Builder;

public class Exports implements Builder.Exports {
	public void register(Builder.Registry registry) {
		registry.add(ServiceBusClient.class);		
	}
}
