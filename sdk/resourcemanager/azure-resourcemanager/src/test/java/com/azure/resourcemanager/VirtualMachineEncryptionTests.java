// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DeleteOptions;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.compute.models.EncryptionType;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDiskOptions;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineEncryptionTests extends DiskEncryptionTestBase {

    @Test
    public void canCreateVirtualMachineWithDiskEncryptionSet() {
        final String clientId = this.clientIdFromFile();

        // create vault and key
        final String vaultName = generateRandomResourceName("kv", 8);
        VaultAndKey vaultAndKey = createVaultAndKey(vaultName, clientId);

        // create disk encryption set
        DiskEncryptionSet diskEncryptionSet = createDiskEncryptionSet("des1",
            DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS, vaultAndKey);

        DiskEncryptionSet diskEncryptionSet2 = createDiskEncryptionSet("des2",
            DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY, vaultAndKey);

        // create disk
        Disk disk1 = azureResourceManager.disks().define("disk1")
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withData()
            .withSizeInGB(32)
            .withDiskEncryptionSet(diskEncryptionSet.id())
            .create();

        Assertions.assertEquals(EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS, disk1.encryption().type());
        assertResourceIdEquals(diskEncryptionSet.id(), disk1.encryption().diskEncryptionSetId());

        final String vmName = "javavm";

        // create virtual machine
        VirtualMachine vm = azureResourceManager.virtualMachines().define(vmName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/27")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("testuser")
            .withSsh(sshPublicKey())
            .withNewDataDisk(16, 0, new VirtualMachineDiskOptions()
                .withDeleteOptions(DeleteOptions.DETACH)
                .withDiskEncryptionSet(null))
            .withExistingDataDisk(disk1)
            .withDataDiskDefaultDeleteOptions(DeleteOptions.DELETE)
            .withDataDiskDefaultDiskEncryptionSet(diskEncryptionSet.id())
            .withOSDiskDeleteOptions(DeleteOptions.DELETE)
            .withOSDiskDiskEncryptionSet(diskEncryptionSet.id())
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        // verification
        Assertions.assertEquals(diskEncryptionSet.id(), vm.osDiskDiskEncryptionSetId());
        Assertions.assertNull(vm.dataDisks().get(0).diskEncryptionSetId());
        assertResourceIdEquals(diskEncryptionSet.id(), vm.dataDisks().get(1).diskEncryptionSetId());
        Assertions.assertEquals(DeleteOptions.DETACH, vm.dataDisks().get(0).deleteOptions());
        Assertions.assertEquals(DeleteOptions.DELETE, vm.dataDisks().get(1).deleteOptions());

        // create disk with disk encryption set
        Disk disk2 = azureResourceManager.disks().define("disk2")
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withData()
            .withSizeInGB(32)
            .create();

        Assertions.assertEquals(EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_KEY, disk2.encryption().type());
        Assertions.assertNull(disk2.encryption().diskEncryptionSetId());

        disk2.update()
            .withDiskEncryptionSet(diskEncryptionSet.id(), EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS)
            .apply();

        Assertions.assertEquals(EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS, disk2.encryption().type());
        assertResourceIdEquals(diskEncryptionSet.id(), disk2.encryption().diskEncryptionSetId());

        // update virtual machine
        vm.update()
            .withoutDataDisk(0)
            .withoutDataDisk(1)
            .withExistingDataDisk(disk2, 32, 2, new VirtualMachineDiskOptions()
                .withDeleteOptions(DeleteOptions.DELETE))
            .withNewDataDisk(16, 3, CachingTypes.NONE)
            .withDataDiskDefaultDeleteOptions(DeleteOptions.DETACH)
            .apply();

        // verification
        assertResourceIdEquals(diskEncryptionSet.id(), vm.dataDisks().get(2).diskEncryptionSetId());
        Assertions.assertNull(vm.dataDisks().get(3).diskEncryptionSetId());
        Assertions.assertEquals(DeleteOptions.DELETE, vm.dataDisks().get(2).deleteOptions());
        Assertions.assertEquals(DeleteOptions.DETACH, vm.dataDisks().get(3).deleteOptions());

        // stop VM and convert disk to CMK
        vm.deallocate();
        Disk disk = azureResourceManager.disks().getById(vm.dataDisks().get(3).id());
        disk.update()
            .withDiskEncryptionSet(diskEncryptionSet.id(), EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS)
            .apply();
        vm.start();
        vm.refresh();
        assertResourceIdEquals(diskEncryptionSet.id(), vm.dataDisks().get(3).diskEncryptionSetId());

        // update virtual machine
        vm.update()
            .withoutDataDisk(2)
            .withoutDataDisk(3)
            .withNewDataDisk(16, 0, new VirtualMachineDiskOptions()
                .withDeleteOptions(DeleteOptions.DELETE)
                .withDiskEncryptionSet(diskEncryptionSet.id()))
            .withNewDataDisk(32, 1, CachingTypes.NONE)
            .withDataDiskDefaultDiskEncryptionSet(diskEncryptionSet2.id())
            .apply();

        assertResourceIdEquals(diskEncryptionSet.id(), vm.dataDisks().get(0).diskEncryptionSetId());
        assertResourceIdEquals(diskEncryptionSet2.id(), vm.dataDisks().get(1).diskEncryptionSetId());
        Assertions.assertEquals(DeleteOptions.DELETE, vm.dataDisks().get(0).deleteOptions());
        Assertions.assertEquals(DeleteOptions.DETACH, vm.dataDisks().get(1).deleteOptions());

        disk = azureResourceManager.disks().getById(vm.dataDisks().get(0).id());
        Assertions.assertEquals(EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_AND_CUSTOMER_KEYS, disk.encryption().type());
        disk = azureResourceManager.disks().getById(vm.dataDisks().get(1).id());
        Assertions.assertEquals(EncryptionType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY, disk.encryption().type());

        // delete virtual machine
        azureResourceManager.virtualMachines().deleteById(vm.id());
    }
}
