// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.core.management.Region;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import org.junit.jupiter.api.Assertions;

public class TestVirtualMachineInAvailabilitySet extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vm", 10);
        final String newRgName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("rgVmInAvail", 10);
        final String newAvailSetName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("avai", 10);

        VirtualMachine vm =
            virtualMachines
                .define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(newRgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword(ResourceManagerTestBase.password())
                .withComputerName("myvm123")
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withNewAvailabilitySet(newAvailSetName)
                .create();

        Assertions.assertNotNull(vm.availabilitySetId());
        Assertions.assertNotNull(vm.computerName());
        Assertions.assertTrue(vm.computerName().equalsIgnoreCase("myvm123"));
        return vm;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
