// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

public class TestVirtualMachineInAvailabilitySet extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().sdkContext().randomResourceName("vm", 10);
        final String newRgName = virtualMachines.manager().sdkContext().randomResourceName("rgVmInAvail", 10);
        final String newAvailSetName = virtualMachines.manager().sdkContext().randomResourceName("avai", 10);

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
                .withRootPassword("12NewPA$$w0rd!")
                .withComputerName("myvm123")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
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
