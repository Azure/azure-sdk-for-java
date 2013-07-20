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

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.storage.utils.Base64;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.GetAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupResult;

public class ManagementIntegrationTest extends IntegrationTestBase {

    @Test
    public void createAffinityGroupSuccess() throws Exception {
        // Arrange
        String expectedAffinityGroupName = "testCreateAffinityGroupSuccess";
        String expectedLabel = Base64.encode("testCreateAffinityGroupSuccess".getBytes("UTF-8"));
        String expectedLocation = "West US";

        // Act
        CreateAffinityGroupResult createAffinityGroupResult = service.createAffinityGroup(expectedAffinityGroupName,
                expectedLabel, expectedLocation);

        AffinityGroupInfo affinityGroupInfo = service.getAffinityGroup(expectedAffinityGroupName).getValue();

        // Assert
        assertNotNull(createAffinityGroupResult.getLocation());
        assertNotNull(createAffinityGroupResult.getRegion());
        assertNotNull(createAffinityGroupResult.getServer());
        assertNotNull(createAffinityGroupResult.getDate());
        assertEquals(expectedAffinityGroupName, affinityGroupInfo.getName());
        assertEquals(expectedLabel, affinityGroupInfo.getLabel());
        assertEquals(expectedLocation, affinityGroupInfo.getLocation());

    }

    @Test
    public void createAffinityGroupWithOptionalParametersSuccess() throws Exception {
        // Arrange 
        String expectedAffinityGroupName = "testCreateAffinityGroupWithOptionalParametersSuccess";
        String expectedLabel = Base64.encode("testCreateAffinityGroupWithOptionalParameterSuccess".getBytes("UTF-8"));
        String expectedLocation = "West US";
        String expectedDescription = "testCreateAffinityGroupWithOptionalParameterSuccessDescription";
        CreateAffinityGroupOptions createAffinityGroupOptions = new CreateAffinityGroupOptions()
                .setDescription(expectedDescription);

        // Act 
        CreateAffinityGroupResult createAffinityGroupResult = service.createAffinityGroup(expectedAffinityGroupName,
                expectedLabel, expectedLocation, createAffinityGroupOptions);

        // Assert
        AffinityGroupInfo actualAffinityGroupInfo = service.getAffinityGroup(expectedAffinityGroupName).getValue();
        assertNotNull(createAffinityGroupResult.getLocation());
        assertNotNull(createAffinityGroupResult.getRegion());
        assertNotNull(createAffinityGroupResult.getServer());
        assertNotNull(createAffinityGroupResult.getDate());
        assertEquals(expectedDescription, actualAffinityGroupInfo.getDescription());

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
        String expectedLocation = "West US";
        String expectedDescription = "updateAffinityGroupSuccess";
        service.createAffinityGroup(expectedAffinityGroupName, expectedAffinityGroupLabel, expectedLocation);
        UpdateAffinityGroupOptions updateAffinityGroupOptions = new UpdateAffinityGroupOptions()
                .setDescription(expectedDescription);

        // Act 
        UpdateAffinityGroupResult updateAffinityGroupResult = service.updateAffinityGroup(expectedAffinityGroupName,
                expectedAffinityGroupLabel, updateAffinityGroupOptions);

        // Assert 
        assertNotNull(updateAffinityGroupResult.getRegion());
        assertNotNull(updateAffinityGroupResult.getDate());
        assertNotNull(updateAffinityGroupResult.getRequestId());

    }

    @Test
    public void getAffinityGroupPropertiesSuccess() throws Exception {
        // Arrange
        String expectedAffinityGroupName = "testGetAffinityGroupPropertiesSuccess";
        String expectedAffinityGroupLabel = Base64.encode("testGetAffinityGroupPropertiesSuccess".getBytes("UTF-8"));
        String expectedLocation = "West US";
        service.createAffinityGroup(expectedAffinityGroupName, expectedAffinityGroupLabel, expectedLocation);

        // Act 
        GetAffinityGroupResult getAffinityGroupResult = service.getAffinityGroup(expectedAffinityGroupName);
        AffinityGroupInfo affinityGroupInfo = getAffinityGroupResult.getValue();

        // Assert
        assertEquals(expectedAffinityGroupName, affinityGroupInfo.getName());
    }

}
