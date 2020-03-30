/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management;

import com.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;

public class TestVirtualMachineInAvailabilitySet extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().getSdkContext().randomResourceName("vm", 10);
        final String newRgName = virtualMachines.manager().getSdkContext().randomResourceName("rgVmInAvail", 10);
        final String newAvailSetName = virtualMachines.manager().getSdkContext().randomResourceName("avai", 10);

        VirtualMachine vm = virtualMachines.define(vmName)
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
