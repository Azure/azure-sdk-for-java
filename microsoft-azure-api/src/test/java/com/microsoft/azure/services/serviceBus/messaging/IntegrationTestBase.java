package com.microsoft.azure.services.serviceBus.messaging;

import java.io.Console;

import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.azure.configuration.Configuration;
import com.microsoft.azure.services.serviceBus.ServiceBusService;
import com.microsoft.azure.services.serviceBus.Queue;
import com.microsoft.azure.services.serviceBus.messaging.ReceiveMessageOptions;
import com.microsoft.azure.services.serviceBus.messaging.ServiceBusClient;

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
	
	@BeforeClass
	public static void initializeSystem() {
		System.out.println("initialize");
		System.setProperty("http.proxyHost", "itgproxy");
		System.setProperty("http.proxyPort", "80");
		//System.setProperty("http.keepAlive", "false");
	}

	@Before
	public void initialize() throws Exception {
		System.out.println("initialize");
		System.setProperty("http.proxyHost", "itgproxy");
		System.setProperty("http.proxyPort", "80");
		//System.setProperty("http.keepAlive", "false");
		
		boolean testAlphaExists = false;
		ServiceBusService service = createConfiguration().create(ServiceBusService.class);
		for(Queue queue : service.iterateQueues()) {
			if (queue.getTitle().startsWith("Test") || queue.getTitle().startsWith("test")) {
				if (queue.getTitle().equalsIgnoreCase("TestAlpha")) {
					testAlphaExists = true;
					long count = queue.getMessageCount();
//					for(long i = 0; i != count; ++i) {
//						queue.receiveMessage(new ReceiveMessageOptions().setTimeout(2000));
//					}
				} else {
//					queue.delete();
				}
			}
		}
		if (!testAlphaExists) {
			service.createQueue(new Queue().setTitle("TestAlpha"));
		}
	}
}
