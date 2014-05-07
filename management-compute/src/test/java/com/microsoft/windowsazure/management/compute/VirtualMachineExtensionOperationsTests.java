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

package com.microsoft.windowsazure.management.compute;

import java.util.ArrayList;

import com.microsoft.windowsazure.management.compute.models.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class VirtualMachineExtensionOperationsTests extends ComputeManagementIntegrationTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createComputeManagementClient();
    }

    @Test
    public void listVirtualMachineExtensionSuccess() throws Exception {        
        VirtualMachineExtensionListResponse virtualMachineExtensionListResponse = computeManagementClient.getVirtualMachineExtensionsOperations().list();
        ArrayList<VirtualMachineExtensionListResponse.ResourceExtension> virtualMachineExtensionResourceExtensionlist = virtualMachineExtensionListResponse.getResourceExtensions();
        Assert.assertNotNull(virtualMachineExtensionResourceExtensionlist);
        for (VirtualMachineExtensionListResponse.ResourceExtension resourceExtension : virtualMachineExtensionResourceExtensionlist)
        {
            Assert.assertNull(resourceExtension.getSampleConfig());
        }
    }

    @Test
    public void listVirtualMachineExtensionVersionSuccess() throws Exception {
        //Arrange
        String publisherName = "BGInfo";
        String extensionName = "BGInfo";
        //Act
        VirtualMachineExtensionListResponse virtualMachineExtensionListResponse = computeManagementClient.getVirtualMachineExtensionsOperations().listVersions(publisherName, extensionName);
        ArrayList<VirtualMachineExtensionListResponse.ResourceExtension> virtualMachineExtensionResourceExtensionlist = virtualMachineExtensionListResponse.getResourceExtensions();
        Assert.assertNotNull(virtualMachineExtensionResourceExtensionlist);
   }
}