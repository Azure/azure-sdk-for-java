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

import com.microsoft.azure.management.compute.models.ComputeLongRunningOperationResponse;
import com.microsoft.azure.management.compute.models.ComputeOperationResponse;
import com.microsoft.azure.management.compute.models.ComputeOperationStatus;
import com.microsoft.azure.management.compute.models.DeleteOperationResponse;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.compute.models.VirtualMachineCaptureParameters;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.windowsazure.core.OperationResponse;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class VMOperationalTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMOperationalTests.class);
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
    /// <summary>
    /// Covers following Operations:
    /// Create RG
    /// Create Storage Account
    /// Create Network Resources
    /// Create VM
    /// GET VM Model View
    /// Start VM
    /// Stop VM
    /// Restart VM
    /// Deallocate VM
    /// Generalize VM
    /// Capture VM
    /// Delete VM
    /// Delete RG
    /// </summary>
    @Test
    public void testVMOperations() throws Exception {
        log.info("creating VM, in mock: " + IS_MOCKED);
        ResourceContext context = createTestResourceContext(false);
        context.setImageReference(
                ComputeHelper.getUbuntuServerDefaultImage(computeManagementClient, context.getLocation()));

        VirtualMachine vm = createVM(context, generateName("VM"));

        log.info("start vm: " + vm.getName());
        ComputeOperationResponse opResponse = computeManagementClient.getVirtualMachinesOperations()
                .beginStarting(context.getResourceGroupName(), vm.getName());
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, opResponse.getStatusCode());

//        ComputeLongRunningOperationResponse lroResponse = computeManagementClient.getVirtualMachinesOperations()
//                .start(context.getResourceGroupName(), vm.getName());
//        Assert.assertEquals(ComputeOperationStatus.SUCCEEDED, lroResponse.getStatus());

        log.info("Restart vm: " + vm.getName());
        opResponse = computeManagementClient.getVirtualMachinesOperations()
                .beginRestarting(context.getResourceGroupName(), vm.getName());
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, opResponse.getStatusCode());

//        lroResponse = computeManagementClient.getVirtualMachinesOperations()
//                .restart(context.getResourceGroupName(), vm.getName());
//        Assert.assertEquals(ComputeOperationStatus.SUCCEEDED, lroResponse.getStatus());

        log.info("Stop vm: " + vm.getName());
        opResponse = computeManagementClient.getVirtualMachinesOperations()
                .beginPoweringOff(context.getResourceGroupName(), vm.getName());
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, opResponse.getStatusCode());

        ComputeLongRunningOperationResponse lroResponse = computeManagementClient.getVirtualMachinesOperations()
                .powerOff(context.getResourceGroupName(), vm.getName());
        Assert.assertEquals(ComputeOperationStatus.Succeeded, lroResponse.getStatus());

        // manual pause for crp bug
        if (!IS_MOCKED) {
            String sleepDuration = System.getenv("stopvmsleep");
            if (sleepDuration != null) {
                Thread.sleep(Long.parseLong(sleepDuration, 10));
            } else {
                Thread.sleep(60000);
            }
        }

        log.info("Generalize vm: " + vm.getName());
        OperationResponse generalizeOpResponse = computeManagementClient.getVirtualMachinesOperations()
                .generalize(context.getResourceGroupName(), vm.getName());
        Assert.assertEquals(HttpStatus.SC_OK, generalizeOpResponse.getStatusCode());

        log.info("Capture vm: " + vm.getName());
        VirtualMachineCaptureParameters captureParameters = new VirtualMachineCaptureParameters();
        captureParameters.setDestinationContainerName(generateName("destcon"));
        captureParameters.setVirtualHardDiskNamePrefix("vhdnamepre");
        captureParameters.setOverwrite(true);

        ComputeLongRunningOperationResponse captureResponse = computeManagementClient.getVirtualMachinesOperations()
                .capture(context.getResourceGroupName(), vm.getName(), captureParameters);
        Assert.assertEquals(ComputeOperationStatus.Succeeded, captureResponse.getStatus());
//        Assert.assertNotNull(captureResponse.getOutput());
//        Assert.assertEquals('{', captureResponse.getOutput().charAt(0));
//        Assert.assertTrue(
//                captureResponse.getOutput().toLowerCase().contains(
//                        captureParameters.getDestinationContainerName().toLowerCase()));
//        Assert.assertTrue(
//                captureResponse.getOutput().toLowerCase().contains(
//                        captureParameters.getVirtualHardDiskNamePrefix().toLowerCase()));

        log.info("Deallocate created vm: " + vm.getName());
        opResponse = computeManagementClient.getVirtualMachinesOperations()
                .beginDeallocating(context.getResourceGroupName(), vm.getName());
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, opResponse.getStatusCode());

//        lroResponse = computeManagementClient.getVirtualMachinesOperations()
//                .deallocate(context.getResourceGroupName(), vm.getName());
//        Assert.assertEquals(ComputeOperationStatus.SUCCEEDED, lroResponse.getStatus());

        log.info("Delete VM: " + vm.getName());
        DeleteOperationResponse deleteResponse = computeManagementClient.getVirtualMachinesOperations()
                .delete(m_rgName, vm.getName());
        Assert.assertNotEquals(ComputeOperationStatus.Failed, deleteResponse.getStatus());
    }
}
