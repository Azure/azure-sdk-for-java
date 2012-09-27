/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.utils.pipeline;

import java.util.Map;
import java.util.Map.Entry;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.core.Builder.Registry;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class Exports implements Builder.Exports {

    @Override
    public void register(Registry registry) {
        registry.add(new Builder.Factory<ClientConfig>() {
            @Override
            public ClientConfig create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = new DefaultClientConfig();
                // Lower levels of the stack assume timeouts are set. 
                // Set default timeout on clientConfig in case user
                // hasn't set it yet in their configuration

                clientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, new Integer(90 * 1000));
                clientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, new Integer(90 * 1000));

                for (Entry<String, Object> entry : properties.entrySet()) {
                    Object propertyValue = entry.getValue();

                    // ClientConfig requires instance of Integer to properly set
                    // timeouts, but config file will deliver strings. Special
                    // case these timeout properties and convert them to Integer
                    // if necessary.
                    if (entry.getKey().equals(ClientConfig.PROPERTY_CONNECT_TIMEOUT)
                            || entry.getKey().equals(ClientConfig.PROPERTY_READ_TIMEOUT)) {
                        if (propertyValue instanceof String) {
                            propertyValue = Integer.valueOf((String) propertyValue);
                        }
                    }
                    clientConfig.getProperties().put(entry.getKey(), propertyValue);
                }
                return clientConfig;
            }
        });

        registry.add(new Builder.Factory<Client>() {
            @Override
            public Client create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = builder.build(profile, ClientConfig.class, properties);
                Client client = Client.create(clientConfig);
                return client;
            }
        });

        registry.add(new Builder.Factory<HttpURLConnectionClient>() {
            @Override
            public HttpURLConnectionClient create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = builder.build(profile, ClientConfig.class, properties);
                HttpURLConnectionClient client = HttpURLConnectionClient.create(clientConfig);
                //client.addFilter(new LoggingFilter());
                return client;
            }
        });
    }
}
