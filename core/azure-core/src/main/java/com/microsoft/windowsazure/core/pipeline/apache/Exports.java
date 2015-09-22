/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.core.pipeline.apache;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.microsoft.windowsazure.credentials.CloudCredentials;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Exports implements Builder.Exports {

    @Override
    public void register(Registry registry) {
        registry.add(new Builder.Factory<ExecutorService>() {
            @Override
            public <S> ExecutorService create(String profile, Class<S> service,
                                              Builder builder, Map<String, Object> properties) {

                return Executors.newCachedThreadPool();
            }
        });

        registry.add(new Builder.Factory<ApacheConfigSettings>() {
            @Override
            public <S> ApacheConfigSettings create(String profile,
                                                   Class<S> service, Builder builder,
                                                   Map<String, Object> properties) {

                if (!ManagementConfiguration.isPlayback() &&
                        properties.containsKey(ManagementConfiguration.SUBSCRIPTION_CLOUD_CREDENTIALS)) {
                    CloudCredentials cloudCredentials = (CloudCredentials) properties
                            .get(ManagementConfiguration.SUBSCRIPTION_CLOUD_CREDENTIALS);
                    cloudCredentials.applyConfig(profile, properties);
                }

                return new ApacheConfigSettings(profile, properties);
            }
        });

        registry.add(new Builder.Factory<HttpClientBuilder>() {
            @Override
            public <S> HttpClientBuilder create(String profile,
                                                Class<S> service, Builder builder,
                                                Map<String, Object> properties) {

                HttpClientBuilder httpClientBuilder = HttpClients.custom();
                ApacheConfigSettings settings = builder.build(profile, service,
                        ApacheConfigSettings.class, properties);

                return settings.applyConfig(httpClientBuilder);
            }
        });
    }
}
