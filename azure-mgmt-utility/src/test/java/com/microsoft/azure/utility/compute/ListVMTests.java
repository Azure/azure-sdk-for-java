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
import com.microsoft.azure.utility.ResourceContext;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.*;

public class ListVMTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(ListVMTests.class);
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

    /**
     * Create two VMs in different resource group with same storage account and availability set.
     * @throws Exception
     */
    @Test
    public void testListVMInSubscription() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        final ResourceContext context = createTestResourceContext(false);
        context.setImageReference(
                ComputeHelper.getWindowsServerDefaultImage(computeManagementClient, context.getLocation()));

        VirtualMachine vm1 = createVM(context, generateName("VM"));

        ResourceContext context2 = createTestResourceContext("2", generateName("2rg"), false);
        context2.setStorageAccount(context.getStorageAccount());
        context2.setAvailabilitySetName(context.getAvailabilitySetName());
        // vm name length limit is 15 chars
        VirtualMachine vm2 = createVM(context2, generateName("2V"));

        log.info("list created VM");
        VirtualMachineListResponse listResponse = computeManagementClient.getVirtualMachinesOperations()
                .listAll(new ListParameters());
        Assert.assertEquals("status code ok", HttpStatus.SC_OK, listResponse.getStatusCode());
        Assert.assertTrue("vm count >= 2", listResponse.getVirtualMachines().size() >= 2);
        Assert.assertNull("nextLink should be null", listResponse.getNextLink());

        int vmCount = 0;
        for (VirtualMachine vm : listResponse.getVirtualMachines()) {
            if (vm.getName().equals(vm1.getName())) {
                validateVM(vm1, vm);
                vmCount++;
            } else if (vm.getName().equals(vm2.getName())) {
                validateVM(vm2, vm);
                vmCount++;
            }
        }
        Assert.assertTrue("VMs pass validation", 2 <= vmCount);
    }
}
