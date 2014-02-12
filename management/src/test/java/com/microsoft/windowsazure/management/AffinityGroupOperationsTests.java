/*
 * Copyright Microsoft.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.models.AffinityGroupCreateParameters;
import com.microsoft.windowsazure.management.models.AffinityGroupGetResponse;
import com.microsoft.windowsazure.exception.ServiceException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AffinityGroupOperationsTests extends ManagementIntegrationTestBase {
    private static final String affinityGroupName1 = "af1";
    private static final String affinityGroupName2 = "af2";

    @BeforeClass
    public static void setup() throws Exception {
        createService();
        cleanup();
        AffinityGroupCreateParameters createParameters = new AffinityGroupCreateParameters();
        createParameters.setName(affinityGroupName1);
        createParameters.setLocation("West US");
        createParameters.setLabel("Great AF");

        managementClient.getAffinityGroupsOperations().create(createParameters);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        try {
            managementClient.getAffinityGroupsOperations().delete(
                    affinityGroupName1);
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        try {
            managementClient.getAffinityGroupsOperations().delete(
                    affinityGroupName2);
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createAffinityGroup() throws Exception {
        // Arrange
        AffinityGroupCreateParameters createParameters = new AffinityGroupCreateParameters();
        createParameters.setName(affinityGroupName2);
        createParameters.setLocation("West US");
        createParameters.setLabel("Great AF");

        // Act
        OperationResponse operationResponse = managementClient
                .getAffinityGroupsOperations().create(createParameters);

        // Assert
        Assert.assertEquals(201, operationResponse.getStatusCode());
        Assert.assertNotNull(operationResponse.getRequestId());
    }

    @Test
    public void getAffinityGroups() throws Exception {
        // Act
        AffinityGroupGetResponse affinityGroupResponse = managementClient
                .getAffinityGroupsOperations().get(affinityGroupName1);

        // Assert
        Assert.assertEquals(200, affinityGroupResponse.getStatusCode());
        Assert.assertNotNull(affinityGroupResponse.getRequestId());
        Assert.assertEquals(affinityGroupName1, affinityGroupResponse.getName());
    }
}