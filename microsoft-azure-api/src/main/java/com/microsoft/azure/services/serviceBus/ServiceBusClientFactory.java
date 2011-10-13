package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.http.ClientFactory;

public class ServiceBusClientFactory extends ClientFactory {
	public ServiceBusClient createClient() {
		return null;
	}	
	public MessageTranceiver openQueue(String queuePath) {
		return null;
	}
	public MessageSender openTopic(String topicPath) {
		return null;
	}
	public MessageReceiver openSubscription(String topicPath, String subscriptionName) {
		return null;
	}
}
