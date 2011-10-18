package com.microsoft.azure.configuration;

import java.util.HashMap;
import java.util.Map;
import com.microsoft.azure.configuration.builder.Builder;
import com.microsoft.azure.configuration.builder.DefaultBuilder;
import com.microsoft.azure.services.serviceBus.contract.EntryModelProvider;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class Configuration  {

	Map<String, Object> properties;
	Builder builder;

	public Configuration()  {
		this.properties = new HashMap<String, Object>();
		this.builder = DefaultBuilder.create();
		init();
	}

	public Configuration(Builder builder)  {
		this.properties = new HashMap<String, Object>();
		this.builder = builder;
		init();
	}

	private void init() {
		setProperty("ClientConfig", new DefaultClientConfig(EntryModelProvider.class));
	}

	public <T> T create(Class<T> service) throws Exception {
		return builder.build(service, properties);
	}
	
	public Builder getBuilder() {
		return builder;
	}

	public Object getProperty(String name) {
		return properties.get(name);
	}

	public void setProperty(String name, Object value) {
		properties.put(name, value);
	}
}
