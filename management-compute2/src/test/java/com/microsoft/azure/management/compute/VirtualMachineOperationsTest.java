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

package com.microsoft.azure.management.compute;

import com.microsoft.azure.ComputeManagementIntegrationTestBase;
import com.microsoft.azure.management.compute.models.HardwareProfile;
import com.microsoft.azure.management.compute.models.NetworkProfile;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.compute.models.VirtualMachineCaptureParameters;
import com.microsoft.azure.management.resources.models.ResourceGroup;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import org.junit.*;

import java.util.ArrayList;

public class VirtualMachineOperationsTest extends ComputeManagementIntegrationTestBase {
    private static String rgName;
    private static String location;
    private static String vmName;

    @BeforeClass
    public static void setup() throws Exception {
        rgName = "csmrg7947";
        vmName = "javatestvm";
        location = "westus";
        addRegexRule("testjava[a-z]{10}");
        createService();
        setupTest(VirtualMachineOperationsTest.class.getSimpleName());
        resetTest(VirtualMachineOperationsTest.class.getSimpleName());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        setupTest(VirtualMachineOperationsTest.class.getSimpleName() + CLEANUP_SUFFIX);
        resetTest(VirtualMachineOperationsTest.class.getSimpleName() + CLEANUP_SUFFIX);
    }

    @Test
    public void createAndListResourceGroupsSuccess() throws Exception {
        computeManagementClient.getVirtualMachinesOperations().generalize(rgName, vmName);
        computeManagementClient.getVirtualMachinesOperations().capture(rgName, vmName, new VirtualMachineCaptureParameters(vmName, "vhds", true));
    }

    @Before
    public void beforeTest() throws Exception {
        setupTest();
    }

    @After
    public void afterTest() throws Exception {
        resetTest();
    }
}
    
