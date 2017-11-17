/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class VirtualMachineBootDiagnosticsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static final Region REGION = Region.US_SOUTH_CENTRAL;
    private static final String VMNAME = "javavm";

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

        Assert.assertNotNull(virtualMachine);
        Assert.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnManagedVMCreation() {
        final String storageName = SdkContext.randomResourceName("st", 14);
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
        Assert.assertNotNull(virtualMachine);
        Assert.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assert.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithExplicitStorageOnManagedVMCreation() {
        final String storageName = SdkContext.randomResourceName("st", 14);
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

        Assert.assertNotNull(virtualMachine);
        Assert.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assert.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
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

        Assert.assertNotNull(virtualMachine);
        Assert.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());

        virtualMachine.update()
                .withoutBootDiagnostics()
                .apply();

        Assert.assertFalse(virtualMachine.isBootDiagnosticsEnabled());
        // Disabling boot diagnostics will not remove the storage uri from the vm payload.
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
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

        Assert.assertNotNull(virtualMachine);
        Assert.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assert.assertNotNull(virtualMachine.osUnmanagedDiskVhdUri());
        Assert.assertTrue(virtualMachine.osUnmanagedDiskVhdUri().toLowerCase().startsWith(virtualMachine.bootDiagnosticsStorageUri().toLowerCase()));
    }

    @Test
    public void bootDiagnosticsShouldUseUnManagedDisksExplicitStorage() {
        final String storageName = SdkContext.randomResourceName("st", 14);
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

        Assert.assertNotNull(virtualMachine);
        Assert.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assert.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
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
        Assert.assertNotNull(osDiskVhd);
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

        Assert.assertNotNull(virtualMachine2);
        Assert.assertTrue(virtualMachine2.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine2.bootDiagnosticsStorageUri());
        Assert.assertNotNull(virtualMachine2.osUnmanagedDiskVhdUri());
        Assert.assertFalse(virtualMachine2.osUnmanagedDiskVhdUri().toLowerCase().startsWith(virtualMachine2.bootDiagnosticsStorageUri().toLowerCase()));
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnUnManagedVMCreation() {
        final String storageName = SdkContext.randomResourceName("st", 14);
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
        Assert.assertNotNull(virtualMachine);
        Assert.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assert.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assert.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
        // There should be a different storage account created for the OS Disk
        Assert.assertFalse(virtualMachine.osUnmanagedDiskVhdUri().toLowerCase().startsWith(virtualMachine.bootDiagnosticsStorageUri().toLowerCase()));
    }
}
