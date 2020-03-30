/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineBootDiagnosticsTests extends ComputeManagementTest {
    private String RG_NAME = "";
    private final Region REGION = Region.US_SOUTH_CENTRAL;
    private final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    public void canEnableBootDiagnosticsWithImplicitStorageOnManagedVMCreation() {
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnManagedVMCreation() {
        final String storageName = sdkContext.randomResourceName("st", 14);
        Creatable<StorageAccount> creatableStorageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withBootDiagnostics(creatableStorageAccount)
                .create();
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithExplicitStorageOnManagedVMCreation() {
        final String storageName = sdkContext.randomResourceName("st", 14);
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .create();

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withBootDiagnostics(storageAccount)
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canDisableBootDiagnostics() {
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orLX")
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());

        virtualMachine.update()
                .withoutBootDiagnostics()
                .apply();

        Assertions.assertFalse(virtualMachine.isBootDiagnosticsEnabled());
        // Disabling boot diagnostics will not remove the storage uri from the vm payload.
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
    }

    @Test
    public void bootDiagnosticsShouldUsesOSUnManagedDiskImplicitStorage() {
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withUnmanagedDisks()   // The implicit storage account for OS disk should be used for boot diagnostics as well
                .withBootDiagnostics()
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertNotNull(virtualMachine.osUnmanagedDiskVhdUri());
        Assertions.assertTrue(virtualMachine.osUnmanagedDiskVhdUri().toLowerCase().startsWith(virtualMachine.bootDiagnosticsStorageUri().toLowerCase()));
    }

    @Test
    public void bootDiagnosticsShouldUseUnManagedDisksExplicitStorage() {
        final String storageName = sdkContext.randomResourceName("st", 14);
        StorageAccount storageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .create();

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .withBootDiagnostics()
                .withExistingStorageAccount(storageAccount) // This storage account must be shared by disk and boot diagnostics
                .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithImplicitStorageOnUnManagedVMCreation() {
        VirtualMachine virtualMachine1 = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .create();

        String osDiskVhd = virtualMachine1.osUnmanagedDiskVhdUri();
        Assertions.assertNotNull(osDiskVhd);
        computeManager.virtualMachines().deleteById(virtualMachine1.id());

        VirtualMachine virtualMachine2 = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withSpecializedOSUnmanagedDisk(osDiskVhd, OperatingSystemTypes.LINUX)
                .withBootDiagnostics()  // A new storage account should be created and used
                .create();

        Assertions.assertNotNull(virtualMachine2);
        Assertions.assertTrue(virtualMachine2.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine2.bootDiagnosticsStorageUri());
        Assertions.assertNotNull(virtualMachine2.osUnmanagedDiskVhdUri());
        Assertions.assertFalse(virtualMachine2.osUnmanagedDiskVhdUri().toLowerCase().startsWith(virtualMachine2.bootDiagnosticsStorageUri().toLowerCase()));
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnUnManagedVMCreation() {
        final String storageName = sdkContext.randomResourceName("st", 14);
        Creatable<StorageAccount> creatableStorageAccount = storageManager.storageAccounts()
                .define(storageName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME);

        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .withBootDiagnostics(creatableStorageAccount)  // This storage account should be used for BDiagnostics not OS disk storage account
                .create();
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
        // There should be a different storage account created for the OS Disk
        Assertions.assertFalse(virtualMachine.osUnmanagedDiskVhdUri().toLowerCase().startsWith(virtualMachine.bootDiagnosticsStorageUri().toLowerCase()));
    }
}
