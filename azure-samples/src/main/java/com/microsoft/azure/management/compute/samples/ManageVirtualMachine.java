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
        try {


            //=============================================================
            // Authenticate

            final File credFile = new File("my.azureauth");

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            final String vmName = Utils.createRandomName("vm");
            final String userName = "tirekicker";
            final String password = "12NewPA$$w0rd!";
            final String dataDiskName = "disk2";

            //=============================================================
            // Create a Windows virtual machine

            System.out.println("Creating a Windows VM");

            VirtualMachine vm = azure.virtualMachines().define(vmName)
                    .withRegion(Region.US_EAST)
                    .withNewGroup()
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIpAddressDynamic()
                    .withoutPrimaryPublicIpAddress()
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUserName(userName)
                    .withPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                    .create();

            System.out.println("Created VM: " + vm.id());
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

            System.out.println("Detached data disk " + dataDiskName + "from VM " + vm.id());


            //=============================================================
            // Update - Resize (expand) the data disk
            // First, deallocate teh virtual machine and then proceed with resize
            // TODO must not use two apply () in a sequence, very confusing

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

            Integer osDiskSizeInGb = vm.osDiskSize();
            if (osDiskSizeInGb == null) {
                // Server is not returning the OS Disk size, possible bug in server
                osDiskSizeInGb = 256;
            } else {
                osDiskSizeInGb += 10;
            }

            vm.update()
                    .withOsDiskSizeInGb(osDiskSizeInGb)
                    .apply();

            System.out.println("Expanded VM " + vm.id() + "'s OS disk to" + osDiskSizeInGb + 10);


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
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private ManageVirtualMachine() {

    }
}
