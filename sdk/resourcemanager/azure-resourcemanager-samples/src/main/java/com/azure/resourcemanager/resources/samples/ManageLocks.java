// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.models.LockLevel;
import com.azure.resourcemanager.resources.models.ManagementLock;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * This sample shows examples of management locks usage on various resources.
 *  - Create a number of various resources to apply locks to
 *  - Apply various locks to the resources
 *  - Retrieve and show lock information
 *  - Remove the locks and clean up
 */
public final class ManageLocks {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {

        final String password = Utils.randomResourceName(azureResourceManager, "P@s", 14);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rg", 15);
        final String vmName = Utils.randomResourceName(azureResourceManager, "vm", 15);
        final String storageName = Utils.randomResourceName(azureResourceManager, "st", 15);
        final String diskName = Utils.randomResourceName(azureResourceManager, "dsk", 15);
        final String netName = Utils.randomResourceName(azureResourceManager, "net", 15);
        final Region region = Region.US_WEST;

        ResourceGroup resourceGroup;
        ManagementLock lockGroup = null,
            lockVM = null,
            lockStorage = null,
            lockDiskRO = null,
            lockDiskDel = null,
            lockSubnet = null;

        try {
            //=============================================================
            // Create a shared resource group for all the resources so they can all be deleted together
            //
            resourceGroup = azureResourceManager.resourceGroups().define(rgName)
                .withRegion(region)
                .create();
            System.out.println("Created a new resource group - " + resourceGroup.id());

            //============================================================
            // Create various resources for demonstrating locks
            //

            // Define a network to apply a lock to
            Creatable<Network> netDefinition = azureResourceManager.networks().define(netName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28");

            // Define a managed disk for testing locks on that
            Creatable<Disk> diskDefinition = azureResourceManager.disks().define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withData()
                .withSizeInGB(100);

            // Define a VM to apply a lock to
            Creatable<VirtualMachine> vmDefinition = azureResourceManager.virtualMachines().define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup)
                .withNewPrimaryNetwork(netDefinition)
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("tester")
                .withRootPassword(password)
                .withNewDataDisk(diskDefinition, 1, CachingTypes.NONE)
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"));

            // Define a storage account to apply a lock to
            Creatable<StorageAccount> storageDefinition = azureResourceManager.storageAccounts().define(storageName)
                .withRegion(region)
                .withExistingResourceGroup(resourceGroup);

            // Create resources in parallel to save time
            System.out.println("Creating the needed resources...");
            Flux.merge(
                storageDefinition.createAsync().subscribeOn(Schedulers.parallel()),
                vmDefinition.createAsync().subscribeOn(Schedulers.parallel()))
                .blockLast();
            System.out.println("Resources created.");

            VirtualMachine vm = (VirtualMachine) vmDefinition;
            StorageAccount storage = (StorageAccount) storageDefinition;
            Disk disk = (Disk) diskDefinition;
            Network network = vm.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();
            Subnet subnet = network.subnets().values().iterator().next();

            //============================================================
            // Create various locks for the created resources
            //

            // Locks can be created serially, and multiple can be applied to the same resource:
            System.out.println("Creating locks sequentially...");

            // Apply a ReadOnly lock to the disk
            lockDiskRO = azureResourceManager.managementLocks().define("diskLockRO")
                .withLockedResource(disk)
                .withLevel(LockLevel.READ_ONLY)
                .create();

            // Apply a lock preventing the disk from being deleted
            lockDiskDel = azureResourceManager.managementLocks().define("diskLockDel")
                .withLockedResource(disk)
                .withLevel(LockLevel.CAN_NOT_DELETE)
                .create();

            // Locks can also be created in parallel, for better overall performance:
            System.out.println("Creating locks in parallel...");

            // Define a subnet lock
            Creatable<ManagementLock> lockSubnetDef = azureResourceManager.managementLocks().define("subnetLock")
                .withLockedResource(subnet.innerModel().id())
                .withLevel(LockLevel.READ_ONLY);

            // Define a VM lock
            Creatable<ManagementLock> lockVMDef = azureResourceManager.managementLocks().define("vmlock")
                .withLockedResource(vm)
                .withLevel(LockLevel.READ_ONLY)
                .withNotes("vm readonly lock");

            // Define a resource group lock
            Creatable<ManagementLock> lockGroupDef = azureResourceManager.managementLocks().define("rglock")
                .withLockedResource(resourceGroup.id())
                .withLevel(LockLevel.CAN_NOT_DELETE);

            // Define a storage lock
            Creatable<ManagementLock> lockStorageDef = azureResourceManager.managementLocks().define("stLock")
                .withLockedResource(storage)
                .withLevel(LockLevel.CAN_NOT_DELETE);

            @SuppressWarnings("unchecked")
            CreatedResources<ManagementLock> created = azureResourceManager.managementLocks().create(
                lockVMDef,
                lockGroupDef,
                lockStorageDef,
                lockSubnetDef);

            lockVM = created.get(lockVMDef.key());
            lockStorage = created.get(lockStorageDef.key());
            lockGroup = created.get(lockGroupDef.key());
            lockSubnet = created.get(lockSubnetDef.key());

            System.out.println("Locks created.");

            //============================================================
            // Retrieve and show lock information
            //

            // Count and show locks (Note: locks returned for a resource include the locks for its resource group and child resources)
            int lockCount = Utils.getSize(azureResourceManager.managementLocks().listForResource(vm.id()));
            System.out.println("Number of locks applied to the virtual machine: " + lockCount);
            lockCount = Utils.getSize(azureResourceManager.managementLocks().listByResourceGroup(resourceGroup.name()));
            System.out.println("Number of locks applied to the resource group (includes locks on resources in the group): " + lockCount);
            lockCount = Utils.getSize(azureResourceManager.managementLocks().listForResource(storage.id()));
            System.out.println("Number of locks applied to the storage account: " + lockCount);
            lockCount = Utils.getSize(azureResourceManager.managementLocks().listForResource(disk.id()));
            System.out.println("Number of locks applied to the managed disk: " + lockCount);
            lockCount = Utils.getSize(azureResourceManager.managementLocks().listForResource(network.id()));
            System.out.println("Number of locks applied to the network (including its subnets): " + lockCount);

            // Locks can be retrieved using their ID
            lockVM = azureResourceManager.managementLocks().getById(lockVM.id());
            lockGroup = azureResourceManager.managementLocks().getByResourceGroup(resourceGroup.name(), "rglock");
            lockStorage = azureResourceManager.managementLocks().getById(lockStorage.id());
            lockDiskRO = azureResourceManager.managementLocks().getById(lockDiskRO.id());
            lockDiskDel = azureResourceManager.managementLocks().getById(lockDiskDel.id());
            lockSubnet = azureResourceManager.managementLocks().getById(lockSubnet.id());

            // Show the locks
            Utils.print(lockGroup);
            Utils.print(lockVM);
            Utils.print(lockDiskDel);
            Utils.print(lockDiskRO);
            Utils.print(lockStorage);
            Utils.print(lockSubnet);

            // List all locks within a subscription
            PagedIterable<ManagementLock> locksSubscription = azureResourceManager.managementLocks().list();
            System.out.println("Total number of locks within this subscription: " + Utils.getSize(locksSubscription));
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {

            //============================================================
            // Delete locks and clean up
            //

            try {
                // Clean up (remember to unlock resources before deleting the resource group)
                azureResourceManager.managementLocks().deleteByIds(
                    lockGroup.id(),
                    lockVM.id(),
                    lockDiskRO.id(),
                    lockDiskDel.id(),
                    lockStorage.id(),
                    lockSubnet.id());
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=================================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageLocks() {
    }
}
