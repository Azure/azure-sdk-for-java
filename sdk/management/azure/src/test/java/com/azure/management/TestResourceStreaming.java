/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management;


import com.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.StorageAccount;
import com.azure.management.storage.StorageAccounts;
import org.junit.jupiter.api.Assertions;

import java.util.concurrent.atomic.AtomicInteger;

public class TestResourceStreaming extends TestTemplate<VirtualMachine, VirtualMachines> {
    private final StorageAccounts storageAccounts;

    public TestResourceStreaming(StorageAccounts storageAccounts) {
        this.storageAccounts = storageAccounts;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().getSdkContext().randomResourceName("vm", 10);;

        System.out.println("In createResource \n\n\n");

        Creatable<ResourceGroup> rgCreatable = virtualMachines.manager().getResourceManager().resourceGroups().define(virtualMachines.manager().getSdkContext().randomResourceName("rg" + vmName, 20))
                .withRegion(Region.US_EAST);

        Creatable<StorageAccount> storageCreatable = this.storageAccounts.define(virtualMachines.manager().getSdkContext().randomResourceName("stg", 20))
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgCreatable);

        final AtomicInteger resourceCount = new AtomicInteger(0);

        VirtualMachine virtualMachine = (VirtualMachine) virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgCreatable)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(virtualMachines.manager().getSdkContext().randomResourceName("pip", 20))
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                .withNewStorageAccount(storageCreatable)
                .withNewAvailabilitySet(virtualMachines.manager().getSdkContext().randomResourceName("avset", 10))
                .createAsync()
                .map(resource -> {
                    resourceCount.incrementAndGet();
                    Resource createdResource = (Resource) resource;
                    System.out.println("Created :" + createdResource.id());
                    return createdResource;
                }).blockLast();

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