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
package com.microsoft.windowsazure.management.website;

import java.util.Map;

import com.microsoft.windowsazure.management.configuration.*;
import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Alteration;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.microsoft.windowsazure.*;
import com.microsoft.windowsazure.management.websites.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public abstract class WebSiteManagementIntegrationTestBase {

    protected static WebSiteManagementClient webSiteManagementClient;

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Registry builder = (Registry) config.getBuilder();
        builder.alter(WebSiteManagementClient.class, Client.class, new Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        webSiteManagementClient = WebSiteManagementService.create(config);
    }

    protected static Configuration createConfiguration() throws Exception {
        return ManagementConfiguration.configure(
                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                System.getenv(ManagementConfiguration.KEYSTORE_PATH),
                System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD));
    }
}