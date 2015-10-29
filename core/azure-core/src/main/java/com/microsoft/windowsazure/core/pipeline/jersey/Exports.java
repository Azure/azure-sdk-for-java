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
package com.microsoft.windowsazure.core.pipeline.jersey;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import java.util.Map;

import static com.microsoft.windowsazure.core.utils.ExportUtils.getPropertyIfExists;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

public class Exports implements Builder.Exports {

    @Override
    public void register(Registry registry) {
        registry.add(new Builder.Factory<ClientConfig>() {
            @Override
            public <S> ClientConfig create(String profile, Class<S> service,
                    Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = new DefaultClientConfig();
                ClientConfigSettings settings = builder.build(profile, service,
                        ClientConfigSettings.class, properties);
                settings.applyConfig(clientConfig);
                return clientConfig;
            }
        });

        registry.add(new Builder.Factory<ClientConfigSettings>() {

            @Override
            public <S> ClientConfigSettings create(String profile,
                    Class<S> service, Builder builder,
                    Map<String, Object> properties) {
                Object connectTimeout = getPropertyIfExists(profile,
                        properties, Configuration.PROPERTY_CONNECT_TIMEOUT);
                Object readTimeout = getPropertyIfExists(profile, properties,
                        Configuration.PROPERTY_READ_TIMEOUT);

                return new ClientConfigSettings(
                        connectTimeout,
                        readTimeout,
                        getPropertyIfExists(profile, properties,
                                Configuration.PROPERTY_LOG_HTTP_REQUESTS) != null);
            }

        });

        registry.add(new Builder.Factory<Client>() {
            @Override
            public <S> Client create(String profile, Class<S> service,
                    Builder builder, Map<String, Object> properties) {
                ClientConfig clientConfig = builder.build(profile, service,
                        ClientConfig.class, properties);
                ClientConfigSettings settings = builder.build(profile, service,
                        ClientConfigSettings.class, properties);
                Client client;
                String proxyHost = (String) getPropertyIfExists(profile,
                        properties, Configuration.PROPERTY_HTTP_PROXY_HOST);
                Integer proxyPort = (Integer) getPropertyIfExists(profile,
                        properties, Configuration.PROPERTY_HTTP_PROXY_PORT);
                if ((proxyHost != null) && (proxyPort != null)) {
                    URLConnectionClientHandler clientHandler = new URLConnectionClientHandler(
                        new ProxyConnectionFactory(proxyHost, proxyPort));
                    client = new Client(clientHandler, clientConfig);
                } else {
                    client = Client.create(clientConfig);
                }
                settings.applyConfig(client);
                return client;
            }
        });

        registry.add(new Builder.Factory<HttpURLConnectionClient>() {
            @Override
            public <S> HttpURLConnectionClient create(String profile,
                    Class<S> service, Builder builder,
                    Map<String, Object> properties) {
                ClientConfig clientConfig = builder.build(profile, service,
                        ClientConfig.class, properties);
                ClientConfigSettings settings = builder.build(profile, service,
                        ClientConfigSettings.class, properties);
                HttpURLConnectionClient client = HttpURLConnectionClient
                        .create(clientConfig);
                settings.applyConfig(client);
                return client;
            }
        });
    }
}
