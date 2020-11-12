// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Assertions;

public class TestVirtualMachineDataDisk extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vm", 10);
        VirtualMachine virtualMachine =
            virtualMachines
                .define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword("12NewPA$$w0rd!")
                .withUnmanagedDisks()
                .withNewUnmanagedDataDisk(30)
                .defineUnmanagedDataDisk("disk2")
                .withNewVhd(20)
                .withCaching(CachingTypes.READ_ONLY)
                .attach()
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

        // Assertions.assertTrue(virtualMachine.size().equals(VirtualMachineSizeTypes.fromString("Standard_D2a_v4")));
        Assertions.assertTrue(virtualMachine.unmanagedDataDisks().size() == 2);
        VirtualMachineUnmanagedDataDisk disk2 = null;
        for (VirtualMachineUnmanagedDataDisk dataDisk : virtualMachine.unmanagedDataDisks().values()) {
            if (dataDisk.name().equalsIgnoreCase("disk2")) {
                disk2 = dataDisk;
                break;
            }
        }
        Assertions.assertNotNull(disk2);
        Assertions.assertTrue(disk2.cachingType() == CachingTypes.READ_ONLY);
        Assertions.assertTrue(disk2.size() == 20);
        return virtualMachine;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        virtualMachine =
            virtualMachine
                .update()
                .withoutUnmanagedDataDisk("disk2")
                .defineUnmanagedDataDisk("disk3")
                .withNewVhd(10)
                .withLun(2)
                .attach()
                .apply();
        Assertions.assertTrue(virtualMachine.unmanagedDataDisks().size() == 2);
        VirtualMachineUnmanagedDataDisk disk3 = null;
        for (VirtualMachineUnmanagedDataDisk dataDisk : virtualMachine.unmanagedDataDisks().values()) {
            if (dataDisk.name().equalsIgnoreCase("disk3")) {
                disk3 = dataDisk;
                break;
            }
        }
        Assertions.assertNotNull(disk3);
        Assertions.assertTrue(disk3.cachingType() == CachingTypes.READ_WRITE);
        Assertions.assertTrue(disk3.size() == 10);
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
