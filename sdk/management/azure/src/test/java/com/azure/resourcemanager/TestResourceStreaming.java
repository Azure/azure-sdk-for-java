// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;

public class TestResourceStreaming extends TestTemplate<VirtualMachine, VirtualMachines> {
    private final StorageAccounts storageAccounts;

    public TestResourceStreaming(StorageAccounts storageAccounts) {
        this.storageAccounts = storageAccounts;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().sdkContext().randomResourceName("vm", 10);

        System.out.println("In createResource \n\n\n");

        Creatable<ResourceGroup> rgCreatable =
            virtualMachines
                .manager()
                .resourceManager()
                .resourceGroups()
                .define(virtualMachines.manager().sdkContext().randomResourceName("rg" + vmName, 20))
                .withRegion(Region.US_EAST);

        Creatable<StorageAccount> storageCreatable =
            this
                .storageAccounts
                .define(virtualMachines.manager().sdkContext().randomResourceName("stg", 20))
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgCreatable);

        final AtomicInteger resourceCount = new AtomicInteger(0);

        VirtualMachine virtualMachine =
            (VirtualMachine)
                virtualMachines
                    .define(vmName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgCreatable)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(
                        virtualMachines.manager().sdkContext().randomResourceName("pip", 20))
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUsername("testuser")
                    .withAdminPassword("12NewPA$$w0rd!")
                    .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                    .withNewStorageAccount(storageCreatable)
                    .withNewAvailabilitySet(virtualMachines.manager().sdkContext().randomResourceName("avset", 10))
                    .createAsync()
                    .map(
                        resource -> {
                            resourceCount.incrementAndGet();
                            Resource createdResource = (Resource) resource;
                            System.out.println("Created :" + createdResource.id());
                            return createdResource;
                        })
                    .blockLast();

        Assertions.assertTrue(resourceCount.get() == 7);
        return virtualMachine;
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
