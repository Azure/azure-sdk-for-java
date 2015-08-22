//
// Copyright (c) Microsoft.  All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.microsoft.azure.utility.compute;

import com.microsoft.azure.management.compute.models.*;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

import java.util.ArrayList;

public class VMExtensionImageTest extends ComputeTestBase {
    private static String vmExtensionImageVersion = "2.0";
    private static String vmExtensionImageType = "VMAccessAgent";

    static {
        log = LogFactory.getLog(VMExtensionImageTest.class);
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    private static VirtualMachineExtensionImageGetParameters parameters;

    static {
        parameters = new VirtualMachineExtensionImageGetParameters();
        parameters.setLocation(m_location);
        parameters.setPublisherName("Microsoft.Compute");
        parameters.setType(vmExtensionImageType);
        parameters.setVersion(vmExtensionImageVersion);
    }

    @Test
    public void testExtensionImageGet() throws Exception {
        VirtualMachineExtensionImageGetResponse vmExtensionImageResponse =
                computeManagementClient.getVirtualMachineExtensionImagesOperations().get(parameters);
        VirtualMachineExtensionImage extension = vmExtensionImageResponse.getVirtualMachineExtensionImage();
        Assert.assertEquals(vmExtensionImageVersion, extension.getName());
        Assert.assertEquals(m_location.toLowerCase(), extension.getLocation().toLowerCase());
        Assert.assertEquals("Windows", extension.getOperatingSystem());
        Assert.assertEquals("IaaS", extension.getComputeRole());

        Assert.assertEquals(false, extension.isVMScaleSetEnabled());
        Assert.assertEquals(false, extension.isSupportsMultipleExtensions());
    }

    @Test
    public void testExtensionImageListTypes() throws Exception {
        VirtualMachineImageResourceList vmExtensionImgList =
                computeManagementClient.getVirtualMachineExtensionImagesOperations().listTypes(parameters);

        Assert.assertTrue(vmExtensionImgList.getResources().size() > 0);
        Assert.assertTrue(countVMExtensionImage(vmExtensionImgList, vmExtensionImageType) > 0);
    }

    @Test
    public void testExtensionImageListVersionsNoFilter() throws Exception {
        VirtualMachineImageResourceList vmExtensionImgList =
                computeManagementClient.getVirtualMachineExtensionImagesOperations().listVersions(parameters);

        Assert.assertTrue(vmExtensionImgList.getResources().size() > 0);
        Assert.assertTrue(countVMExtensionImage(vmExtensionImgList, vmExtensionImageVersion) > 0);
    }

    @Test
    public void TestExtImgListVersionsFilters() throws Exception {
        VirtualMachineExtensionImageListVersionsParameters listVersionsParamers =
                new VirtualMachineExtensionImageListVersionsParameters();
        listVersionsParamers.setLocation(parameters.getLocation());
        listVersionsParamers.setType(parameters.getType());
        listVersionsParamers.setPublisherName(parameters.getPublisherName());

        // Filter: startswith - Positive Test
        listVersionsParamers.setFilterExpression(
                String.format("$filter=startswith(name,'%s')", vmExtensionImageVersion));

        VirtualMachineImageResourceList vmExtensionImgList =
                computeManagementClient.getVirtualMachineExtensionImagesOperations().listVersions(listVersionsParamers);
        Assert.assertTrue(vmExtensionImgList.getResources().size() > 0);
        Assert.assertTrue(countVMExtensionImage(vmExtensionImgList, vmExtensionImageVersion) != 0);

        // Filter: startswith - Negative Test
        listVersionsParamers.setFilterExpression("$filter=startswith(name,'1.0')");
        vmExtensionImgList =
                computeManagementClient.getVirtualMachineExtensionImagesOperations().listVersions(listVersionsParamers);
        Assert.assertTrue(vmExtensionImgList.getResources().size() == 0);
        Assert.assertTrue(countVMExtensionImage(vmExtensionImgList, vmExtensionImageVersion) == 0);

        // Filter: top - Positive Test
        listVersionsParamers.setFilterExpression("$top=1");
        vmExtensionImgList =
                computeManagementClient.getVirtualMachineExtensionImagesOperations().listVersions(listVersionsParamers);
        Assert.assertTrue(vmExtensionImgList.getResources().size() == 1);
        Assert.assertTrue(countVMExtensionImage(vmExtensionImgList, vmExtensionImageVersion) != 0);

        // Filter: top - Negative Test
        listVersionsParamers.setFilterExpression("$top=0");
        vmExtensionImgList =
                computeManagementClient.getVirtualMachineExtensionImagesOperations().listVersions(listVersionsParamers);
        Assert.assertTrue(vmExtensionImgList.getResources().size() == 0);
    }

    private int countVMExtensionImage(VirtualMachineImageResourceList vmExtensionImgList, String vmExtensionImageType) {
        int cnt = 0;
        ArrayList<VirtualMachineImageResource> list = vmExtensionImgList.getResources();
        for (VirtualMachineImageResource resource : list) {
            if (resource.getName().equals(vmExtensionImageType)) {
                cnt++;
            }
        }
        return cnt;
    }
}
