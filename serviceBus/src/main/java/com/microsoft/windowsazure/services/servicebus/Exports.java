/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.servicebus;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.UserAgentFilter;
import java.util.Map;

import com.microsoft.windowsazure.services.servicebus.implementation.BrokerPropertiesMapper;
import com.microsoft.windowsazure.services.servicebus.implementation.EntryModelProvider;
import com.microsoft.windowsazure.services.servicebus.implementation.MarshallerProvider;
import com.microsoft.windowsazure.services.servicebus.implementation.ServiceBusExceptionProcessor;
import com.microsoft.windowsazure.services.servicebus.implementation.ServiceBusRestProxy;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class Exports implements Builder.Exports {
    @Override
    public void register(Builder.Registry registry) {

        // provide contract implementation
        registry.add(ServiceBusContract.class,
                ServiceBusExceptionProcessor.class);
        registry.add(ServiceBusExceptionProcessor.class);
        registry.add(ServiceBusRestProxy.class);
        registry.add(UserAgentFilter.class);

        // alter jersey client config for serviceBus
        registry.alter(ServiceBusContract.class, ClientConfig.class,
                new Builder.Alteration<ClientConfig>() {

                    @Override
                    public ClientConfig alter(String profile,
                            ClientConfig instance, Builder builder,
                            Map<String, Object> properties) {

                        // enable this feature for unattributed json object
                        // serialization
                        instance.getProperties().put(
                                JSONConfiguration.FEATURE_POJO_MAPPING, true);

                        // need to avoid certain element prefixes, which the
                        // service does not ignore
                        instance.getSingletons().add(new MarshallerProvider());

                        // add body reader/writer for EntryModel<?> descendant
                        // classes
                        instance.getClasses().add(EntryModelProvider.class);

                        return instance;
                    }
                });

        // convenience provider to transform BrokerProperty headers to json
        registry.add(BrokerPropertiesMapper.class);

    }
}
