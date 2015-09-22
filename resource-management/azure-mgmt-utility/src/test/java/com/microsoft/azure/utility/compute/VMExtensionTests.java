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

import com.microsoft.azure.management.compute.models.DeleteOperationResponse;
import com.microsoft.azure.management.compute.models.VirtualMachine;
import com.microsoft.azure.management.compute.models.VirtualMachineExtension;
import com.microsoft.azure.management.compute.models.VirtualMachineExtensionCreateOrUpdateResponse;
import com.microsoft.azure.management.compute.models.VirtualMachineExtensionGetResponse;
import com.microsoft.azure.management.compute.models.VirtualMachineExtensionInstanceView;
import com.microsoft.azure.management.compute.models.VirtualMachineGetResponse;
import com.microsoft.azure.utility.ComputeHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

public class VMExtensionTests extends ComputeTestBase {
    static {
        log = LogFactory.getLog(VMExtensionTests.class);
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
    public void testVmExtensionsOperations()
            throws Exception {
        VirtualMachineExtension extension = getTestVmExtension();
        ResourceContext context = createTestResourceContext(false);
        context.setImageReference(
                ComputeHelper.getWindowsServerDefaultImage(computeManagementClient, context.getLocation()));

        VirtualMachine vm = createVM(context, generateName("vm"));
        // test framwork recording bug, track in issues
        // verifyDeleteNonExistingExtension(context.getResourceGroupName(), vm.getName(), "VMExtensionDoesNotExist");
        verifyAddExtensionToVM(vm, context, extension);

        log.info("Start verify created extension in vm: " + vm.getName());
        verifyGetExtension(extension, vm, context.getResourceGroupName());
        verifyGetExtensionInstanceView(extension, vm, context.getResourceGroupName());
        verifyVmExtensionInVmInfo(extension, vm, context.getResourceGroupName());
        verifyVmExtensionInstanceViewInVmInstanceView(vm, context.getResourceGroupName());

        log.info("Delete extension in vm: " + vm.getName());
        verifyDeleteExtension(extension, vm, context.getResourceGroupName());
    }

    private void verifyDeleteNonExistingExtension(String rgName, String vmName, String extensionName)
            throws Exception {
        DeleteOperationResponse response = computeManagementClient.getVirtualMachineExtensionsOperations()
                .delete(rgName, vmName, extensionName);
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusCode());
        Assert.assertEquals(OperationStatus.Succeeded, response.getStatus());
    }

    private void verifyDeleteExtension(VirtualMachineExtension extension, VirtualMachine vm, String rgName)
            throws IOException, ServiceException {
        OperationResponse deleteResponse = computeManagementClient.getVirtualMachineExtensionsOperations()
                .beginDeleting(rgName, vm.getName(), extension.getName());
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, deleteResponse.getStatusCode());
    }

    private void verifyVmExtensionInstanceViewInVmInstanceView(VirtualMachine vm, String rgName)
            throws IOException, ServiceException, URISyntaxException {
        VirtualMachineGetResponse getVmWithInstanceViewResponse =
                computeManagementClient.getVirtualMachinesOperations().getWithInstanceView(rgName, vm.getName());
        Assert.assertEquals(HttpStatus.SC_OK, getVmWithInstanceViewResponse.getStatusCode());
        validateVmExtensionInstanceView(
                getVmWithInstanceViewResponse.getVirtualMachine().getInstanceView().getExtensions().get(0));
    }

    private void verifyVmExtensionInVmInfo(VirtualMachineExtension extension, VirtualMachine vm, String rgName)
            throws IOException, ServiceException, URISyntaxException {
        VirtualMachineGetResponse getVmResponse =
                computeManagementClient.getVirtualMachinesOperations().get(rgName, vm.getName());
        Assert.assertEquals(HttpStatus.SC_OK, getVmResponse.getStatusCode());
        validateVmExtension(extension, getVmResponse.getVirtualMachine().getExtensions().get(0));
    }

    private void verifyGetExtensionInstanceView(VirtualMachineExtension extension, VirtualMachine vm, String rgName)
            throws IOException, ServiceException, URISyntaxException {
        VirtualMachineExtensionGetResponse getVmExtensionInstanceViewResponse =
                computeManagementClient.getVirtualMachineExtensionsOperations()
                        .getWithInstanceView(rgName, vm.getName(), extension.getName());
        Assert.assertEquals(HttpStatus.SC_OK, getVmExtensionInstanceViewResponse.getStatusCode());
        validateVmExtensionInstanceView(
                getVmExtensionInstanceViewResponse.getVirtualMachineExtension().getInstanceView());
    }

    private void verifyGetExtension(VirtualMachineExtension extension, VirtualMachine vm, String rgName)
            throws IOException, ServiceException, URISyntaxException {
        VirtualMachineExtensionGetResponse vmExtensionGetResponse =
                computeManagementClient.getVirtualMachineExtensionsOperations()
                        .get(rgName, vm.getName(), extension.getName());
        Assert.assertEquals(HttpStatus.SC_OK, vmExtensionGetResponse.getStatusCode());
        validateVmExtension(extension, vmExtensionGetResponse.getVirtualMachineExtension());
    }

    private void validateVmExtensionInstanceView(VirtualMachineExtensionInstanceView virtualMachineExtension) {
        Assert.assertNotNull(virtualMachineExtension);
    }

    private void verifyAddExtensionToVM(VirtualMachine vm, ResourceContext context, VirtualMachineExtension extension)
            throws Exception {
        VirtualMachineExtensionCreateOrUpdateResponse response =
                computeManagementClient.getVirtualMachineExtensionsOperations()
                        .beginCreatingOrUpdating(context.getResourceGroupName(), vm.getName(), extension);

        Assert.assertEquals("statusCode should be created", HttpStatus.SC_CREATED, response.getStatusCode());
        validateVmExtension(extension, response.getVirtualMachineExtension());

        if (!IS_MOCKED) {
            Thread.sleep(120000);
        }

//        log.info("Start waiting for extension creation at vm: " + vm.getName());
//        ComputeLongRunningOperationResponse lroResponse =
//                computeManagementClient.getVirtualMachineExtensionsOperations()
//                        .createOrUpdate(context.getResourceGroupName(), vm.getName(), extension);
//        Assert.assertEquals(ComputeOperationStatus.SUCCEEDED, lroResponse.getStatus());
    }

    private void validateVmExtension(VirtualMachineExtension vmExtExpected, VirtualMachineExtension vmExtReturned) {
        Assert.assertNotNull(vmExtReturned);
        Assert.assertNotNull(vmExtReturned.getProvisioningState());
        Assert.assertNotEquals("", vmExtReturned.getProvisioningState());
        Assert.assertEquals(vmExtExpected.getPublisher(), vmExtReturned.getPublisher());
        Assert.assertEquals(vmExtExpected.getExtensionType(), vmExtReturned.getExtensionType());
        Assert.assertEquals(vmExtExpected.isAutoUpgradeMinorVersion(), vmExtReturned.isAutoUpgradeMinorVersion());
        Assert.assertEquals(vmExtExpected.getTypeHandlerVersion(), vmExtReturned.getTypeHandlerVersion());
        //Assert.assertEquals(vmExtExpected.getSettings(), vmExtReturned.getSettings());
    }

    private VirtualMachineExtension getTestVmExtension() {
        VirtualMachineExtension vmExtension = new VirtualMachineExtension(ComputeTestBase.m_location);
        vmExtension.setName("javatestext1");
        vmExtension.setTags(new HashMap<String, String>() {
            {
                put("extensionTag1", "1");
                put("extensionTag2", "2");
            }
        });
        vmExtension.setType("Microsoft.Compute/virtualMachines/extensions");
        vmExtension.setPublisher("Microsoft.Compute");
        vmExtension.setTypeHandlerVersion("1.3");
        vmExtension.setAutoUpgradeMinorVersion(true);
        vmExtension.setExtensionType("CustomScriptExtension");
        vmExtension.setSettings(
                "{\"fileUris\":[],\"commandToExecute\":\"powershell -ExecutionPolicy Unrestricted pwd\"}");
        vmExtension.setProtectedSettings("{}");

        return vmExtension;
    }
}