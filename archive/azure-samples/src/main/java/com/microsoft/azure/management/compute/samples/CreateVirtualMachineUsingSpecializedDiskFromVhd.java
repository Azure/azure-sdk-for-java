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
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachineUnmanagedDataDisk;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
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
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String linuxVMName1 = Utils.createRandomName("VM1");
        final String linuxVMName2 = Utils.createRandomName("VM2");
        final String managedOSDiskName = Utils.createRandomName("ds-os-");
        final String managedDataDiskNamePrefix = Utils.createRandomName("ds-data-");
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
            // Create a Linux VM using an image from PIR (Platform Image Repository)

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
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
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

            azure.virtualMachines().deleteById(linuxVM.id());

            System.out.println("Deleted the VM");

            //=============================================================
            // Create Managed disk from the specialized OS VHD

            System.out.println(String.format("Creating managed disk from the specialized OS VHD: %s ", specializedOSVhdUri));

            Disk osDisk = azure.disks().define(managedOSDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromVhd(specializedOSVhdUri)
                    .withSizeInGB(100)
                    .create();

            System.out.println("Created managed disk holding OS: " + osDisk.id());
            // Utils.print(osDisk); TODO

            //=============================================================
            // Create Managed disks from the Data VHDs

            List<Disk> dataDisks = new ArrayList<>();
            int i = 0;
            for (String dataVhdUri : dataVhdUris) {
                System.out.println(String.format("Creating managed disk from the Data VHD: %s ", dataVhdUri));

                Disk dataDisk = azure.disks().define(managedDataDiskNamePrefix + "-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withData()
                        .fromVhd(dataVhdUri)
                        .withSizeInGB(150)
                        .withSku(DiskSkuTypes.STANDARD_LRS)
                        .create();
                dataDisks.add(dataDisk);

                System.out.println("Created managed disk holding data: " + dataDisk.id());
                // Utils.print(dataDisk); TODO
                i++;
            }

            //=============================================================
            // Create a Linux VM by attaching the disks

            System.out.println("Creating a Linux VM using specialized OS and data disks");

            VirtualMachine linuxVM2 = azure.virtualMachines().define(linuxVMName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSDisk(osDisk, OperatingSystemTypes.LINUX)
                    .withExistingDataDisk(dataDisks.get(0))
                    .withExistingDataDisk(dataDisks.get(1), 1, CachingTypes.READ_WRITE)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
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
                Disk dataDisk = azure.disks().getById(diskId);
                String dataDiskSasUri = dataDisk.grantAccess(24 * 60);
                System.out.println(String.format("Data disk SAS Uri: %s", dataDiskSasUri));
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

            Azure azure = Azure
                    .configure()
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
}
