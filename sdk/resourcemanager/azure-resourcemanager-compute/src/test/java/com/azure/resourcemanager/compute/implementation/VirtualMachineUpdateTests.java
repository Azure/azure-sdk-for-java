// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;


import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.compute.ComputeManagementTest;
import com.azure.resourcemanager.compute.models.CachingTypes;
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
        final String vmname = "javavm1";

        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installCommand = "bash install_mysql_server_5.6.sh Abc.123x(";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(mySqlInstallScript);

        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmname)
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withLatestLinuxImage("Canonical", "UbuntuServer", "14.04.4-LTS")
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
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
