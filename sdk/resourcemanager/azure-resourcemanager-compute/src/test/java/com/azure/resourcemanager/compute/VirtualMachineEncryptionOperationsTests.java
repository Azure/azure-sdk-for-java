// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DiskVolumeEncryptionMonitor;
import com.azure.resourcemanager.compute.models.EncryptionStatus;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.LinuxVMDiskEncryptionConfiguration;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.keyvault.models.Vault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class VirtualMachineEncryptionOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private final Region region = Region.US_EAST;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("vmencryptst", 18);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    @Disabled("Requires manually creating service principal and setting SP credentials in the test")
    public void canEncryptVirtualMachineLegacy() {
        // https://docs.microsoft.com/azure/virtual-machines/linux/disk-encryption-overview
        //
        // KeyVault Resource ID
        String keyVaultId = "KEY_VAULT_ID_HERE";
        // Azure AD service principal client (application) ID
        String aadClientId = "AAD_APPLICATION_ID_HERE";
        // Azure AD service principal client secret
        String aadSecret = "AAD_CLIENT_SECRET_HERE";

        final String vmName1 = "myvm1";
        final String uname = "juser";
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername(uname)
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        DiskVolumeEncryptionMonitor monitor1 = virtualMachine.diskEncryption().getMonitor();
        Assertions.assertNotNull(monitor1);
        Assertions.assertNotNull(monitor1.osDiskStatus());
        Assertions.assertNotNull(monitor1.dataDiskStatus());
        Assertions.assertEquals(EncryptionStatus.NOT_ENCRYPTED, monitor1.osDiskStatus());
        Assertions.assertEquals(EncryptionStatus.NOT_ENCRYPTED, monitor1.dataDiskStatus());
        DiskVolumeEncryptionMonitor monitor2 =
            virtualMachine.diskEncryption().enable(keyVaultId, aadClientId, aadSecret);
        Assertions.assertNotNull(monitor2);
        Assertions.assertNotNull(monitor2.osDiskStatus());
        Assertions.assertNotNull(monitor2.dataDiskStatus());
        monitor1.refresh();
        Assertions.assertEquals(monitor1.osDiskStatus(), monitor2.osDiskStatus());
        Assertions.assertEquals(monitor1.dataDiskStatus(), monitor2.dataDiskStatus());
        monitor2.refresh();
        Assertions.assertNotEquals(EncryptionStatus.NOT_ENCRYPTED, monitor2.osDiskStatus());
    }


    @Test
    public void canEncryptVirtualMachine() {
        // https://docs.microsoft.com/azure/virtual-machines/linux/disk-encryption-overview

        final String vmName1 = "myvm1";
        final String uname = "juser";
        VirtualMachine virtualMachine = computeManager.virtualMachines()
            .define(vmName1)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername(uname)
            .withSsh(sshPublicKey())
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))    // ADE need 8GB
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .create();

        final String vaultName = generateRandomResourceName("vault", 20);
        Vault vault = keyVaultManager.vaults().define(vaultName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withEmptyAccessPolicy()
            .withDiskEncryptionEnabled()
            .create();
        final String vaultId = vault.id();
        final String vaultUri = vault.vaultUri();

        DiskVolumeEncryptionMonitor monitor1 = virtualMachine.diskEncryption().getMonitor();
        Assertions.assertNotNull(monitor1);
        Assertions.assertNotNull(monitor1.osDiskStatus());
        Assertions.assertNotNull(monitor1.dataDiskStatus());
        Assertions.assertEquals(EncryptionStatus.NOT_ENCRYPTED, monitor1.osDiskStatus());
        Assertions.assertEquals(EncryptionStatus.NOT_ENCRYPTED, monitor1.dataDiskStatus());
        DiskVolumeEncryptionMonitor monitor2 = virtualMachine
            .diskEncryption()
            .enable(new LinuxVMDiskEncryptionConfiguration(vaultId, vaultUri));
        Assertions.assertNotNull(monitor2);
        Assertions.assertNotNull(monitor2.osDiskStatus());
        Assertions.assertNotNull(monitor2.dataDiskStatus());
        monitor1.refresh();
        Assertions.assertEquals(monitor1.osDiskStatus(), monitor2.osDiskStatus());
        Assertions.assertEquals(monitor1.dataDiskStatus(), monitor2.dataDiskStatus());
        monitor2.refresh();
        Assertions.assertNotEquals(EncryptionStatus.NOT_ENCRYPTED, monitor2.osDiskStatus());
    }
}
