package com.microsoft.azure.services.serviceBus;

import org.junit.Before;

import com.microsoft.azure.configuration.Configuration;

public abstract class IntegrationTestBase {
	protected Configuration createConfiguration() {
		Configuration config = new Configuration();
		config.setProperty("serviceBus.uri", "https://lodejard.servicebus.windows.net");
		config.setProperty("wrapClient.uri", "https://lodejard-sb.accesscontrol.windows.net/WRAPv0.9");
		config.setProperty("wrapClient.name", "owner");
		config.setProperty("wrapClient.password", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
		config.setProperty("wrapClient.scope", "http://lodejard.servicebus.windows.net/");
		return config;
	}

	@Before
	public void initialize() throws Exception {
		ServiceBusClient client = createConfiguration().create(ServiceBusClient.class);
		for(Queue queue : client.listQueues()){
			if (queue.getPath().startsWith("Test") || queue.getPath().startsWith("test"))
				queue.delete();
		}
		client.getQueue("TestAlpha").save();
	}
}
