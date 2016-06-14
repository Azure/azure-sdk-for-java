/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
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
 *  - Delete a virtual machine
 */

public class ManageVirtualMachine {

    public static void main (String [] args) {

        try {

            final File credFile = new File("my.azureauth");

            Azure azure = Azure.authenticate(credFile).withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            final String vmName = Utils.createRandomName("vm");
            final String userName = "tirekicker";
            final String password = "12NewPA$$w0rd!";

            // Create a Windows virtual machine
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
            Utils.print(vm);

            // Start a virtual machine


            // Stop a virtual machine


            // Restart a virtual machine (start first, restart next)


            // List virtual machines


            // Update a virtual machine
            // Update - Expand the OS drive


            // Update - Tag a virtual machine

            // Update - Attach data disks


            // Update - detach data disks


            // List virtual machines


            // Delete a virtual machine

        } catch (Exception e)
        {
            System.err.println(e.getMessage());
        }

    }

}
