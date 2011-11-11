package com.microsoft.azure.services.serviceBus;

import java.util.Map;

import com.microsoft.azure.configuration.builder.Builder;
import com.microsoft.azure.services.serviceBus.client.MessagingClient;
import com.microsoft.azure.services.serviceBus.implementation.BrokerPropertiesMapper;
import com.microsoft.azure.services.serviceBus.implementation.EntryModelProvider;
import com.microsoft.azure.services.serviceBus.implementation.MarshallerProvider;
import com.microsoft.azure.services.serviceBus.implementation.ServiceBusServiceForJersey;
import com.microsoft.azure.services.serviceBus.implementation.ServiceBusServiceImpl;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class Exports implements Builder.Exports {
	public void register(Builder.Registry registry) {
		
		// provide contract implementation
		registry.add(ServiceBusService.class, ServiceBusServiceImpl.class);
		registry.add(ServiceBusServiceForJersey.class);
		registry.add(MessagingClient.class);

		// alter jersey client config for serviceBus
		registry.alter(ClientConfig.class, new Builder.Alteration<ClientConfig>() {

			public ClientConfig alter(ClientConfig instance, Builder builder,
					Map<String, Object> properties) throws Exception {
				
				// enable this feature for unattributed json object serialization
				instance.getProperties().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
				
				// need to avoid certain element prefixes, which the service does not ignore
				instance.getSingletons().add(new MarshallerProvider());
				
				// add body reader/writer for EntryModel<?> descendant classes
				instance.getClasses().add(EntryModelProvider.class);
				
				return instance;
			}
		});

		// convenience provider to transform BrokerProperty headers to json
		registry.add(BrokerPropertiesMapper.class);
		
	}
}
