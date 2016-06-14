package com.microsoft.azure;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

public class TestVirtualMachine extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        VirtualMachine vm = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUserName("testuser")
                .withPassword("12NewPA$$w0rd!")
                .create();
        return vm;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine resource) throws Exception {
        return null;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
