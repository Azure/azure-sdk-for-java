// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;


import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.compute.ComputeManagementTest;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.ResourceIdentityType;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineExtension;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualMachineUpdateTests extends ComputeManagementTest {

    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void testVirtualMachineUpdate() {
        // Management Long running operation is Failed or Cancelled
        final String vmname = "javavm1";

        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/00df5c3ae1e25c526e265e78a00211d068b94f93/sdk/resourcemanager/azure-resourcemanager-compute/src/test/assets/install_mysql_server_5.7.sh";
        final String installCommand = "bash install_mysql_server_5.7.sh " + password();
        List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlInstallScript);

        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmname)
            .withRegion(Region.US_WEST2)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withSize(VirtualMachineSizeTypes.STANDARD_D2S_V3)
            .create();

        VirtualMachine.Update vmUpdate = vm.update();
        Assertions.assertFalse(this.isVirtualMachineModifiedDuringUpdate(vm));

        vmUpdate = vmUpdate.defineNewExtension("CustomScriptForLinux")
            .withPublisher("Microsoft.OSTCExtensions")
            .withType("CustomScriptForLinux")
            .withVersion("1.4")
            .withMinorVersionAutoUpgrade()
            .withPublicSetting("fileUris", fileUris)
            .withPublicSetting("commandToExecute", installCommand)
            .attach();
        // extension added, but VM not modified
        Assertions.assertFalse(this.isVirtualMachineModifiedDuringUpdate(vm));

        // modified disk caching
        vmUpdate = vmUpdate.withOSDiskCaching(CachingTypes.READ_ONLY);
        Assertions.assertTrue(this.isVirtualMachineModifiedDuringUpdate(vm));

        vmUpdate = vmUpdate.withOSDiskCaching(CachingTypes.READ_WRITE);
        Assertions.assertFalse(this.isVirtualMachineModifiedDuringUpdate(vm));

        // modified tag
        vmUpdate = vmUpdate.withTag("key1", "value1");
        Assertions.assertTrue(this.isVirtualMachineModifiedDuringUpdate(vm));

        vm = vmUpdate.apply();

        // verify extensions
        Map<String, VirtualMachineExtension> extensions = vm.listExtensions();
        Assertions.assertNotNull(extensions);
        Assertions.assertFalse(extensions.isEmpty());
        VirtualMachineExtension customScriptExtension = extensions.get("CustomScriptForLinux");
        Assertions.assertNotNull(customScriptExtension);

        // verify tags
        Assertions.assertEquals("value1", vm.tags().get("key1"));

        // modify msi
        vm.update()
            .withSystemAssignedManagedServiceIdentity()
            .withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
            .apply();
        Assertions.assertTrue(this.isVirtualMachineModifiedDuringUpdate(vm));

        // verify msi
        Assertions.assertEquals(ResourceIdentityType.SYSTEM_ASSIGNED, vm.managedServiceIdentityType());
        Assertions.assertNotNull(vm.systemAssignedManagedServiceIdentityPrincipalId());
    }

    private boolean isVirtualMachineModifiedDuringUpdate(VirtualMachine vm) {
        VirtualMachineImpl vmImpl = (VirtualMachineImpl) vm;
        // this parameter is not correct for managed identities
        return vmImpl.isVirtualMachineModifiedDuringUpdate(vmImpl.deepCopyInnerToUpdateParameter());
    }
}
