// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.models.AzureCloud;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.samples.SampleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for creating a virtual machine from a specialized VHD.
 *  - Create a virtual machine with un-managed OS and data disks
 *  - Create managed disks from the specialized un-managed OS and data VHDs
 *  - Create a new virtual machine by attaching the managed disks
 */
public final class CreateVirtualMachineUsingSpecializedDiskFromVhd {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST;
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String linuxVMName1 = SampleUtils.randomResourceName(azureResourceManager, "VM1", 15);
        final String linuxVMName2 = SampleUtils.randomResourceName(azureResourceManager, "VM2", 15);
        final String managedOSDiskName = SampleUtils.randomResourceName(azureResourceManager, "ds-os-", 15);
        final String managedDataDiskName = SampleUtils.randomResourceName(azureResourceManager, "ds-data-", 15);
        final String storageAccountName = SampleUtils.randomResourceName(azureResourceManager, "stg", 15);
        final String userName = "tirekicker";
        final String sshPublicKey = SampleUtils.sshPublicKey();

        try {
            // Create a Linux virtual machine using un-managed OS and data disks (VHDs in a storage account).
            VirtualMachine linuxVM = azureResourceManager.virtualMachines()
                .define(linuxVMName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS)
                .withRootUsername(userName)
                .withSsh(sshPublicKey)
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                .withNewVhd(50)
                .withLun(1)
                .attach()
                .withNewStorageAccount(storageAccountName)
                .withSize(VirtualMachineSizeTypes.STANDARD_D2_V3)
                .create();

            // Collect the specialized OS and data disk VHD URIs before deleting the virtual machine.
            String specializedOSVhdUri = linuxVM.osUnmanagedDiskVhdUri();
            List<String> dataVhdUris = new ArrayList<>();
            for (VirtualMachineUnmanagedDataDisk dataDisk : linuxVM.unmanagedDataDisks().values()) {
                dataVhdUris.add(dataDisk.vhdUri());
            }

            azureResourceManager.virtualMachines().deleteById(linuxVM.id());

            // Create a managed disk from the specialized OS VHD.
            Disk osDisk = azureResourceManager.disks()
                .define(managedOSDiskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLinuxFromVhd(specializedOSVhdUri)
                .withStorageAccountName(storageAccountName)
                .withSizeInGB(100)
                .create();

            // Create a managed disk from the specialized data VHD.
            Disk dataDisk = azureResourceManager.disks()
                .define(managedDataDiskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withData()
                .fromVhd(dataVhdUris.get(0))
                .withStorageAccountName(storageAccountName)
                .withSizeInGB(150)
                .withSku(DiskSkuTypes.STANDARD_LRS)
                .create();

            // Create a new virtual machine by attaching the specialized managed disks.
            VirtualMachine linuxVM2 = azureResourceManager.virtualMachines()
                .define(linuxVMName2)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withSpecializedOSDisk(osDisk, OperatingSystemTypes.LINUX)
                .withExistingDataDisk(dataDisk)
                .withSize(VirtualMachineSizeTypes.STANDARD_D2_V3)
                .create();

            System.out.println("Created virtual machine from specialized disks: " + linuxVM2.id());
            return true;
        } finally {
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            runSample(azureResourceManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CreateVirtualMachineUsingSpecializedDiskFromVhd() {
    }
}
