package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccounts;
import rx.functions.Func1;

public class TestResourceStreaming extends TestTemplate<VirtualMachine, VirtualMachines> {
    private final StorageAccounts storageAccounts;
    private final ResourceGroups resourceGroups;

    public TestResourceStreaming(StorageAccounts storageAccounts, ResourceGroups resourceGroups) {
        this.storageAccounts = storageAccounts;
        this.resourceGroups = resourceGroups;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;

        System.out.println("In createResource \n\n\n");

        Creatable<ResourceGroup> rgCreatable = this.resourceGroups.define(ResourceNamer.randomResourceName("rg" + vmName, 20))
                .withRegion(Region.US_EAST);


        Creatable<StorageAccount> storageCreatable = this.storageAccounts.define(ResourceNamer.randomResourceName("stg", 20))
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgCreatable);

         VirtualMachine virtualMachine = (VirtualMachine) virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(rgCreatable)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("testuser")
                .withAdminPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                 .withNewStorageAccount(storageCreatable)
                .createAsync(true)
                .map(new Func1<Indexable, Resource>() {
                    @Override
                    public Resource call(Indexable resource) {
                        Resource createdResource = (Resource) resource;
                        System.out.println("Created :" + createdResource.id());
                        return createdResource;
                    }
                }).toBlocking().last();
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
