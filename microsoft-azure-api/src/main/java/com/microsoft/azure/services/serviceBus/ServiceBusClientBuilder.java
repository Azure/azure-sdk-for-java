package com.microsoft.azure.services.serviceBus;

import com.microsoft.azure.http.ClientBuilderBase;


public class ServiceBusClientBuilder extends ClientBuilderBase {
	public ServiceBusClient createClient() {
		return new ServiceBusClientImpl(this.client, this.getUrl());
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
