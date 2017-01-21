/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.VirtualMachineUnmanagedDataDisk;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestVirtualMachineDataDisk extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        VirtualMachine virtualMachine = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword("12NewPA$$w0rd!")
                .withUnmanagedDisks()
                .withNewUnmanagedDataDisk(30)
                .defineUnmanagedDataDisk("disk2")
                    .withNewVhd(20)
                    .withCaching(CachingTypes.READ_ONLY)
                    .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_A8)
                .create();

        Assert.assertTrue(virtualMachine.size().equals(VirtualMachineSizeTypes.STANDARD_A8));
        Assert.assertTrue(virtualMachine.unmanagedDataDisks().size() == 2);
        VirtualMachineUnmanagedDataDisk disk2 = null;
        for (VirtualMachineUnmanagedDataDisk dataDisk : virtualMachine.unmanagedDataDisks().values()) {
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
                .withoutUnmanagedDataDisk("disk2")
                .defineUnmanagedDataDisk("disk3")
                    .withNewVhd(10)
                    .withLun(2)
                    .attach()
                .apply();
        Assert.assertTrue(virtualMachine.unmanagedDataDisks().size() == 2);
        VirtualMachineUnmanagedDataDisk disk3 = null;
        for (VirtualMachineUnmanagedDataDisk dataDisk : virtualMachine.unmanagedDataDisks().values()) {
            if (dataDisk.name().equalsIgnoreCase("disk3")) {
                disk3 = dataDisk;
                break;
            }
        }
        Assert.assertNotNull(disk3);
        Assert.assertTrue(disk3.cachingType() == CachingTypes.READ_WRITE);
        Assert.assertTrue(disk3.size() == 10);
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
