/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.Date;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a virtual machine with managed OS Disk
 *  - Start a virtual machine
 *  - Stop a virtual machine
 *  - Restart a virtual machine
 *  - Update a virtual machine
 *    - Tag a virtual machine (there are many possible variations here)
 *    - Attach data disks
 *    - Detach data disks
 *  - List virtual machines
 *  - Delete a virtual machine.
 */
public final class ManageVirtualMachine {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_WEST_CENTRAL;
        final String windowsVMName = Utils.createRandomName("wVM");
        final String linuxVMName = Utils.createRandomName("lVM");
        final String rgName = Utils.createRandomName("rgCOMV");
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";

        try {

            //=============================================================
            // Create a Windows virtual machine

            // Prepare a creatable data disk for VM
            //
            Creatable<Disk> dataDiskCreatable = azure.disks().define(Utils.createRandomName("dsk-"))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(100);

            // Create a data disk to attach to VM
            //
            Disk dataDisk = azure.disks()
                    .define(Utils.createRandomName("dsk-"))
                        .withRegion(region)
                        .withNewResourceGroup(rgName)
                        .withData()
                        .withSizeInGB(50)
                        .create();

            System.out.println("Creating a Windows VM");

            Date t1 = new Date();

            VirtualMachine windowsVM = azure.virtualMachines()
                    .define(windowsVMName)
                        .withRegion(region)
                        .withNewResourceGroup(rgName)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUsername(userName)
                        .withAdminPassword(password)
                        .withNewDataDisk(10)
                        .withNewDataDisk(dataDiskCreatable)
                        .withExistingDataDisk(dataDisk)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .create();

            Date t2 = new Date();
            System.out.println("Created VM: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + windowsVM.id());
            // Print virtual machine details
            Utils.print(windowsVM);


            //=============================================================
            // Update - Tag the virtual machine

            windowsVM.update()
                    .withTag("who-rocks", "java")
                    .withTag("where", "on azure")
                    .apply();

            System.out.println("Tagged VM: " + windowsVM.id());


            //=============================================================
            // Update - Add data disk

                windowsVM.update()
                        .withNewDataDisk(10)
                        .apply();


            System.out.println("Added a data disk to VM" + windowsVM.id());
            Utils.print(windowsVM);


            //=============================================================
            // Update - detach data disk

                windowsVM.update()
                        .withoutDataDisk(0)
                        .apply();

            System.out.println("Detached data disk at lun 0 from VM " + windowsVM.id());


            //=============================================================
            // Restart the virtual machine

            System.out.println("Restarting VM: " + windowsVM.id());

            windowsVM.restart();

            System.out.println("Restarted VM: " + windowsVM.id() + "; state = " + windowsVM.powerState());


            //=============================================================
            // Stop (powerOff) the virtual machine

            System.out.println("Powering OFF VM: " + windowsVM.id());

            windowsVM.powerOff();

            System.out.println("Powered OFF VM: " + windowsVM.id() + "; state = " + windowsVM.powerState());

            // Get the network where Windows VM is hosted
            Network network = windowsVM.getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();


            //=============================================================
            // Create a Linux VM in the same virtual network

            System.out.println("Creating a Linux VM in the network");

            VirtualMachine linuxVM = azure.virtualMachines()
                    .define(linuxVMName)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("subnet1") // Referencing the default subnet name when no name specified at creation
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withRootPassword(password)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .create();

            System.out.println("Created a Linux VM (in the same virtual network): " + linuxVM.id());
            Utils.print(linuxVM);

            //=============================================================
            // List virtual machines in the resource group

            String resourceGroupName = windowsVM.resourceGroupName();

            System.out.println("Printing list of VMs =======");

            for (VirtualMachine virtualMachine : azure.virtualMachines().listByResourceGroup(resourceGroupName)) {
                Utils.print(virtualMachine);
            }

            //=============================================================
            // Delete the virtual machine
            System.out.println("Deleting VM: " + windowsVM.id());

            azure.virtualMachines().deleteById(windowsVM.id());

            System.out.println("Deleted VM: " + windowsVM.id());
            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
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
                    .withLogLevel(LogLevel.BASIC)
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

    private ManageVirtualMachine() {

    }
}