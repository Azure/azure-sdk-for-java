package com.microsoft.azure.services.serviceBus.contract;

import java.util.Map;

import com.microsoft.azure.configuration.builder.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class Exports implements Builder.Exports {
	public void register(Builder.Registry registry) {
		
		// provide contract implementation
		registry.add(ServiceBusContract.class, ServiceBusContractImpl.class);

		// alter jersey client config for serviceBus
		registry.alter(ClientConfig.class, new Builder.Alteration<ClientConfig>() {

			public ClientConfig alter(ClientConfig instance, Builder builder,
					Map<String, Object> properties) throws Exception {
				
				// enable this feature for unattributed json object serialization
				instance.getProperties().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
				
				// need to avoid certain element prefixes, which the service does not ignore
				instance.getSingletons().add(new MarshallerProvider());
				
				return instance;
			}
		});

		// convenience provider to transform BrokerProperty headers to json
		registry.add(BrokerPropertiesMapper.class);
		
	}
}
