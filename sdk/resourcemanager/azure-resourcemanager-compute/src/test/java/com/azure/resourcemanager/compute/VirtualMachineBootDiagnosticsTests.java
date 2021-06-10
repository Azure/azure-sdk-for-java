// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.core.management.Region;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineBootDiagnosticsTests extends ComputeManagementTest {
    private String rgName = "";
    private final Region region = Region.US_SOUTH_CENTRAL;
    private final String vmName = "javavm";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canEnableBootDiagnosticsWithImplicitStorageOnManagedVMCreation() {
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnManagedVMCreation() {
        final String storageName = generateRandomResourceName("st", 14);
        Creatable<StorageAccount> creatableStorageAccount =
            storageManager.storageAccounts().define(storageName).withRegion(region).withNewResourceGroup(rgName);

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withBootDiagnostics(creatableStorageAccount)
                .create();
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithExplicitStorageOnManagedVMCreation() {
        final String storageName = generateRandomResourceName("st", 14);
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(storageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .create();

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withBootDiagnostics(storageAccount)
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canDisableBootDiagnostics() {
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());

        virtualMachine.update().withoutBootDiagnostics().apply();

        Assertions.assertFalse(virtualMachine.isBootDiagnosticsEnabled());
        // Disabling boot diagnostics will not remove the storage uri from the vm payload.
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
    }

    @Test
    public void bootDiagnosticsShouldUsesOSUnManagedDiskImplicitStorage() {
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withUnmanagedDisks() // The implicit storage account for OS disk should be used for boot diagnostics as
                                      // well
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertNotNull(virtualMachine.osUnmanagedDiskVhdUri());
        Assertions
            .assertTrue(
                virtualMachine
                    .osUnmanagedDiskVhdUri()
                    .toLowerCase()
                    .startsWith(virtualMachine.bootDiagnosticsStorageUri().toLowerCase()));
    }

    @Test
    public void bootDiagnosticsShouldUseUnManagedDisksExplicitStorage() {
        final String storageName = generateRandomResourceName("st", 14);
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(storageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .create();

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .withBootDiagnostics()
                .withExistingStorageAccount(
                    storageAccount) // This storage account must be shared by disk and boot diagnostics
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithImplicitStorageOnUnManagedVMCreation() {
        VirtualMachine virtualMachine1 =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .create();

        String osDiskVhd = virtualMachine1.osUnmanagedDiskVhdUri();
        Assertions.assertNotNull(osDiskVhd);
        computeManager.virtualMachines().deleteById(virtualMachine1.id());

        VirtualMachine virtualMachine2 =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withSpecializedOSUnmanagedDisk(osDiskVhd, OperatingSystemTypes.LINUX)
                .withBootDiagnostics() // A new storage account should be created and used
                .create();

        Assertions.assertNotNull(virtualMachine2);
        Assertions.assertTrue(virtualMachine2.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine2.bootDiagnosticsStorageUri());
        Assertions.assertNotNull(virtualMachine2.osUnmanagedDiskVhdUri());
        Assertions
            .assertFalse(
                virtualMachine2
                    .osUnmanagedDiskVhdUri()
                    .toLowerCase()
                    .startsWith(virtualMachine2.bootDiagnosticsStorageUri().toLowerCase()));
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnUnManagedVMCreation() {
        final String storageName = generateRandomResourceName("st", 14);
        Creatable<StorageAccount> creatableStorageAccount =
            storageManager.storageAccounts().define(storageName).withRegion(region).withNewResourceGroup(rgName);

        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .withBootDiagnostics(
                    creatableStorageAccount) // This storage account should be used for BDiagnostics not OS disk storage
                                             // account
                .create();
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
        // There should be a different storage account created for the OS Disk
        Assertions
            .assertFalse(
                virtualMachine
                    .osUnmanagedDiskVhdUri()
                    .toLowerCase()
                    .startsWith(virtualMachine.bootDiagnosticsStorageUri().toLowerCase()));
    }

    @Test
    public void canEnableBootDiagnosticsOnManagedStorageAccount() {
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("jvuser")
                .withSsh(sshPublicKey())
                .withBootDiagnosticsOnManagedStorageAccount()
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNull(virtualMachine.bootDiagnosticsStorageUri());

        virtualMachine = computeManager.virtualMachines().getById(virtualMachine.id());
        virtualMachine.update()
            .withNewDataDisk(10)
            .apply();

        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNull(virtualMachine.bootDiagnosticsStorageUri());
    }
}
