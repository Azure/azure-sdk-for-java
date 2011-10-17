package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.configuration.builder.BuilderModule;
import com.microsoft.azure.configuration.builder.BuilderRegistry;

public class Module implements BuilderModule {
	public void register(BuilderRegistry registry) {
		registry.add(ServiceBusClient.class);
	}
}
