package com.microsoft.azure.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.inject.Provider;

import com.microsoft.azure.configuration.builder.Builder;
import com.microsoft.azure.configuration.builder.DefaultBuilder;
import com.microsoft.azure.services.serviceBus.contract.EntryModelProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class Configuration  {

	private Builder builder;
	Map<String, Object> properties;


	public Configuration()  {
		this.properties = new HashMap<String, Object>();
		this.builder = createDefaultBuilder();
		setProperty("ClientConfig", createDefaultClientConfig());
	}

	public Configuration(Builder builder)  {
		this.properties = new HashMap<String, Object>();
		this.builder = builder;
		setProperty("ClientConfig", createDefaultClientConfig());
	}

	public static ClientConfig createDefaultClientConfig() { 
		return new DefaultClientConfig(EntryModelProvider.class);
	}

	Builder createDefaultBuilder() {
		
		final DefaultBuilder builder = new DefaultBuilder();
		
		for(Builder.Exports exports : ServiceLoader.load(Builder.Exports.class)) {
			exports.register(builder);
		}
				
		return builder;
	}
	

	public <T> T build(Class<T> c) throws Exception {
		return builder.build(c, getProperties());
	}
	
	public Builder getBuilder() {
		return builder;
	}

	public void setBuilder(Builder builder) {
		this.builder = builder;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Object getProperty(String name) {
		return getProperties().get(name);
	}

	public void setProperty(String name, Object value) {
		getProperties().put(name, value);
	}
}
