package com.microsoft.azure.configuration.jersey;

import java.util.Map;

import com.microsoft.azure.configuration.builder.Builder;
import com.microsoft.azure.configuration.builder.Builder.Registry;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;

public class Exports implements Builder.Exports {

	public void register(Registry registry) {
		registry.add(new Builder.Factory<Client>() {
			public Client create(Builder builder, Map<String, Object> properties) {
				ClientConfig clientConfig = (ClientConfig) properties.get("ClientConfig");
				return Client.create(clientConfig);
			}
		});
	}
}
