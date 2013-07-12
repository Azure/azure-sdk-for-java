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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.core.Builder.Alteration;
import com.microsoft.windowsazure.services.core.Builder.Registry;
import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public abstract class IntegrationTestBase {

    protected ManagementContract service;

    @BeforeClass
    public static void initializeSystem() {
        System.setProperty("http.keepAlive", "false");
    }

    @Before
    public void initialize() throws Exception {
        createService();
        removeEntities();
    }

    private void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Registry builder = (Registry) config.getBuilder();
        builder.alter(Client.class, new Alteration<Client>() {
            @Override
            public Client alter(String profile, Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        // applied as default configuration 
        Configuration.setInstance(config);
        service = ManagementService.create(config);
    }

    private void removeEntities() {
        ListResult<AffinityGroupInfo> listAffinityGroupResult = null;
        try {
            listAffinityGroupResult = service.listAffinityGroups();
        }
        catch (ServiceException e) {
        }

        for (AffinityGroupInfo affinityGroupInfo : listAffinityGroupResult) {
            try {
                String affinityGroupName = affinityGroupInfo.getName();
                if ((affinityGroupName != null) && (affinityGroupName.startsWith("test"))) {
                    service.deleteAffinityGroup(affinityGroupInfo.getName());
                }
            }
            catch (ServiceException e) {
            }
        }

    }

    @AfterClass
    public static void cleanUpTestArtifacts() throws Exception {
        // Configuration config = createConfiguration();
    }

    protected static Configuration createConfiguration() throws Exception {
        Configuration config = Configuration.load();

        overrideWithEnv(config, ManagementConfiguration.URI);
        overrideWithEnv(config, ManagementConfiguration.SUBSCRIPTION_ID);
        overrideWithEnv(config, ManagementConfiguration.KEYSTORE_PASSWORD);
        overrideWithEnv(config, ManagementConfiguration.KEYSTORE_PATH);

        return config;
    }

    private static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }
}
