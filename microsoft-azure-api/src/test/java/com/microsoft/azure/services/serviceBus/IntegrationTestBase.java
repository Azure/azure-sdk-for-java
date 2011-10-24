package com.microsoft.azure.services.serviceBus;

import org.junit.Before;

import com.microsoft.azure.configuration.Configuration;

public abstract class IntegrationTestBase {
	protected Configuration createConfiguration() {
		Configuration config = new Configuration();
		config.setProperty("serviceBus.uri", "https://lodejard.servicebus.windows.net");
		config.setProperty("serviceBus.wrap.uri", "https://lodejard-sb.accesscontrol.windows.net/WRAPv0.9");
		config.setProperty("serviceBus.wrap.name", "owner");
		config.setProperty("serviceBus.wrap.password", "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=");
		config.setProperty("serviceBus.wrap.scope", "http://lodejard.servicebus.windows.net/");

		// when mock running
		//config.setProperty("serviceBus.uri", "http://localhost:8086");
		//config.setProperty("wrapClient.uri", "http://localhost:8081/WRAPv0.9");
		
		return config;
	}

	@Before
	public void initialize() throws Exception {
		
		System.setProperty("http.keepAlive", "false");
		
		ServiceBusClient client = createConfiguration().create(ServiceBusClient.class);
		for(Queue queue : client.listQueues()){
			if (queue.getPath().startsWith("Test") || queue.getPath().startsWith("test"))
				queue.delete();
		}
		client.getQueue("TestAlpha").save();
	}
}
