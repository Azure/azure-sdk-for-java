// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.SnapshotSkuType;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String linuxVMName1 = Utils.randomResourceName(azureResourceManager, "VM1", 15);
        final String linuxVMName2 = Utils.randomResourceName(azureResourceManager, "VM2", 15);
        final String managedOSSnapshotName = Utils.randomResourceName(azureResourceManager, "ss-os-", 15);
        final String managedDataDiskSnapshotPrefix = Utils.randomResourceName(azureResourceManager, "ss-data-", 15);
        final String managedNewOSDiskName = Utils.randomResourceName(azureResourceManager, "ds-os-nw-", 15);
        final String managedNewDataDiskNamePrefix = Utils.randomResourceName(azureResourceManager, "ds-data-nw-", 15);

        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String publicIpDnsLabel = Utils.randomResourceName(azureResourceManager, "pip", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final Region region = Region.US_WEST_CENTRAL;

        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-libraries-for-java/master/azure-samples/src/main/resources/install_apache.sh";
        final String apacheInstallCommand = "bash install_apache.sh";
        List<String> apacheInstallScriptUris = new ArrayList<>();
        apacheInstallScriptUris.add(apacheInstallScript);

        try {
            //=============================================================
            // Create a Linux VM using a PIR image with managed OS and data disks and customize virtual
            // machine using custom script extension

            System.out.println("Creating a un-managed Linux VM");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName1)
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
            Disk osDisk = azureResourceManager.disks().getById(linuxVM.osDiskId());
            List<Disk> dataDisks = new ArrayList<>();
            for (VirtualMachineDataDisk disk : linuxVM.dataDisks().values()) {
                Disk dataDisk = azureResourceManager.disks().getById(disk.id());
                dataDisks.add(dataDisk);
            }

            //=============================================================
            // Delete the virtual machine
            System.out.println("Deleting VM: " + linuxVM.id());

            azureResourceManager.virtualMachines().deleteById(linuxVM.id());

            System.out.println("Deleted the VM");

            //=============================================================
            // Create Snapshot from the OS managed disk

            System.out.println(String.format("Creating managed snapshot from the managed disk (holding specialized OS): %s ", osDisk.id()));

            Snapshot osSnapshot = azureResourceManager.snapshots().define(managedOSSnapshotName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromDisk(osDisk)
                    .create();

            System.out.println("Created managed snapshot holding OS: " + osSnapshot.id());
            // ResourceManagerUtils.print(osSnapshot); TODO

            //=============================================================
            // Create Managed snapshot from the Data managed disks

            List<Snapshot> dataSnapshots = new ArrayList<>();
            int i = 0;
            for (Disk dataDisk : dataDisks) {
                System.out.println(String.format("Creating managed snapshot from the managed disk (holding data): %s ", dataDisk.id()));

                Snapshot dataSnapshot = azureResourceManager.snapshots().define(managedDataDiskSnapshotPrefix + "-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withDataFromDisk(dataDisk)
                        .withSku(SnapshotSkuType.STANDARD_LRS)
                        .create();
                dataSnapshots.add(dataSnapshot);

                System.out.println("Created managed snapshot holding data: " + dataSnapshot.id());
                // ResourceManagerUtils.print(dataDisk); TODO
                i++;
            }

            //=============================================================
            // Create Managed disk from the specialized OS snapshot

            System.out.println(String.format("Creating managed disk from the snapshot holding OS: %s ", osSnapshot.id()));

            Disk newOSDisk = azureResourceManager.disks().define(managedNewOSDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromSnapshot(osSnapshot)
                    .withSizeInGB(100)
                    .create();

            System.out.println("Created managed disk holding OS: " + osDisk.id());
            // ResourceManagerUtils.print(osDisk); TODO

            //=============================================================
            // Create Managed disks from the data snapshots

            List<Disk> newDataDisks = new ArrayList<>();
            i = 0;
            for (Snapshot dataSnapshot : dataSnapshots) {
                System.out.println(String.format("Creating managed disk from the Data snapshot: %s ", dataSnapshot.id()));

                Disk dataDisk = azureResourceManager.disks().define(managedNewDataDiskNamePrefix + "-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withData()
                        .fromSnapshot(dataSnapshot)
                        .create();
                newDataDisks.add(dataDisk);

                System.out.println("Created managed disk holding data: " + dataDisk.id());
                // ResourceManagerUtils.print(dataDisk); TODO
                i++;
            }

            //
            //=============================================================
            // Create a Linux VM by attaching the managed disks

            System.out.println("Creating a Linux VM using specialized OS and data disks");

            VirtualMachine linuxVM2 = azureResourceManager.virtualMachines().define(linuxVMName2)
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

            azureResourceManager.snapshots().deleteById(osSnapshot.id());

            System.out.println("Deleted OS snapshot");

            for (Snapshot dataSnapshot : dataSnapshots) {
                System.out.println("Deleting data snapshot - " + dataSnapshot.id());

                azureResourceManager.snapshots().deleteById(dataSnapshot.id());

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
            osDisk = azureResourceManager.disks().getById(linuxVM2.osDiskId());

            String osDiskSasUri = osDisk.grantAccess(24 * 60);

            System.out.println("OS disk SAS Uri: " + osDiskSasUri);

            // Data disks SAS Uri
            for (VirtualMachineDataDisk disk : linuxVM2.dataDisks().values()) {
                Disk dataDisk = azureResourceManager.disks().getById(disk.id());
                String dataDiskSasUri = dataDisk.grantAccess(24 * 60);
                System.out.println(String.format("Data disk (lun: %d) SAS Uri: %s", disk.lun(), dataDiskSasUri));
            }
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
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

    private CreateVirtualMachineUsingSpecializedDiskFromSnapshot() {
    }
}
