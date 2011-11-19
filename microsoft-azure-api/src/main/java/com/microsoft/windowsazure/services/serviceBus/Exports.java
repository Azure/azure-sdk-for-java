package com.microsoft.windowsazure.services.serviceBus;

import java.util.Map;

import com.microsoft.windowsazure.common.Builder;
import com.microsoft.windowsazure.services.serviceBus.implementation.BrokerPropertiesMapper;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntryModelProvider;
import com.microsoft.windowsazure.services.serviceBus.implementation.MarshallerProvider;
import com.microsoft.windowsazure.services.serviceBus.implementation.ServiceBusExceptionProcessor;
import com.microsoft.windowsazure.services.serviceBus.implementation.ServiceBusRestProxy;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class Exports implements Builder.Exports {
    public void register(Builder.Registry registry) {

        // provide contract implementation
        registry.add(ServiceBusContract.class, ServiceBusExceptionProcessor.class);
        registry.add(ServiceBusService.class);
        registry.add(ServiceBusExceptionProcessor.class);
        registry.add(ServiceBusRestProxy.class);

        // alter jersey client config for serviceBus
        registry.alter(ClientConfig.class, new Builder.Alteration<ClientConfig>() {

            public ClientConfig alter(ClientConfig instance, Builder builder, Map<String, Object> properties) {

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
