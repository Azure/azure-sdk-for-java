/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management;

import com.azure.management.compute.Disk;
import com.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineDataDisk;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Indexable;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;

public class TestVirtualMachine extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().getSdkContext().randomResourceName("vm", 10);;
        final VirtualMachine[] vms = new VirtualMachine[1];
        final SettableFuture<VirtualMachine> future = SettableFuture.create();

        Flux<Indexable> resourceStream = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword("12NewPA$$w0rd!")
                .withNewDataDisk(150)
                .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                .createAsync();

        resourceStream.last().doOnSuccess(vm -> future.set((VirtualMachine) vm));
        vms[0] = future.get();

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
        resource = resource.update()
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .withNewDataDisk(100)
                .apply();
        return resource;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
