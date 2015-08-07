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
import com.microsoft.azure.utility.ConsumerWrapper;
import com.microsoft.azure.utility.ResourceContext;
import org.apache.commons.logging.LogFactory;
import org.junit.*;

public class VMDiskSizeTests extends ComputeTestBase{
    static {
        log = LogFactory.getLog(VMDiskSizeTests.class);
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
    public void testVMDiskSizeScenario() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        ResourceContext context = createTestResourceContext(false);

        VirtualMachine vm = createVM(context, generateName("VM"), new ConsumerWrapper<VirtualMachine>() {
            @Override
            public void accept(VirtualMachine virtualMachine) {
                virtualMachine.getStorageProfile().getOSDisk().setDiskSizeGB(100);
            }
        });
        VirtualMachine vmInput = context.getVMInput();

        log.info("created VM: " + vm.getName());
        VirtualMachineGetResponse vmResponse = computeManagementClient.getVirtualMachinesOperations()
                .get(m_rgName, vmInput.getName());
        validateVM(vmInput, vmResponse.getVirtualMachine());
    }
}
