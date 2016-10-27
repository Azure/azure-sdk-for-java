package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.junit.Assert;

public class TestVirtualMachineInAvailabilitySet extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        final String newRgName = ResourceNamer.randomResourceName("rgVmInAvail", 10);
        final String newAvailSetName = ResourceNamer.randomResourceName("avai", 10);

        VirtualMachine vm = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(newRgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword("12NewPA$$w0rd!")
                .withComputerName("myvm123")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .withNewAvailabilitySet(newAvailSetName)
                .create();

        Assert.assertNotNull(vm.availabilitySetId());
        Assert.assertNotNull(vm.computerName());
        Assert.assertTrue(vm.computerName().equalsIgnoreCase("myvm123"));
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
