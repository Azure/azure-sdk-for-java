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
package com.microsoft.windowsazure.services.management;

import java.util.Map;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.core.UserAgentFilter;
import com.microsoft.windowsazure.services.management.implementation.ManagementExceptionProcessor;
import com.microsoft.windowsazure.services.serviceBus.implementation.BrokerPropertiesMapper;
import com.sun.jersey.api.client.config.ClientConfig;

public class Exports implements Builder.Exports {
    @Override
    public void register(Builder.Registry registry) {

        // provide contract implementation
        registry.add(ManagementContract.class, ManagementExceptionProcessor.class);
        registry.add(UserAgentFilter.class);

        // alter jersey client config for serviceBus
        registry.alter(ClientConfig.class, new Builder.Alteration<ClientConfig>() {

            @Override
            public ClientConfig alter(ClientConfig instance, Builder builder, Map<String, Object> properties) {

                // need to avoid certain element prefixes, which the service does not ignore

                // add body reader/writer for EntryModel<?> descendant classes

                return instance;
            }
        });

        // convenience provider to transform BrokerProperty headers to json
        registry.add(BrokerPropertiesMapper.class);

    }
}
