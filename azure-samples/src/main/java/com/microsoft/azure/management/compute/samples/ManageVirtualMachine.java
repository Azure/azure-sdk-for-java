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
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
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
            // Authenticate
            //
            final File credFile = new File("my.azureauth");
            Azure azure = Azure.authenticate(credFile).withDefaultSubscription();

            // Print selected subscription
            //
            System.out.println("Selected subscription: " + azure.subscriptionId());

            final String vmName = Utils.createRandomName("vm");
            final String userName = "tirekicker";
            final String password = "12NewPA$$w0rd!";
            final String dataDiskName = "disk2";

            // Create a Windows virtual machine
            //
            VirtualMachine vm = azure.virtualMachines().define(vmName)
                    .withRegion(Region.US_EAST)
                    .withNewGroup()
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIpAddressDynamic()
                    .withoutPrimaryPublicIpAddress()
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUserName(userName)
                    .withPassword(password)
                    .create();

            // Print virtual machine details
            //
            Utils.print(vm);

            // Update - Tag the virtual machine
            //
            vm.update()
                    .withTag("who-rocks", "java")
                    .withTag("where", "in azure")
                    .apply();

            // Update - Attach data disks
            //
            vm.update()
                    .withNewDataDisk(10)
                    .defineNewDataDisk(dataDiskName)
                    .withSizeInGB(20)
                    .withCaching(CachingTypes.READ_WRITE)
                    .attach()
                    .apply();

            // Update - detach data disk
            //
            vm.update()
                    .withoutDataDisk(dataDiskName)
                    .apply();

            // Deallocate the virtual machine
            //
            vm.deallocate();

            // Update - Resize (expand) the data disk
            //
            DataDisk dataDisk = vm.dataDisks().get(0);
            vm.update()
                    .updateDataDisk(dataDisk.name())
                        .withSizeInGB(30)
                    .set()
                    .apply();


            // Update - Expand the OS drive size by 10 GB
            //
            int osDiskSizeInGb = vm.osDiskSize();
            vm.update()
                    .withOsDiskSizeInGb(osDiskSizeInGb + 10)
                    .apply();

            // Start the virtual machine
            //
            vm.start();

            // Restart the virtual machine
            //
            vm.restart();

            // Stop (powerOff) the virtual machine
            //
            vm.powerOff();

            // List virtual machines in the resource group
            //
            String resourceGroupName = vm.resourceGroupName();
            for (VirtualMachine virtualMachine : azure.virtualMachines().listByGroup(resourceGroupName)) {
                Utils.print(virtualMachine);
            }

            // Delete the virtual machine
            //
            azure.virtualMachines().delete(vm.id());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private ManageVirtualMachine() {

    }
}
