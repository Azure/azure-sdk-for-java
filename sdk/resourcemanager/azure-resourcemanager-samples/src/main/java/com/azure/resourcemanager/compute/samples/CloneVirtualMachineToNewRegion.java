// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.models.AzureCloud;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.samples.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create an managed virtual machine from PIR image with data disks
 *  - Create incremental snapshot from the virtual machine's OS and data disks
 *  - Copy incremental snapshots to the new region
 *  - Create managed disks from the snapshots of the new region
 *  - Create virtual machine by attaching the managed disks
 */
public final class CloneVirtualMachineToNewRegion {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String linuxVMName1 = Utils.randomResourceName(azureResourceManager, "VM1", 15);
        final String linuxVMName2 = Utils.randomResourceName(azureResourceManager, "VM2", 15);
        final String snapshotCopiedSuffix = "-snp-copied";

        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String rgNameNew = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String publicIpDnsLabel = Utils.randomResourceName(azureResourceManager, "pip", 15);
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();
        final Region region = Region.US_WEST;
        final Region regionNew = Region.US_EAST;

        try {
            //=============================================================
            // Create a Linux VM using a PIR image with managed OS and data disks and customize virtual
            // machine using custom script extension

            System.out.println("Creating a un-managed Linux VM");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines()
                .define(linuxVMName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername(userName)
                .withSsh(sshPublicKey)
                .withNewDataDisk(100)
                .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

            System.out.printf("Created a Linux VM with managed OS and data disks: %s %n", linuxVM.id());
            Utils.print(linuxVM);

            // Gets the specialized managed OS and Data disks of the virtual machine
            //
            Disk osDisk = azureResourceManager.disks().getById(linuxVM.osDiskId());
            List<Disk> dataDisks = new ArrayList<>();
            for (VirtualMachineDataDisk disk : linuxVM.dataDisks().values()) {
                Disk dataDisk = azureResourceManager.disks().getById(disk.id());
                dataDisks.add(dataDisk);
            }

            //=============================================================
            // Deallocating the virtual machine
            System.out.printf("Deallocating VM: %s %n", linuxVM.id());

            linuxVM.deallocate();

            System.out.println("Deallocated the VM");

            //=============================================================
            // Create incremental Snapshot from the OS managed disk

            System.out.printf("Creating managed snapshot from the managed disk (holding specialized OS): %s %n",
                osDisk.id());

            Snapshot osSnapshot = azureResourceManager.snapshots()
                .define(osDisk.name() + "-snp")
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLinuxFromDisk(osDisk)
                .withIncremental(true) // incremental is mandatory for snapshot to be copied across region
                .create();

            //=============================================================
            // Copy snapshots to the new region

            System.out.printf("Copying managed snapshot %s to a new region.%n", osDisk.id());

            Snapshot osSnapshotNewRegion = azureResourceManager.snapshots()
                .define(osDisk.name() + snapshotCopiedSuffix)
                .withRegion(regionNew)
                .withNewResourceGroup(rgNameNew)
                .withDataFromSnapshot(osSnapshot)
                .withCopyStart()
                .withIncremental(true)
                .create();
            osSnapshotNewRegion.awaitCopyStartCompletion();

            // Delete snapshot from old region
            azureResourceManager.snapshots().deleteById(osSnapshot.id());

            System.out.printf("Created managed snapshot holding OS: %s %n", osSnapshotNewRegion.id());

            //=============================================================
            // Create Managed snapshot from the Data managed disks

            List<Snapshot> dataSnapshots = new ArrayList<>();
            for (Disk dataDisk : dataDisks) {
                System.out.printf("Creating managed snapshot from the managed disk (holding data): %s %n",
                    dataDisk.id());

                Snapshot dataSnapshot = azureResourceManager.snapshots()
                    .define(dataDisk.name() + "-snp")
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withDataFromDisk(dataDisk)
                    .withIncremental(true) // incremental is mandatory for snapshot to be copied across region
                    .create();

                //=============================================================
                // Copy snapshots to the new region

                System.out.printf("Copying managed snapshot %s to a new region %n", dataSnapshot.id());

                Snapshot dataSnapshotNewRegion = azureResourceManager.snapshots()
                    .define(dataDisk.name() + snapshotCopiedSuffix)
                    .withRegion(regionNew)
                    .withExistingResourceGroup(rgNameNew)
                    .withDataFromSnapshot(dataSnapshot)
                    .withCopyStart()
                    .withIncremental(true)
                    .create();
                dataSnapshotNewRegion.awaitCopyStartCompletion();

                // Delete snapshot from old region
                azureResourceManager.snapshots().deleteById(dataSnapshot.id());

                dataSnapshots.add(dataSnapshotNewRegion);

                System.out.printf("Created managed snapshot holding data: %s %n", dataSnapshotNewRegion.id());
                // ResourceManagerUtils.print(dataDisk); TODO
            }

            //=============================================================
            // Create Managed disk from the specialized OS snapshot

            System.out.printf("Creating managed disk from the snapshot holding OS: %s %n", osSnapshotNewRegion.id());

            Disk newOSDisk = azureResourceManager.disks()
                .define(osSnapshotNewRegion.name().replace(snapshotCopiedSuffix, "-new"))
                .withRegion(regionNew)
                .withExistingResourceGroup(rgNameNew)
                .withLinuxFromSnapshot(osSnapshotNewRegion.id())
                .withSizeInGB(100)
                .create();

            System.out.printf("Created managed disk holding OS: %s %n", osDisk.id());
            // ResourceManagerUtils.print(osDisk); TODO

            //=============================================================
            // Create Managed disks from the data snapshots

            List<Disk> newDataDisks = new ArrayList<>();
            for (Snapshot dataSnapshot : dataSnapshots) {
                System.out.printf("Creating managed disk from the Data snapshot: %s %n", dataSnapshot.id());

                Disk dataDisk = azureResourceManager.disks()
                    .define(dataSnapshot.name().replace(snapshotCopiedSuffix, "-new"))
                    .withRegion(regionNew)
                    .withExistingResourceGroup(rgNameNew)
                    .withData()
                    .fromSnapshot(dataSnapshot.id())
                    .create();
                newDataDisks.add(dataDisk);

                System.out.printf("Created managed disk holding data: %s %n", dataDisk.id());
                // ResourceManagerUtils.print(dataDisk); TODO
            }

            //
            //=============================================================
            // Create a Linux VM by attaching the managed disks

            System.out.println("Creating a Linux VM using specialized OS and data disks");

            VirtualMachine linuxVM2 = azureResourceManager.virtualMachines()
                .define(linuxVMName2)
                .withRegion(regionNew)
                .withExistingResourceGroup(rgNameNew)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withSpecializedOSDisk(newOSDisk, OperatingSystemTypes.LINUX)
                .withExistingDataDisk(newDataDisks.get(0))
                .withExistingDataDisk(newDataDisks.get(1), 1, CachingTypes.READ_WRITE)
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

            Utils.print(linuxVM2);

            //=============================================================
            //
            System.out.printf("Deleting OS snapshot - %s %n", osSnapshotNewRegion.id());

            azureResourceManager.snapshots().deleteById(osSnapshotNewRegion.id());

            System.out.println("Deleted OS snapshot");

            for (Snapshot dataSnapshot : dataSnapshots) {
                System.out.printf("Deleting data snapshot - %s %n", dataSnapshot.id());

                azureResourceManager.snapshots().deleteById(dataSnapshot.id());

                System.out.println("Deleted data snapshot");
            }

            System.out.printf("De-allocating the virtual machine - %s %n", linuxVM2.id());

            linuxVM2.deallocate();
            return true;
        } finally {
            System.out.printf("Deleting Resource Group: %s %n", rgName);
            azureResourceManager.resourceGroups().beginDeleteByName(rgName);
            System.out.printf("Deleting Resource Group: %s %n", rgNameNew);
            azureResourceManager.resourceGroups().beginDeleteByName(rgNameNew);
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureCloud.AZURE_PUBLIC_CLOUD);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager.configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private CloneVirtualMachineToNewRegion() {
    }
}
