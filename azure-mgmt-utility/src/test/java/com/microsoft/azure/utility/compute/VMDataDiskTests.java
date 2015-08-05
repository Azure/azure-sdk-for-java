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
import com.microsoft.azure.utility.ConsumerWrapper;
import com.microsoft.azure.utility.ResourceContext;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.*;

import java.util.ArrayList;

public class VMDataDiskTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMDataDiskTests.class);
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        log.debug("after class, clean resource group: " + rgName);
        cleanupResourceGroup();
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }

    @Test
    public void testVMDataDiskScenario() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        final ResourceContext context = createTestResourceContext(false);
        context.setImageReference(
                ComputeHelper.getWindowsServerDefaultImage(computeManagementClient, context.getLocation()));

        VirtualMachine vm = createVM(context, generateName("VM"), new ConsumerWrapper<VirtualMachine>() {
            @Override
            public void accept(VirtualMachine virtualMachine) {
                String vhdContainer = ComputeHelper.getVhdContainerUrl(context);
                virtualMachine.getHardwareProfile().setVirtualMachineSize(VirtualMachineSizeTypes.STANDARD_A4);
                virtualMachine.getStorageProfile().setDataDisks(new ArrayList<DataDisk>(2));
                for (int i = 1; i < 3; i++) {
                    String diskName = String.format("%s%s", "dataDisk", i);
                    String ddUri = String.format("%s/%s.vhd", vhdContainer, diskName);
                    log.info("using dd uri: " + ddUri);
                    DataDisk dd = new DataDisk();
                    dd.setCaching(CachingTypes.NONE);
                    dd.setSourceImage(null);
                    dd.setDiskSizeGB(10);
                    dd.setCreateOption(DiskCreateOptionTypes.EMPTY);
                    dd.setLun(i + 1);
                    dd.setName(diskName);
                    VirtualHardDisk vhd = new VirtualHardDisk();
                    vhd.setUri(ddUri);
                    dd.setVirtualHardDisk(vhd);

                    virtualMachine.getStorageProfile().getDataDisks().add(dd);
                }
            }
        });
        VirtualMachine vmInput = context.getVMInput();

        log.info("created VM: " + vm.getName());
        VirtualMachineGetResponse getVMWithInstanceViewResponse = computeManagementClient
                .getVirtualMachinesOperations().getWithInstanceView(context.getResourceGroupName(), vmInput.getName());
        Assert.assertEquals("statusCode should be ok", HttpStatus.SC_OK, getVMWithInstanceViewResponse.getStatusCode());
        Assert.assertNotNull("get vm not null", getVMWithInstanceViewResponse.getVirtualMachine());
        validateVMInstanceView(vmInput, getVMWithInstanceViewResponse.getVirtualMachine());

        log.info("re-create same vm");
        VirtualMachine vm2 = getVMWithInstanceViewResponse.getVirtualMachine();
        ComputeLongRunningOperationResponse vmReCreateResponse = computeManagementClient.getVirtualMachinesOperations()
                .createOrUpdate(context.getResourceGroupName(), vm2);
        Assert.assertNotEquals("status should not be failed",
                ComputeOperationStatus.FAILED, vmReCreateResponse.getStatus());

        log.info("delete vm");
        DeleteOperationResponse vmDeleteResponse = computeManagementClient.getVirtualMachinesOperations()
                .delete(context.getResourceGroupName(), vm2.getName());
        Assert.assertNotEquals("status should not be failed",
                ComputeOperationStatus.FAILED, vmDeleteResponse.getStatus());
    }
}
