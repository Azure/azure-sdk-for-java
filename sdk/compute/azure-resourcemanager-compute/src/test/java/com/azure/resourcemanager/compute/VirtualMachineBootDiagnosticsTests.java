// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualMachineBootDiagnosticsTests extends ComputeManagementTest {
    private String rgName = "";
    private final Region region = Region.US_WEST2;
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
        StorageAccount storageAccount = this.storageManager.storageAccounts()
            .define(generateRandomResourceName("stg", 17))
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .disableSharedKeyAccess()
            .create();

        VirtualMachine virtualMachine = computeManager.virtualMachines()
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
            .withExistingStorageAccount(storageAccount)
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
    }

    @Test
    public void canEnableBootDiagnosticsWithCreatableStorageOnManagedVMCreation() {
        final String storageName = generateRandomResourceName("st", 14);
        Creatable<StorageAccount> creatableStorageAccount = storageManager.storageAccounts()
            .define(storageName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .disableSharedKeyAccess();

        VirtualMachine virtualMachine = computeManager.virtualMachines()
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
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canEnableBootDiagnosticsWithExplicitStorageOnManagedVMCreation() {
        final String storageName = generateRandomResourceName("st", 14);
        StorageAccount storageAccount = storageManager.storageAccounts()
            .define(storageName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .disableSharedKeyAccess()
            .create();

        VirtualMachine virtualMachine = computeManager.virtualMachines()
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
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNotNull(virtualMachine.bootDiagnosticsStorageUri());
        Assertions.assertTrue(virtualMachine.bootDiagnosticsStorageUri().contains(storageName));
    }

    @Test
    public void canDisableBootDiagnostics() {
        StorageAccount storageAccount = this.storageManager.storageAccounts()
            .define(generateRandomResourceName("stg", 17))
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .disableSharedKeyAccess()
            .create();

        VirtualMachine virtualMachine = computeManager.virtualMachines()
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
            .withExistingStorageAccount(storageAccount)
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
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
    public void canEnableBootDiagnosticsOnManagedStorageAccount() {
        VirtualMachine virtualMachine = computeManager.virtualMachines()
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
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        Assertions.assertNotNull(virtualMachine);
        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNull(virtualMachine.bootDiagnosticsStorageUri());

        virtualMachine = computeManager.virtualMachines().getById(virtualMachine.id());
        virtualMachine.update().withNewDataDisk(10).apply();

        Assertions.assertTrue(virtualMachine.isBootDiagnosticsEnabled());
        Assertions.assertNull(virtualMachine.bootDiagnosticsStorageUri());
    }
}
