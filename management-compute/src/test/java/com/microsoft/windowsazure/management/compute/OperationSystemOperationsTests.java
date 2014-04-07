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

package com.microsoft.windowsazure.management.compute;

import java.util.ArrayList;

import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.management.compute.models.*;
import com.microsoft.windowsazure.tracing.CloudTracing;
import com.microsoft.windowsazure.exception.ServiceException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class OperationSystemOperationsTests extends ComputeManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createComputeManagementClient();
    }

    @Test
    public void listOperationSystemSuccess() throws Exception {
        //Arrange
        OperatingSystemListResponse  operatingSystemListResponse = computeManagementClient.getOperatingSystemsOperations().list();
        ArrayList<OperatingSystemListResponse.OperatingSystem> operatingSystemlist = operatingSystemListResponse.getOperatingSystems();
        Assert.assertNotNull(operatingSystemListResponse);

        for (OperatingSystemListResponse.OperatingSystem os: operatingSystemlist)
        {
            if (os.getFamilyLabel().contains("R2 SP1")==true){
                Assert.assertEquals(2, os.getFamily());
                Assert.assertEquals(false, os.isActive());
                Assert.assertEquals(false, os.isDefault());
            }
        }
    }

    @Test
    public void listOperationSystemFamiliesSuccess() throws Exception {
        //Arrange
        OperatingSystemListFamiliesResponse operatingSystemListFamiliesResponse = computeManagementClient.getOperatingSystemsOperations().listFamilies();
        ArrayList<OperatingSystemListFamiliesResponse.OperatingSystemFamily> operatingSystemFamilylist = operatingSystemListFamiliesResponse.getOperatingSystemFamilies();
        Assert.assertNotNull(operatingSystemFamilylist);

        for (OperatingSystemListFamiliesResponse.OperatingSystemFamily osf: operatingSystemFamilylist)
        {
            if (osf.getName() == 1){
                Assert.assertEquals("Windows Server 2008 SP2", osf.getLabel()); 
            }
        }
    }
}