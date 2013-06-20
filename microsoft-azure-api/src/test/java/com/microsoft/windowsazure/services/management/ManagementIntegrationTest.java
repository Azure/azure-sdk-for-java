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

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.core.Builder.Alteration;
import com.microsoft.windowsazure.services.core.Builder.Registry;
import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class ManagementIntegrationTest extends IntegrationTestBase {

    private ManagementContract service;

    @Before
    public void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();

        // add LoggingFilter to any pipeline that is created
        Registry builder = (Registry) config.getBuilder();
        builder.alter(Client.class, new Alteration<Client>() {
            @Override
            public Client alter(Client instance, Builder builder, Map<String, Object> properties) {
                instance.addFilter(new LoggingFilter());
                return instance;
            }
        });

        // applied as default configuration 
        Configuration.setInstance(config);
        service = ManagementService.create();
    }

    @Test
    public void createAffinityGroupSuccess() {
        // Arrange
        String subscriptionId = "12345";

        // Act
        // service.createAffinityGroup(subscriptionId);

        // Assert
    }

    @Test
    public void listAffinityGroupsSuccess() throws ServiceException {
        // Arrange 
        String subscriptionId = "279b0675-cf67-467f-98f0-67ae31eb540f";

        // Act 
        ListResult<AffinityGroupInfo> result = service.listAffinityGroups(subscriptionId);

        // Assert
        assertNotNull(result);

    }
}
