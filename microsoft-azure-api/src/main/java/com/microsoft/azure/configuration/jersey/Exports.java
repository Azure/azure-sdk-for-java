package com.microsoft.azure.configuration.jersey;

import java.util.Map;

import com.microsoft.azure.configuration.builder.Builder;
import com.microsoft.azure.configuration.builder.Builder.Registry;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class Exports implements Builder.Exports {

	public void register(Registry registry) {
		registry.add(new Builder.Factory<ClientConfig>() {
			public ClientConfig create(Builder builder, Map<String, Object> properties) {
				return new DefaultClientConfig();
			}
		});
		
		registry.add(new Builder.Factory<Client>() {
			public Client create(Builder builder, Map<String, Object> properties) {
				ClientConfig clientConfig = (ClientConfig) properties.get("ClientConfig");
				Client client = Client.create(clientConfig);
				client.addFilter(new LoggingFilter());
				return client;
			}
		});
	}
}
