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
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines.
 *  - Create an un-managed virtual machine from PIR image with data disks
 *  - Create managed disks from specialized un-managed OS and Data disk of virtual machine
 *  - Create a virtual machine by attaching the managed disks
 *  - Get SAS Uri to the virtual machine's managed disks
 */
public class CreateVirtualMachineUsingSpecializedDiskFromVhd {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String linuxVMName1 = Utils.randomResourceName(azureResourceManager, "VM1", 15);
        final String linuxVMName2 = Utils.randomResourceName(azureResourceManager, "VM2", 15);
        final String managedOSDiskName = Utils.randomResourceName(azureResourceManager, "ds-os-", 15);
        final String managedDataDiskNamePrefix = Utils.randomResourceName(azureResourceManager, "ds-data-", 15);
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String publicIpDnsLabel = Utils.randomResourceName(azureResourceManager, "pip", 15);
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "stg", 15);
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();
        final Region region = Region.US_WEST;

        final String apacheInstallScript = "https://raw.githubusercontent.com/Azure/azure-sdk-for-java/main/sdk/resourcemanager/azure-resourcemanager-samples/src/main/resources/install_apache.sh";
        final String apacheInstallCommand = "bash install_apache.sh";
        List<String> apacheInstallScriptUris = new ArrayList<>();
        apacheInstallScriptUris.add(apacheInstallScript);
        try {
            //=============================================================
            // Create a Linux VM using an image from PIR (Platform Image Repository)

            System.out.println("Creating a un-managed Linux VM");

            VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshPublicKey)
                    .withUnmanagedDisks()
                    .defineUnmanagedDataDisk("disk-1")
                        .withNewVhd(50)
                        .withLun(1)
                        .attach()
                    .defineUnmanagedDataDisk("disk-2")
                        .withNewVhd(50)
                        .withLun(2)
                        .attach()
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", apacheInstallScriptUris)
                        .withPublicSetting("commandToExecute", apacheInstallCommand)
                        .attach()
                    .withNewStorageAccount(storageAccountName)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created a Linux VM with un-managed OS and data disks: " + linuxVM.id());
            Utils.print(linuxVM);

            // Gets the specialized OS and Data disk VHDs of the virtual machine
            //
            String specializedOSVhdUri = linuxVM.osUnmanagedDiskVhdUri();
            List<String> dataVhdUris = new ArrayList<>();
            for (VirtualMachineUnmanagedDataDisk dataDisk : linuxVM.unmanagedDataDisks().values()) {
                dataVhdUris.add(dataDisk.vhdUri());
            }

            //=============================================================
            // Delete the virtual machine
            System.out.println("Deleting VM: " + linuxVM.id());

            azureResourceManager.virtualMachines().deleteById(linuxVM.id());

            System.out.println("Deleted the VM");

            //=============================================================
            // Create Managed disk from the specialized OS VHD

            System.out.println(String.format("Creating managed disk from the specialized OS VHD: %s ", specializedOSVhdUri));

            Disk osDisk = azureResourceManager.disks().define(managedOSDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromVhd(specializedOSVhdUri)
                    .withStorageAccountName(storageAccountName)
                    .withSizeInGB(100)
                    .create();

            System.out.println("Created managed disk holding OS: " + osDisk.id());
            // ResourceManagerUtils.print(osDisk); TODO

            //=============================================================
            // Create Managed disks from the Data VHDs

            List<Disk> dataDisks = new ArrayList<>();
            int i = 0;
            for (String dataVhdUri : dataVhdUris) {
                System.out.println(String.format("Creating managed disk from the Data VHD: %s ", dataVhdUri));

                Disk dataDisk = azureResourceManager.disks().define(managedDataDiskNamePrefix + "-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withData()
                        .fromVhd(dataVhdUri)
                        .withStorageAccountName(storageAccountName)
                        .withSizeInGB(150)
                        .withSku(DiskSkuTypes.STANDARD_LRS)
                        .create();
                dataDisks.add(dataDisk);

                System.out.println("Created managed disk holding data: " + dataDisk.id());
                // ResourceManagerUtils.print(dataDisk); TODO
                i++;
            }

            //=============================================================
            // Create a Linux VM by attaching the disks

            System.out.println("Creating a Linux VM using specialized OS and data disks");

            VirtualMachine linuxVM2 = azureResourceManager.virtualMachines().define(linuxVMName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSDisk(osDisk, OperatingSystemTypes.LINUX)
                    .withExistingDataDisk(dataDisks.get(0))
                    .withExistingDataDisk(dataDisks.get(1), 1, CachingTypes.READ_WRITE)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            Utils.print(linuxVM2);

            List<String> dataDiskIds = new ArrayList<>();
            for (VirtualMachineDataDisk disk : linuxVM2.dataDisks().values()) {
                dataDiskIds.add(disk.id());
            }

            //=============================================================
            // Detach the data disks from the virtual machine

            System.out.println("Updating VM by detaching the data disks");

            linuxVM2.update()
                    .withoutDataDisk(0)
                    .withoutDataDisk(1)
                    .apply();

            Utils.print(linuxVM2);

            //=============================================================
            // Get the readonly SAS URI to the data disks
            System.out.println("Getting data disks SAS Uris");

            for (String diskId : dataDiskIds) {
                Disk dataDisk = azureResourceManager.disks().getById(diskId);
                String dataDiskSasUri = dataDisk.grantAccess(24 * 60);
                System.out.println(String.format("Data disk SAS Uri: %s", dataDiskSasUri));
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
}
