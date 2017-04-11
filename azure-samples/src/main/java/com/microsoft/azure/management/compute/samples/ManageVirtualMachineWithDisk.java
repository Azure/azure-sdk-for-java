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
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a virtual machine with
 *      - Implicit data disks
 *      - Creatable data disks
 *      - Existing data disks
 *  - Update a virtual machine
 *      - Attach data disks
 *      - Detach data disks
 *  - Stop a virtual machine
 *  - Update a virtual machine
 *      - Expand the OS disk
 *      - Expand data disks.
 */
public final class ManageVirtualMachineWithDisk {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String linuxVMName1 = Utils.createRandomName("VM1");
        final String rgName = Utils.createRandomName("rgCOMV");
        final String publicIPDnsLabel = Utils.createRandomName("pip");
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";
        final Region region = Region.US_WEST_CENTRAL;

        try {
            // Creates an empty data disk to attach to the virtual machine
            //
            System.out.println("Creating an empty managed disk");

            Disk dataDisk1 = azure.disks().define(Utils.createRandomName("dsk-"))
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .create();

            System.out.println("Created managed disk");

            // Prepare first creatable data disk
            //
            Creatable<Disk> dataDiskCreatable1 = azure.disks().define(Utils.createRandomName("dsk-"))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(100);

            // Prepare second creatable data disk
            //
            Creatable<Disk> dataDiskCreatable2 = azure.disks().define(Utils.createRandomName("dsk-"))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .withSku(DiskSkuTypes.STANDARD_LRS);

            //======================================================================
            // Create a Linux VM using a PIR image with managed OS and Data disks

            System.out.println("Creating a managed Linux VM");

            VirtualMachine linuxVM = azure.virtualMachines().define(linuxVMName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPDnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)

                    // Begin: Managed data disks
                    .withNewDataDisk(100)
                    .withNewDataDisk(100, 1, CachingTypes.READ_WRITE)
                    .withNewDataDisk(dataDiskCreatable1)
                    .withNewDataDisk(dataDiskCreatable2, 2, CachingTypes.READ_ONLY)
                    .withExistingDataDisk(dataDisk1)

                    // End: Managed data disks
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created a Linux VM with managed OS and data disks: " + linuxVM.id());
            Utils.print(linuxVM);

            //======================================================================
            // Update the virtual machine by detaching two data disks with lun 3 and 4 and adding one

            System.out.println("Updating Linux VM");

            String lun3DiskId = linuxVM.dataDisks().get(3).id();

            linuxVM.update()
                    .withoutDataDisk(3)
                    .withoutDataDisk(4)
                    .withNewDataDisk(200)
                    .apply();

            System.out.println("Updated Linux VM: " + linuxVM.id());
            Utils.print(linuxVM);

            // ======================================================================
            // Delete a managed disk

            Disk disk = azure.disks().getById(lun3DiskId);
            System.out.println("Delete managed disk: " + disk.id());

            azure.disks().deleteByResourceGroup(disk.resourceGroupName(), disk.name());

            System.out.println("Deleted managed disk");

            //======================================================================
            // Deallocate the virtual machine

            System.out.println("De-allocate Linux VM");

            linuxVM.deallocate();

            System.out.println("De-allocated Linux VM");

            //======================================================================
            // Resize the OS and Data Disks

            Disk osDisk = azure.disks().getById(linuxVM.osDiskId());
            List<Disk> dataDisks = new ArrayList<>();
            for (VirtualMachineDataDisk vmDataDisk : linuxVM.dataDisks().values()) {
                Disk dataDisk = azure.disks().getById(vmDataDisk.id());
                dataDisks.add(dataDisk);
            }

            System.out.println("Update OS disk: " + osDisk.id());

            osDisk.update()
                    .withSizeInGB(2 * osDisk.sizeInGB())
                    .apply();

            System.out.println("OS disk updated");

            for (Disk dataDisk : dataDisks) {
                System.out.println("Update data disk: " + dataDisk.id());

                dataDisk.update()
                        .withSizeInGB(dataDisk.sizeInGB() + 10)
                        .apply();

                System.out.println("Data disk updated");
            }

            //======================================================================
            // Starting the virtual machine

            System.out.println("Starting Linux VM");

            linuxVM.start();

            System.out.println("Started Linux VM");
            Utils.print(linuxVM);
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

    private ManageVirtualMachineWithDisk() {
    }
}
