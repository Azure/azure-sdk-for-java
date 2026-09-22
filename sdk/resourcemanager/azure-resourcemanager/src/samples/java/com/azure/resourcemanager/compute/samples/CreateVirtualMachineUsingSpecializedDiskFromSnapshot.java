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
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.samples.SampleUtils;

/**
 * Azure Compute sample for creating a virtual machine from a specialized managed disk.
 *  - Create a virtual machine with managed OS and data disks
 *  - Snapshot the specialized OS and data disks, then create new managed disks from the snapshots
 *  - Create a new virtual machine by attaching the specialized managed disks
 */
public final class CreateVirtualMachineUsingSpecializedDiskFromSnapshot {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST2;
        final String rgName = SampleUtils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String linuxVMName1 = SampleUtils.randomResourceName(azureResourceManager, "VM1", 15);
        final String linuxVMName2 = SampleUtils.randomResourceName(azureResourceManager, "VM2", 15);
        final String osSnapshotName = SampleUtils.randomResourceName(azureResourceManager, "ss-os-", 15);
        final String dataSnapshotName = SampleUtils.randomResourceName(azureResourceManager, "ss-data-", 15);
        final String managedOSDiskName = SampleUtils.randomResourceName(azureResourceManager, "ds-os-", 15);
        final String managedDataDiskName = SampleUtils.randomResourceName(azureResourceManager, "ds-data-", 15);
        final String userName = "tirekicker";
        final String sshPublicKey = SampleUtils.sshPublicKey();

        try {
            // Create a Linux virtual machine with managed OS and data disks.
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
                .withNewDataDisk(50)
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                .create();

            // The VM's managed OS and data disks are "specialized" - they retain the machine state.
            Disk osDisk = azureResourceManager.disks().getById(linuxVM.osDiskId());
            Disk dataDisk
                = azureResourceManager.disks().getById(linuxVM.dataDisks().values().iterator().next().id());

            // Delete the virtual machine; the managed disks remain and keep their specialized state.
            azureResourceManager.virtualMachines().deleteById(linuxVM.id());

            // Snapshot the specialized OS and data disks.
            Snapshot osSnapshot = azureResourceManager.snapshots()
                .define(osSnapshotName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLinuxFromDisk(osDisk)
                .create();

            Snapshot dataSnapshot = azureResourceManager.snapshots()
                .define(dataSnapshotName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withDataFromDisk(dataDisk)
                .create();

            // Create managed disks from the specialized snapshots.
            Disk newOSDisk = azureResourceManager.disks()
                .define(managedOSDiskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLinuxFromSnapshot(osSnapshot)
                .withSizeInGB(100)
                .create();

            Disk newDataDisk = azureResourceManager.disks()
                .define(managedDataDiskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withData()
                .fromSnapshot(dataSnapshot)
                .withSizeInGB(50)
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
                .withSpecializedOSDisk(newOSDisk, OperatingSystemTypes.LINUX)
                .withExistingDataDisk(newDataDisk)
                .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
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

    private CreateVirtualMachineUsingSpecializedDiskFromSnapshot() {
    }
}
