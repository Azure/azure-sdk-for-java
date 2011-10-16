package com.microsoft.azure.configuration;

import com.microsoft.azure.services.serviceBus.ServiceBusClient;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContract;
import com.microsoft.azure.services.serviceBus.contract.ServiceBusContractImpl;
import com.sun.jersey.api.client.Client;


public class Configuration implements Builder {

	private Builder builder;

	public Configuration()  {
		setBuilder(getStandardComponentsBuilder());
	}

	public Configuration(Builder builder)  {
		setBuilder(builder);
	}

	public static Builder getStandardComponentsBuilder() {
		DefaultBuilder builder = new DefaultBuilder();
		builder.add(ServiceBusClient.class, ServiceBusClient.class, ServiceBusContract.class);
		builder.add(ServiceBusContract.class, ServiceBusContractImpl.class);
		builder.add(Client.class, Client.class);
		return builder;
	}
	
	public Builder getBuilder() {
		return builder;
	}

	public void setBuilder(Builder builder) {
		this.builder = builder;
	}

	public <T> T build(Class<T> c) throws Exception {
		return builder.build(c);
	}
    
}
