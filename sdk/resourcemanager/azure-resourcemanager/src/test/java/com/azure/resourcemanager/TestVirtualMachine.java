// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.core.management.Region;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

public class TestVirtualMachine extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vm", 10);

        final VirtualMachine[] vms = new VirtualMachine[1];

        Mono<VirtualMachine> resourceStream =
            virtualMachines
                .define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword(ResourceManagerTestBase.password())
                .withNewDataDisk(150)
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .createAsync();

        vms[0] = resourceStream.block();

        Assertions.assertEquals(1, vms[0].dataDisks().size());
        VirtualMachineDataDisk dataDisk = vms[0].dataDisks().values().iterator().next();
        Assertions.assertEquals(150, dataDisk.size());
        Assertions.assertEquals(128, vms[0].osDiskSize());
        Disk osDisk = virtualMachines.manager().disks().getById(vms[0].osDiskId());
        Assertions.assertNotNull(osDisk);
        Assertions.assertEquals(128, osDisk.sizeInGB());

        return vms[0];
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine resource) throws Exception {
        resource = resource.update().withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4")).withNewDataDisk(100).apply();
        return resource;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
