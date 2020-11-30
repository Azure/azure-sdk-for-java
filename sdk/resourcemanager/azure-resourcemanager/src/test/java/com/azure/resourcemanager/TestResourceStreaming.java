// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import org.junit.jupiter.api.Assertions;

public class TestResourceStreaming extends TestTemplate<VirtualMachine, VirtualMachines> {
    private final StorageAccounts storageAccounts;

    public TestResourceStreaming(StorageAccounts storageAccounts) {
        this.storageAccounts = storageAccounts;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vm", 10);

        System.out.println("In createResource \n\n\n");

        Creatable<ResourceGroup> rgCreatable =
            virtualMachines
                .manager()
                .resourceManager()
                .resourceGroups()
                .define(virtualMachines.manager().resourceManager().internalContext().randomResourceName("rg" + vmName, 20))
                .withRegion(Region.US_EAST);

        Creatable<StorageAccount> storageCreatable =
            this
                .storageAccounts
                .define(virtualMachines.manager().resourceManager().internalContext().randomResourceName("stg", 20))
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgCreatable);

        VirtualMachine virtualMachine =
            virtualMachines
                .define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgCreatable)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(
                    virtualMachines.manager().resourceManager().internalContext().randomResourceName("pip", 20))
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withNewStorageAccount(storageCreatable)
                .withNewAvailabilitySet(virtualMachines.manager().resourceManager().internalContext().randomResourceName("avset", 10))
                .createAsync()
                .block();

        ComputeManager manager = virtualMachines.manager();
        Assertions.assertEquals(1, manager.storageManager().storageAccounts().listByResourceGroup(rgCreatable.name()).stream().count());
        Assertions.assertEquals(1, manager.networkManager().publicIpAddresses().listByResourceGroup(rgCreatable.name()).stream().count());
        Assertions.assertEquals(1, manager.networkManager().networks().listByResourceGroup(rgCreatable.name()).stream().count());
        Assertions.assertEquals(1, manager.networkManager().networkInterfaces().listByResourceGroup(rgCreatable.name()).stream().count());
        Assertions.assertEquals(1, manager.availabilitySets().listByResourceGroup(rgCreatable.name()).stream().count());

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
