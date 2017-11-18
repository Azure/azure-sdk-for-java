/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

import java.util.List;

public class TestVirtualMachineSizes extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        List<VirtualMachineSize> availableSizes = virtualMachines.sizes().listByRegion(Region.US_EAST);
        Assert.assertTrue(availableSizes.size() > 0);
        System.out.println("VM Sizes: " + availableSizes);
        final String vmName = "vm" + this.testId;
        VirtualMachine vm = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword("12NewPA$$w0rd!")
                .withSize(availableSizes.get(0).name()) // Use the first size
                .create();

        Assert.assertTrue(vm.size().toString().equalsIgnoreCase(availableSizes.get(0).name()));
        return vm;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        List<VirtualMachineSize> resizableSizes = virtualMachine.availableSizes();
        Assert.assertTrue(resizableSizes.size() > 1);
        VirtualMachineSize newSize = null;
        for (VirtualMachineSize resizableSize : resizableSizes) {
            if (!resizableSize.name().equalsIgnoreCase(virtualMachine.size().toString())) {
                newSize = resizableSize;
                break;
            }
        }
        Assert.assertNotNull(newSize);
        virtualMachine = virtualMachine.update()
                .withSize(newSize.name())
                .apply();

        Assert.assertTrue(virtualMachine.size().toString().equalsIgnoreCase(newSize.name()));
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {

    }
}
