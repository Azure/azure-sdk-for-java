/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

    public void register(Registry registry) {
        registry.add(new Builder.Factory<ClientConfig>() {
            public ClientConfig create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = new DefaultClientConfig();
                for (Entry<String, Object> entry : properties.entrySet()) {
                    clientConfig.getProperties().put(entry.getKey(), entry.getValue());
                }
                return clientConfig;
            }
        });

        registry.add(new Builder.Factory<Client>() {
            public Client create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = (ClientConfig) properties.get("ClientConfig");
                Client client = Client.create(clientConfig);
                return client;
            }
        });

        registry.add(new Builder.Factory<HttpURLConnectionClient>() {
            public HttpURLConnectionClient create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = (ClientConfig) properties.get("ClientConfig");
                HttpURLConnectionClient client = HttpURLConnectionClient.create(clientConfig);
                //client.addFilter(new LoggingFilter());
                return client;
            }
        });
    }
}
