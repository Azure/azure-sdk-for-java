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

package com.microsoft.azure.utility.compute;

import com.microsoft.azure.management.compute.models.*;
import com.microsoft.azure.utility.ResourceContext;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.*;

public class VMScenarioTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMScenarioTests.class);
    }

    @BeforeClass
    public static void setup() throws Exception {
        ensureClientsInitialized();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        log.debug("after class, clean resource group: " + m_rgName);
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
    public void testVMScenarioOperations() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        ResourceContext context = createTestResourceContext(false);

        VirtualMachine vm = createVM(context, generateName("VM"));
        VirtualMachine vmInput = context.getVMInput();

        log.info("created VM: " + vm.getName());
        VirtualMachineGetResponse vmInstanceResponse = computeManagementClient.getVirtualMachinesOperations()
                .getWithInstanceView(m_rgName, vmInput.getName());
        validateVMInstanceView(vmInput, vmInstanceResponse.getVirtualMachine());

        log.info("test list VM: ");
        VirtualMachineListResponse listResponse = computeManagementClient.getVirtualMachinesOperations().list(m_rgName);
        Assert.assertEquals(HttpStatus.SC_OK, listResponse.getStatusCode());
        VirtualMachine vmFromList = null;
        for (VirtualMachine v : listResponse.getVirtualMachines()) {
            if (v.getName().equals(vm.getName())) {
                vmFromList = v;
            }
        }
        Assert.assertNotNull(vmFromList);
        log.info("test list VM response: " + vm.getName());
        validateVM(vmInput, vmFromList);

        log.info("test vm listAvailableSizes");
        VirtualMachineSizeListResponse listVMSizesResponse = computeManagementClient.getVirtualMachinesOperations()
                .listAvailableSizes(m_rgName, vmInput.getName());
        Assert.assertEquals(HttpStatus.SC_OK, listVMSizesResponse.getStatusCode());
        ComputeTestHelper.validateVirtualMachineSizeListResponse(listVMSizesResponse);

        log.info("test as listAvailableSizes");
        VirtualMachineSizeListResponse listVMSizesResponse2 = computeManagementClient.getAvailabilitySetsOperations()
                .listAvailableSizes(m_rgName, context.getAvailabilitySetName());
        Assert.assertEquals(HttpStatus.SC_OK, listVMSizesResponse2.getStatusCode());
        ComputeTestHelper.validateVirtualMachineSizeListResponse(listVMSizesResponse2);

        log.info("test delete VM");
        //only test the delete request. Leave the LongRunningOperation check to VMOperational tests
        ComputeOperationResponse deleteResponse = computeManagementClient.getVirtualMachinesOperations()
                .beginDeleting(m_rgName, vm.getName());
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, deleteResponse.getStatusCode());
    }
}
