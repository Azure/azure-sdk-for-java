/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility.compute;

import com.microsoft.azure.management.compute.models.*;
import com.microsoft.azure.utility.ComputeHelper;
import org.apache.commons.logging.LogFactory;
import org.junit.*;
import java.util.function.Predicate;

public class VMImagesTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMImagesTests.class);
    }

    private static final String[] AvailableWindowsServerImageVersions = new String[] {
        "4.0.201506", "4.0.201505", "4.0.201504"
    };

    private VirtualMachineImageGetParameters parameters;
    private VirtualMachineImageListParameters listParameters;

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
//        log.debug("after class, clean resource group: " + rgName);
//        cleanupResourceGroup();
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
        log.info(String.format("start test, in  %s, mock: %s", m_location, IS_MOCKED));

        // setup image params
        ImageReference imageRef = ComputeHelper.getWindowsServerDefaultImage(computeManagementClient, m_location);
        parameters = new VirtualMachineImageGetParameters();
        parameters.setLocation(m_location);
        parameters.setPublisherName(imageRef.getPublisher());
        parameters.setOffer(imageRef.getOffer());
        parameters.setSkus(imageRef.getSku());
        parameters.setVersion(imageRef.getVersion());

        listParameters = new VirtualMachineImageListParameters();
        listParameters.setLocation(m_location);
        listParameters.setPublisherName(imageRef.getPublisher());
        listParameters.setOffer(imageRef.getOffer());
        listParameters.setSkus(imageRef.getSku());
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    @Test
    public void testVMImageGet() throws Exception {
        VirtualMachineImage vmImage = computeManagementClient.getVirtualMachineImagesOperations()
                .get(parameters).getVirtualMachineImage();

        Assert.assertEquals("image version equal", parameters.getVersion(), vmImage.getName());
        Assert.assertTrue("image location equal", parameters.getLocation().equalsIgnoreCase(vmImage.getLocation()));
        Assert.assertTrue("OS is windows", vmImage.getOSDiskImage().getOperatingSystem().equalsIgnoreCase("Windows"));
    }

    @Test
    public void testVMImageListNoFilter() throws Exception {
        VirtualMachineImageResourceList vmImages = computeManagementClient.getVirtualMachineImagesOperations()
                .list(listParameters);

        Assert.assertTrue("image count", vmImages.getResources().size() > 0);
    }

    @Test
    public void testVMImageListWithFilters() throws Exception {
        log.info("Filter: top - Negative Test");
        listParameters.setFilterExpression("$top=0");
        VirtualMachineImageResourceList vmImages = computeManagementClient.getVirtualMachineImagesOperations()
                .list(listParameters);
        Assert.assertTrue("image count", vmImages.getResources().size() == 0);

        log.info("Filter: top - Positive Test");
        listParameters.setFilterExpression("$top=1");
        vmImages = computeManagementClient.getVirtualMachineImagesOperations().list(listParameters);
        Assert.assertTrue("image count", vmImages.getResources().size() == 1);

        log.info("Filter: top - Positive Test 2");
        listParameters.setFilterExpression("$top=2");
        vmImages = computeManagementClient.getVirtualMachineImagesOperations().list(listParameters);
        Assert.assertTrue("image count", vmImages.getResources().size() == 2);

        log.info("Filter: orderby - Positive Test");
        listParameters.setFilterExpression("$orderby=name desc");
        vmImages = computeManagementClient.getVirtualMachineImagesOperations().list(listParameters);
        Assert.assertTrue("image count", vmImages.getResources().size() > 0);

        log.info("Filter: orderby - Positive Test 2");
        listParameters.setFilterExpression("$top=2&$orderby=name asc");
        vmImages = computeManagementClient.getVirtualMachineImagesOperations().list(listParameters);
        Assert.assertTrue("image count", vmImages.getResources().size() == 2);

        log.info("Filter: orderby - Positive Test desc");
        listParameters.setFilterExpression("$top=2&$orderby=name desc");
        VirtualMachineImageResourceList vmImages2 = computeManagementClient.getVirtualMachineImagesOperations()
                .list(listParameters);
        Assert.assertTrue("image count", vmImages2.getResources().size() == 2);
        Assert.assertNotEquals("desc vs asc top result should not match",
                vmImages.getResources().get(0).getName(), vmImages2.getResources().get(0).getName());
    }

    @Test
    public void testVMImageListPublisher() throws Exception {
        VirtualMachineImageResourceList vmImages = computeManagementClient.getVirtualMachineImagesOperations()
                .listPublishers(parameters);

        Assert.assertTrue("image count", vmImages.getResources().size() > 0);
        Assert.assertTrue("list publishers contain param publishers",
                vmImages.getResources().stream().anyMatch(new Predicate<VirtualMachineImageResource>() {
                    @Override
                    public boolean test(VirtualMachineImageResource virtualMachineImageResource) {
                        return virtualMachineImageResource.getName().equalsIgnoreCase(parameters.getPublisherName());
                    }
        }));
    }

    @Test
    public void testVMImageListOffers() throws Exception {
        VirtualMachineImageResourceList vmImages = computeManagementClient.getVirtualMachineImagesOperations()
                .listOffers(parameters);

        Assert.assertTrue("image count", vmImages.getResources().size() > 0);
        Assert.assertTrue("list offers contain param offer",
                vmImages.getResources().stream().anyMatch(new Predicate<VirtualMachineImageResource>() {
                    @Override
                    public boolean test(VirtualMachineImageResource virtualMachineImageResource) {
                        return virtualMachineImageResource.getName().equalsIgnoreCase(parameters.getOffer());
                    }
                }));
    }

    @Test
    public void testVMImageListSkus() throws Exception {
        VirtualMachineImageResourceList vmImages = computeManagementClient.getVirtualMachineImagesOperations()
                .listSkus(parameters);

        Assert.assertTrue("image count", vmImages.getResources().size() > 0);
        Assert.assertTrue("list offers contain param offer",
                vmImages.getResources().stream().anyMatch(new Predicate<VirtualMachineImageResource>() {
                    @Override
                    public boolean test(VirtualMachineImageResource virtualMachineImageResource) {
                        return virtualMachineImageResource.getName().equalsIgnoreCase(parameters.getSkus());
                    }
                }));
    }
}
