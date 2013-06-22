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
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.GetAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupResult;
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
            public Client alter(Client client, Builder builder, Map<String, Object> properties) {
                client.addFilter(new LoggingFilter());
                return client;
            }
        });

        // applied as default configuration 
        Configuration.setInstance(config);
        service = ManagementService.create();
    }

    @Test
    public void createAffinityGroupSuccess() throws Exception {
        // Arrange
        String expectedAffinityGroupName = "testCreateAffinityGroupSuccess";
        String expectedLabel = Base64.encode("testCreateAffinityGroupSuccess".getBytes("UTF-8"));
        String expectedLocation = "US West";

        // Act
        CreateAffinityGroupResult createAffinityGroupResult = service.createAffinityGroup(expectedAffinityGroupName,
                expectedLabel, expectedLocation);
        AffinityGroupInfo affinityGroupInfo = createAffinityGroupResult.getValue();

        // Assert
        assertEquals(expectedAffinityGroupName, affinityGroupInfo.getName());
        assertEquals(expectedLabel, affinityGroupInfo.getLabel());
        assertEquals(expectedLocation, affinityGroupInfo.getLocation());

    }

    @Test
    public void createAffinityGroupWithOptionalParametersSuccess() throws Exception {
        // Arrange 
        String expectedAffinityGroupName = "testCreateAffinityGroupWithOptionalParametersSuccess";
        String expectedLabel = Base64.encode("testCreateAffinityGroupWithOptionalParameterSuccess".getBytes("UTF-8"));
        String expectedLocation = "US West";
        String expectedDescription = "testCreateAffinityGroupWithOptionalParameterSuccessDescription";
        CreateAffinityGroupOptions createAffinityGroupOptions = new CreateAffinityGroupOptions()
                .setDescription(expectedDescription);

        // Act 
        CreateAffinityGroupResult createAffinityGroupResult = service.createAffinityGroup(expectedAffinityGroupName,
                expectedLabel, expectedLocation, createAffinityGroupOptions);
        AffinityGroupInfo affinityGroupInfo = createAffinityGroupResult.getValue();

        // Assert
        assertEquals(expectedAffinityGroupName, affinityGroupInfo.getName());
        assertEquals(expectedLabel, affinityGroupInfo.getLabel());
        assertEquals(expectedDescription, affinityGroupInfo.getDescription());
        assertEquals(expectedLocation, affinityGroupInfo.getLocation());

    }

    @Test
    public void listAffinityGroupsSuccess() throws ServiceException {
        // Arrange

        // Act 
        ListResult<AffinityGroupInfo> listAffinityGroupsResult = service.listAffinityGroups();

        // Assert
        assertNotNull(listAffinityGroupsResult);

    }

    @Test
    public void deleteAffinityGroupSuccess() throws ServiceException, Exception {
        // Arrange 
        String affinityGroupName = "testDeleteAffinityGroupSuccess";
        String label = Base64.encode("testDeleteAffinityGroupSuccesslabel".getBytes("UTF-8"));
        String location = "West US";
        service.createAffinityGroup(affinityGroupName, label, location);

        // Act
        service.deleteAffinityGroup(affinityGroupName);

        // Assert 

    }

    @Test
    public void updateAffinityGroupSuccess() throws Exception {
        // Arrange 
        String expectedAffinityGroupName = "testUpdateAffinityGroupSuccess";
        String expectedAffinityGroupLabel = Base64.encode("testUpdateAffinityGroupSuccess".getBytes("UTF-8"));
        String expectedLocation = "US West";
        String expectedDescription = "updateAffinityGroupSuccess";
        service.createAffinityGroup(expectedAffinityGroupName, expectedAffinityGroupLabel, expectedLocation);
        UpdateAffinityGroupOptions updateAffinityGroupOptions = new UpdateAffinityGroupOptions()
                .setDescription(expectedDescription);

        // Act 
        UpdateAffinityGroupResult updateAffinityGroupResult = service.updateAffinityGroup(expectedAffinityGroupLabel,
                expectedDescription, updateAffinityGroupOptions);
        AffinityGroupInfo affinityGroupInfo = updateAffinityGroupResult.getValue();

        // Assert 
        assertEquals(expectedDescription, affinityGroupInfo.getDescription());

    }

    @Test
    public void getAffinityGroupPropertiesSuccess() throws Exception {
        // Arrange
        String expectedAffinityGroupName = "testGetAffinityGroupPropertiesSuccess";
        String expectedAffinityGroupLabel = Base64.encode("testGetAffinityGroupPropertiesSuccess".getBytes("UTF-8"));
        String expectedLocation = "US West";
        service.createAffinityGroup(expectedAffinityGroupName, expectedAffinityGroupLabel, expectedLocation);

        // Act 
        GetAffinityGroupResult getAffinityGroupResult = service.getAffinityGroup(expectedAffinityGroupName);
        AffinityGroupInfo affinityGroupInfo = getAffinityGroupResult.getValue();

        // Assert
        assertEquals(expectedAffinityGroupName, affinityGroupInfo.getName());
    }

}
