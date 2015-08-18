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
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.azure.utility.StorageHelper;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.exception.ServiceException;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.junit.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

public class ExtensionTests extends ComputeTestBase {
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
    public void testVmExtensionsOperations() throws Exception {
        VirtualMachineExtension extension = getTestVmExtension();
        ImageReference imageReference = getPlatformVmImage(true);

        ResourceContext context = createTestResourceContext(false);
        context.setImageReference(imageReference);
        createOrUpdateResourceGroup(context.getResourceGroupName());

        StorageAccount storageAccount = StorageHelper.createStorageAccount(storageManagementClient, context);
        Assert.assertNotNull(storageAccount);

        String vmName = generateName("vm");

        VirtualMachine vm = createVM(context, vmName);
        // TODO: Disable this case because there is (potentially) a bug in the http recorder which make this case fails under playback
//            verifyDeleteNonExistingExtension(context.getResourceGroupName(), "RandomVM", "VMExtensionDoesNotExist");
        verifyAddExtensionToVM(vm, context.getResourceGroupName());
        verifyGetExtension(extension, vm, context.getResourceGroupName());
        verifyGetExtensionInstanceView(extension, vm, context.getResourceGroupName());
        verifyVmExtensionInVmInfo(extension, vm, context.getResourceGroupName());
        verifyVmExtensionInstanceViewInVmInstanceView(vm, context.getResourceGroupName());
        verifyDeleteExtension(extension, vm, context.getResourceGroupName());
    }

    private void verifyDeleteNonExistingExtension(String rgName, String vmName, String extensionName) throws Exception {
        DeleteOperationResponse response = computeManagementClient.getVirtualMachineExtensionsOperations().delete(rgName, vmName, extensionName);
        Assert.assertEquals(HttpStatus.SC_NO_CONTENT, response.getStatusCode());
        Assert.assertEquals(OperationStatus.SUCCEEDED, response.getStatus());
    }

    private void verifyDeleteExtension(VirtualMachineExtension extension, VirtualMachine vm, String rgName) throws IOException, ServiceException {
        // Validate the extension delete API
        OperationResponse deleteResponse = computeManagementClient.getVirtualMachineExtensionsOperations().beginDeleting(rgName, vm.getName(), extension.getName());
        Assert.assertEquals(HttpStatus.SC_ACCEPTED, deleteResponse.getStatusCode());
    }

    private void verifyVmExtensionInstanceViewInVmInstanceView(VirtualMachine vm, String rgName) throws IOException, ServiceException, URISyntaxException {
        // Validate the extension instance view in the VM isntance-view
        VirtualMachineGetResponse getVmWithInstanceViewResponse = computeManagementClient.getVirtualMachinesOperations().getWithInstanceView(rgName, vm.getName());
        Assert.assertEquals(HttpStatus.SC_OK, getVmWithInstanceViewResponse.getStatusCode());
        validateVmExtensionInstanceView(getVmWithInstanceViewResponse.getVirtualMachine().getInstanceView().getExtensions().get(0));
    }

    private void verifyVmExtensionInVmInfo(VirtualMachineExtension extension, VirtualMachine vm, String rgName) throws IOException, ServiceException, URISyntaxException {
        // Validate the extension in the VM info
        VirtualMachineGetResponse getVmResponse = computeManagementClient.getVirtualMachinesOperations().get(rgName, vm.getName());
        Assert.assertEquals(HttpStatus.SC_OK, getVmResponse.getStatusCode());
        validateVmExtension(extension, getVmResponse.getVirtualMachine().getExtensions().get(0));
    }

    private void verifyGetExtensionInstanceView(VirtualMachineExtension extension, VirtualMachine vm, String rgName) throws IOException, ServiceException, URISyntaxException {
        // Validate Get InstanceView for the extension
        VirtualMachineExtensionGetResponse getVmExtensionInstanceViewResponse =
                computeManagementClient.getVirtualMachineExtensionsOperations().getWithInstanceView(rgName, vm.getName(), extension.getName());
        Assert.assertEquals(HttpStatus.SC_OK, getVmExtensionInstanceViewResponse.getStatusCode());
        validateVmExtensionInstanceView(getVmExtensionInstanceViewResponse.getVirtualMachineExtension().getInstanceView());
    }

    private void verifyGetExtension(VirtualMachineExtension extension, VirtualMachine vm, String rgName) throws IOException, ServiceException, URISyntaxException {
        // Perform a Get operation on the extension
        VirtualMachineExtensionGetResponse vmExtensionGetResponse = computeManagementClient.getVirtualMachineExtensionsOperations().get(rgName, vm.getName(), extension.getName());
        Assert.assertEquals(HttpStatus.SC_OK, vmExtensionGetResponse.getStatusCode());
        validateVmExtension(extension, vmExtensionGetResponse.getVirtualMachineExtension());
    }

    private void validateVmExtensionInstanceView(VirtualMachineExtensionInstanceView virtualMachineExtension) {
        Assert.assertNotNull(virtualMachineExtension);
    }

    private void verifyAddExtensionToVM(VirtualMachine vm, String rgName) throws Exception {
        // Add an extension to the VM
        VirtualMachineExtension vmExtension = getTestVmExtension();
        VirtualMachineExtensionCreateOrUpdateResponse response = computeManagementClient.getVirtualMachineExtensionsOperations().beginCreatingOrUpdating(m_rgName, vm.getName(), vmExtension);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatusCode());

        validateVmExtension(vmExtension, response.getVirtualMachineExtension());
    }

    private void validateVmExtension(VirtualMachineExtension vmExtExpected, VirtualMachineExtension vmExtReturned) {
        Assert.assertNotNull(vmExtReturned);
        Assert.assertFalse(vmExtReturned.getProvisioningState().equals(null));
        Assert.assertFalse(vmExtReturned.getProvisioningState().equals(""));
        Assert.assertEquals(vmExtExpected.getPublisher(), vmExtReturned.getPublisher());
        Assert.assertEquals(vmExtExpected.getExtensionType(), vmExtReturned.getExtensionType());
        Assert.assertEquals(vmExtExpected.isAutoUpgradeMinorVersion(), vmExtReturned.isAutoUpgradeMinorVersion());
        Assert.assertEquals(vmExtExpected.getTypeHandlerVersion(), vmExtReturned.getTypeHandlerVersion());
        Assert.assertEquals(vmExtExpected.getSettings(), vmExtReturned.getSettings());
    }

    private VirtualMachineExtension getTestVmExtension() {
        VirtualMachineExtension vmExtension = new VirtualMachineExtension(ComputeTestBase.m_location);
        vmExtension.setName("vmext01");
        vmExtension.setTags(new HashMap<String, String>() {
            {
                put("extensionTag1", "1");
                put("extensionTag2", "2");
            }
        });
        vmExtension.setType("Microsoft.Compute/virtualMachines/extensions");
        vmExtension.setPublisher("Microsoft.Compute");
        vmExtension.setTypeHandlerVersion("2.0");
        vmExtension.setAutoUpgradeMinorVersion(true);
        vmExtension.setExtensionType("VMAccessAgent");
        vmExtension.setSettings("{}");
        vmExtension.setProtectedSettings("{}");

        return vmExtension;
    }
}
