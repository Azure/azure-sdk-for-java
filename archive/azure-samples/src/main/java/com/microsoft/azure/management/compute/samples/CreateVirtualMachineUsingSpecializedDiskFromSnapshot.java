/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.DiskSkuTypes;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.Snapshot;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create an managed virtual machine from PIR image with data disks
 *  - Create snapshot from the virtual machine's OS and data disks
 *  - Create managed disks from the snapshots
 *  - Create virtual machine by attaching the managed disks
 *  - Get SAS Uri to the virtual machine's managed disks.
 */
public final class CreateVirtualMachineUsingSpecializedDiskFromSnapshot {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String linuxVMName1 = Utils.createRandomName("VM1");
        final String linuxVMName2 = Utils.createRandomName("VM2");
        final String managedOSSnapshotName = Utils.createRandomName("ss-os-");
        final String managedDataDiskSnapshotPrefix = Utils.createRandomName("ss-data-");
        final String managedNewOSDiskName = Utils.createRandomName("ds-os-nw-");
        final String managedNewDataDiskNamePrefix = Utils.createRandomName("ds-data-nw-");

        final String rgName = Utils.createRandomName("rgCOMV");
        final String publicIpDnsLabel = Utils.createRandomName("pip");
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";
        final Region region = Region.US_WEST_CENTRAL;

        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/master/azure-samples/src/main/resources/install_apache.sh";
        final String apacheInstallCommand = "bash install_apache.sh";
        List<String> apacheInstallScriptUris = new ArrayList<>();
        apacheInstallScriptUris.add(apacheInstallScript);

        try {
            //=============================================================
            // Create a Linux VM using a PIR image with managed OS and data disks and customize virtual
            // machine using custom script extension

            System.out.println("Creating a un-managed Linux VM");

            VirtualMachine linuxVM = azure.virtualMachines().define(linuxVMName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withNewDataDisk(100)
                    .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", apacheInstallScriptUris)
                        .withPublicSetting("commandToExecute", apacheInstallCommand)
                        .attach()
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created a Linux VM with managed OS and data disks: " + linuxVM.id());
            Utils.print(linuxVM);

            // Gets the specialized managed OS and Data disks of the virtual machine
            //
            Disk osDisk = azure.disks().getById(linuxVM.osDiskId());
            List<Disk> dataDisks = new ArrayList<>();
            for (VirtualMachineDataDisk disk : linuxVM.dataDisks().values()) {
                Disk dataDisk = azure.disks().getById(disk.id());
                dataDisks.add(dataDisk);
            }

            //=============================================================
            // Delete the virtual machine
            System.out.println("Deleting VM: " + linuxVM.id());

            azure.virtualMachines().deleteById(linuxVM.id());

            System.out.println("Deleted the VM");

            //=============================================================
            // Create Snapshot from the OS managed disk

            System.out.println(String.format("Creating managed snapshot from the managed disk (holding specialized OS): %s ", osDisk.id()));

            Snapshot osSnapshot = azure.snapshots().define(managedOSSnapshotName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromDisk(osDisk)
                    .create();

            System.out.println("Created managed snapshot holding OS: " + osSnapshot.id());
            // Utils.print(osSnapshot); TODO

            //=============================================================
            // Create Managed snapshot from the Data managed disks

            List<Snapshot> dataSnapshots = new ArrayList<>();
            int i = 0;
            for (Disk dataDisk : dataDisks) {
                System.out.println(String.format("Creating managed snapshot from the managed disk (holding data): %s ", dataDisk.id()));

                Snapshot dataSnapshot = azure.snapshots().define(managedDataDiskSnapshotPrefix + "-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withDataFromDisk(dataDisk)
                        .withSku(DiskSkuTypes.STANDARD_LRS)
                        .create();
                dataSnapshots.add(dataSnapshot);

                System.out.println("Created managed snapshot holding data: " + dataSnapshot.id());
                // Utils.print(dataDisk); TODO
                i++;
            }

            //=============================================================
            // Create Managed disk from the specialized OS snapshot

            System.out.println(String.format("Creating managed disk from the snapshot holding OS: %s ", osSnapshot.id()));

            Disk newOSDisk = azure.disks().define(managedNewOSDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromSnapshot(osSnapshot)
                    .withSizeInGB(100)
                    .create();

            System.out.println("Created managed disk holding OS: " + osDisk.id());
            // Utils.print(osDisk); TODO

            //=============================================================
            // Create Managed disks from the data snapshots

            List<Disk> newDataDisks = new ArrayList<>();
            i = 0;
            for (Snapshot dataSnapshot : dataSnapshots) {
                System.out.println(String.format("Creating managed disk from the Data snapshot: %s ", dataSnapshot.id()));

                Disk dataDisk = azure.disks().define(managedNewDataDiskNamePrefix + "-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withData()
                        .fromSnapshot(dataSnapshot)
                        .create();
                newDataDisks.add(dataDisk);

                System.out.println("Created managed disk holding data: " + dataDisk.id());
                // Utils.print(dataDisk); TODO
                i++;
            }

            //
            //=============================================================
            // Create a Linux VM by attaching the managed disks

            System.out.println("Creating a Linux VM using specialized OS and data disks");

            VirtualMachine linuxVM2 = azure.virtualMachines().define(linuxVMName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSDisk(newOSDisk, OperatingSystemTypes.LINUX)
                    .withExistingDataDisk(newDataDisks.get(0))
                    .withExistingDataDisk(newDataDisks.get(1), 1, CachingTypes.READ_WRITE)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            Utils.print(linuxVM2);

            //=============================================================
            //
            System.out.println("Deleting OS snapshot - " + osSnapshot.id());

            azure.snapshots().deleteById(osSnapshot.id());

            System.out.println("Deleted OS snapshot");

            for (Snapshot dataSnapshot : dataSnapshots) {
                System.out.println("Deleting data snapshot - " + dataSnapshot.id());

                azure.snapshots().deleteById(dataSnapshot.id());

                System.out.println("Deleted data snapshot");
            }

            // Getting the SAS URIs requires virtual machines to be de-allocated
            // [Access not permitted because'disk' is currently attached to running VM]
            //
            System.out.println("De-allocating the virtual machine - " + linuxVM2.id());

            linuxVM2.deallocate();

            //=============================================================
            // Get the readonly SAS URI to the OS and data disks

            System.out.println("Getting OS and data disks SAS Uris");

            // OS Disk SAS Uri
            osDisk = azure.disks().getById(linuxVM2.osDiskId());

            String osDiskSasUri = osDisk.grantAccess(24 * 60);

            System.out.println("OS disk SAS Uri: " + osDiskSasUri);

            // Data disks SAS Uri
            for (VirtualMachineDataDisk disk : linuxVM2.dataDisks().values()) {
                Disk dataDisk = azure.disks().getById(disk.id());
                String dataDiskSasUri = dataDisk.grantAccess(24 * 60);
                System.out.println(String.format("Data disk (lun: %d) SAS Uri: %s", disk.lun(), dataDiskSasUri));
            }
            return true;
        } catch (Exception f) {
            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
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
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private CreateVirtualMachineUsingSpecializedDiskFromSnapshot() {
    }
}
