/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.CachingTypes;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.Date;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a virtual machine
 *  - Start a virtual machine
 *  - Stop a virtual machine
 *  - Restart a virtual machine
 *  - Update a virtual machine
 *    - Expand the OS drive
 *    - Tag a virtual machine (there are many possible variations here)
 *    - Attach data disks
 *    - Detach data disks
 *  - List virtual machines
 *  - Delete a virtual machine.
 */
public final class ManageVirtualMachine {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String vmName = Utils.createRandomName("vm");
        final String rgName = Utils.createRandomName("rgCOMV");
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";
        final String dataDiskName = "disk2";

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File("my.azureauth");

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());


            try {

                //=============================================================
                // Create a Windows virtual machine

                System.out.println("Creating a Windows VM");

                Date t1 = new Date();

                VirtualMachine vm = azure.virtualMachines().define(vmName)
                        .withRegion(Region.US_EAST)
                        .withNewGroup(rgName)
                        .withNewPrimaryNetwork("10.0.0.0/28")
                        .withPrimaryPrivateIpAddressDynamic()
                        .withoutPrimaryPublicIpAddress()
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUserName(userName)
                        .withPassword(password)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .create();

                Date t2 = new Date();
                System.out.println("Created VM: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) " + vm.id());
                // Print virtual machine details
                Utils.print(vm);


                //=============================================================
                // Update - Tag the virtual machine

                vm.update()
                        .withTag("who-rocks", "java")
                        .withTag("where", "on azure")
                        .apply();

                System.out.println("Tagged VM: " + vm.id());


                //=============================================================
                // Update - Attach data disks

                vm.update()
                        .withNewDataDisk(10)
                        .defineNewDataDisk(dataDiskName)
                            .withSizeInGB(20)
                            .withCaching(CachingTypes.READ_WRITE)
                        .attach()
                        .apply();

                System.out.println("Attached a new data disk" + dataDiskName + " to VM" + vm.id());
                Utils.print(vm);


                //=============================================================
                // Update - detach data disk

                vm.update()
                        .withoutDataDisk(dataDiskName)
                        .apply();

                System.out.println("Detached data disk " + dataDiskName + " from VM " + vm.id());


                //=============================================================
                // Update - Resize (expand) the data disk
                // First, deallocate teh virtual machine and then proceed with resize

                System.out.println("De-allocating VM: " + vm.id());

                vm.deallocate();

                System.out.println("De-allocated VM: " + vm.id());

                DataDisk dataDisk = vm.dataDisks().get(0);

                vm.update()
                        .updateDataDisk(dataDisk.name())
                            .withSizeInGB(30)
                        .set()
                        .apply();

                System.out.println("Expanded VM " + vm.id() + "'s data disk to 30GB");


                //=============================================================
                // Update - Expand the OS drive size by 10 GB

                int osDiskSizeInGb = vm.osDiskSize();
                if (osDiskSizeInGb == 0) {
                    // Server is not returning the OS Disk size, possible bug in server
                    System.out.println("Server is not returning the OS disk size, possible bug in the server?");
                    System.out.println("Assuming that the OS disk size is 256 GB");
                    osDiskSizeInGb = 256;
                }

                vm.update()
                        .withOsDiskSizeInGb(osDiskSizeInGb + 10)
                        .apply();

                System.out.println("Expanded VM " + vm.id() + "'s OS disk to " + (osDiskSizeInGb + 10));


                //=============================================================
                // Start the virtual machine

                System.out.println("Starting VM " + vm.id());

                vm.start();

                System.out.println("Started VM: " + vm.id() + "; state = " + vm.powerState());


                //=============================================================
                // Restart the virtual machine

                System.out.println("Restarting VM: " + vm.id());

                vm.restart();

                System.out.println("Restarted VM: " + vm.id() + "; state = " + vm.powerState());


                //=============================================================
                // Stop (powerOff) the virtual machine

                System.out.println("Powering OFF VM: " + vm.id());

                vm.powerOff();

                System.out.println("Powered OFF VM: " + vm.id() + "; state = " + vm.powerState());


                //=============================================================
                // TODO Create a Linux VM


                //=============================================================
                // List virtual machines in the resource group

                String resourceGroupName = vm.resourceGroupName();

                System.out.println("Printing list of VMs =======");

                for (VirtualMachine virtualMachine : azure.virtualMachines().listByGroup(resourceGroupName)) {
                    Utils.print(virtualMachine);
                }


                //=============================================================
                // Delete the virtual machine
                System.out.println("Deleting VM: " + vm.id());

                azure.virtualMachines().delete(vm.id());

                System.out.println("Deleted VM: " + vm.id());

            } catch (Exception f) {
                System.out.println(f.getMessage());
                f.printStackTrace();
            } finally {
                if (azure.resourceGroups().getByName(rgName) != null) {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().delete(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } else {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageVirtualMachine() {

    }
}
