package com.microsoft.azure.configuration;

import java.util.ServiceLoader;

import javax.inject.Provider;

import com.microsoft.azure.configuration.builder.Builder;
import com.microsoft.azure.configuration.builder.DefaultBuilder;
import com.microsoft.azure.services.serviceBus.contract.EntryModelProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class Configuration implements Builder {

	private Builder builder;
	protected ClientConfig clientConfig;

	public Configuration()  {
		this.clientConfig = createDefaultClientConfig();
		try {
			this.builder = createDefaultBuilder();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Configuration(Builder builder)  {
		this.builder = builder;
	}

	public static ClientConfig createDefaultClientConfig() { 
		return new DefaultClientConfig(EntryModelProvider.class);
	}

	Builder createDefaultBuilder() throws Exception {
		
		final DefaultBuilder builder = new DefaultBuilder();
		
		for(Builder.Exports exports : ServiceLoader.load(Builder.Exports.class)){
			exports.register(builder);
		}
		
		final Configuration self = this;
				
		builder.add(ClientConfig.class, new Provider<ClientConfig>(){
			public ClientConfig get() {
				return self.clientConfig;
			}});

		builder.add(Client.class, new Provider<Client>(){
			public Client get() {
				ClientConfig clientConfig = builder.build(ClientConfig.class);
				return Client.create(clientConfig);
			}});
		
		return builder;
	}
	

	public <T> T build(Class<T> c) throws Exception {
		return builder.build(c);
	}
	
	public Builder getBuilder() {
		return builder;
	}

	public void setBuilder(Builder builder) {
		this.builder = builder;
	}
    
}
