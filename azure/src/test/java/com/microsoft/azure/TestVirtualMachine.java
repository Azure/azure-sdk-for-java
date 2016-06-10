package com.microsoft.azure;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.KnownVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.DataDisk;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

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
                .withMarketplaceImage()
                .popular(KnownVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withWindowsOS()
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
        Utils.print(virtualMachine);
    }
}
