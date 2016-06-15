package com.microsoft.azure;

import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.CachingTypes;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestVirtualMachineDataDisk extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        VirtualMachine virtualMachine = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUserName("testuser")
                .withPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_A8)
                .withNewDataDisk(30)
                .defineNewDataDisk("disk2")
                    .withSizeInGB(20)
                    .withCaching(CachingTypes.READ_ONLY)
                    .attach()
                .create();

        Assert.assertTrue(virtualMachine.size().equalsIgnoreCase("STANDARD_A8"));
        Assert.assertTrue(virtualMachine.dataDisks().size() == 2);
        DataDisk disk2 = null;
        for (DataDisk dataDisk : virtualMachine.dataDisks()) {
            if (dataDisk.name().equalsIgnoreCase("disk2")) {
                disk2 = dataDisk;
                break;
            }
        }
        Assert.assertNotNull(disk2);
        Assert.assertTrue(disk2.cachingType() == CachingTypes.READ_ONLY);
        Assert.assertTrue(disk2.size() == 20);
        return virtualMachine;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        virtualMachine = virtualMachine.update()
                .withoutDataDisk("disk2")
                .defineNewDataDisk("disk3")
                    .withSizeInGB(10)
                    .attach()
                .apply();
        Assert.assertTrue(virtualMachine.dataDisks().size() == 2);
        DataDisk disk3 = null;
        for (DataDisk dataDisk : virtualMachine.dataDisks()) {
            if (dataDisk.name().equalsIgnoreCase("disk3")) {
                disk3 = dataDisk;
                break;
            }
        }
        Assert.assertNotNull(disk3);
        Assert.assertTrue(disk3.cachingType() == CachingTypes.READ_WRITE);
        Assert.assertTrue(disk3.size() == 10);
        return null;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
