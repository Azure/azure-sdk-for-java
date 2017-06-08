/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import org.junit.Assert;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;
import rx.functions.Action1;

public class TestVirtualMachine extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        final VirtualMachine[] vms = new VirtualMachine[1];
        final SettableFuture<VirtualMachine> future = SettableFuture.create();

        Observable<Indexable> resourceStream = virtualMachines.define(vmName)
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

        Utils.<VirtualMachine>rootResource(resourceStream)
                .subscribe(new Action1<VirtualMachine>() {
                    @Override
                    public void call(VirtualMachine virtualMachine) {
                        future.set(virtualMachine);
                    }
                });
        vms[0] = future.get();

        Assert.assertEquals(1, vms[0].dataDisks().size());
        VirtualMachineDataDisk dataDisk = vms[0].dataDisks().values().iterator().next();
        Assert.assertEquals(150, dataDisk.size());
        Assert.assertEquals(128, vms[0].osDiskSize());
        Disk osDisk = virtualMachines.manager().disks().getById(vms[0].osDiskId());
        Assert.assertNotNull(osDisk);
        Assert.assertEquals(128, osDisk.sizeInGB());

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
