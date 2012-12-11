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
import com.microsoft.windowsazure.services.core.Configuration;
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
                TimeoutSettings timeoutSettings = builder.build(profile, TimeoutSettings.class, properties);
                timeoutSettings.applyTimeout(clientConfig);
                return clientConfig;
            }
        });

        registry.add(new Builder.Factory<TimeoutSettings>() {

            @Override
            public TimeoutSettings create(String profile, Builder builder, Map<String, Object> properties) {
                Object connectTimeout = null;
                Object readTimeout = null;

                profile = normalizeProfile(profile);

                for (Entry<String, Object> entry : properties.entrySet()) {
                    Object propertyValue = entry.getValue();
                    String propertyKey = entry.getKey();

                    if (propertyKey.equals(profile + Configuration.PROPERTY_CONNECT_TIMEOUT)) {
                        connectTimeout = propertyValue;
                    }
                    if (propertyKey.equals(profile + Configuration.PROPERTY_READ_TIMEOUT)) {
                        readTimeout = propertyValue;
                    }
                }

                return new TimeoutSettings(connectTimeout, readTimeout);
            }
        });

        registry.add(new Builder.Factory<Client>() {
            @Override
            public Client create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = builder.build(profile, ClientConfig.class, properties);
                Client client = Client.create(clientConfig);
                // client.addFilter(new LoggingFilter());
                return client;
            }
        });

        registry.add(new Builder.Factory<HttpURLConnectionClient>() {
            @Override
            public HttpURLConnectionClient create(String profile, Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = builder.build(profile, ClientConfig.class, properties);
                HttpURLConnectionClient client = HttpURLConnectionClient.create(clientConfig);
                // client.addFilter(new LoggingFilter());
                return client;
            }
        });
    }

    private static String normalizeProfile(String profile) {
        if (profile == null) {
            return "";
        }

        if (profile.endsWith(".")) {
            return profile;
        }

        return profile + ".";
    }
}
