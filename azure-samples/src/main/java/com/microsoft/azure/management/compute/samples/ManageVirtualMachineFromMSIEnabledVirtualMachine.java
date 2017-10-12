/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.MSICredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;

/**
 * Azure Compute sample for managing virtual machine from Managed Service Identity (MSI) enabled virtual machine -
 *   - Create a virtual machine using MSI credentials from MSI enabled VM
 *   - Delete the virtual machine using MSI credentials from MSI enabled VM.
 */
public final class ManageVirtualMachineFromMSIEnabledVirtualMachine {
    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            final String rgName = "msi-rg-test";
            final Region region = Region.US_WEST_CENTRAL;

// This sample required to be run from a MSI enabled virtual machine with role
// based contributor access to the resource group with name "msi-rg-test". MSI
// enabled VM can be created using service principal credentials as shown below.
//
//            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
//
//            Azure azure = Azure.configure()
//                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
//                    .authenticate(credFile)
//                    .withDefaultSubscription(subscriptionId);

//            VirtualMachine virtualMachine = azure.virtualMachines()
//                    .define("<vm-name>")
//                    .withRegion(region)
//                    .withNewResourceGroup(rgName)
//                    .withNewPrimaryNetwork("10.0.0.0/28")
//                    .withPrimaryPrivateIPAddressDynamic()
//                    .withNewPrimaryPublicIPAddress(pipName)
//                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
//                    .withRootUsername("<user-name>")
//                    .withRootPassword("<password>")
//                    .withManagedServiceIdentity()
//                    .withRoleBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
//                    .create();


            // Specify your subscription Id
            final String subscriptionId = "<subscription-id>";
            final String linuxVMName = Utils.createRandomName("VM1");
            final String userName = "tirekicker";
            final String password = "12NewPA$$w0rd!";

            //=============================================================
            // MSI Authenticate

            final MSICredentials credentials = new MSICredentials(AzureEnvironment.AZURE);
            Azure azure = Azure.configure()
                    .authenticate(credentials)
                    .withSubscription(subscriptionId);

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            //=============================================================
            // Create a Linux VM using MSI credentials

            System.out.println("Creating a Linux VM using MSI credentials");

            VirtualMachine virtualMachine = azure.virtualMachines()
                    .define(linuxVMName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                    .withOSDiskCaching(CachingTypes.READ_WRITE)
                    .withManagedServiceIdentity()
                    .withRoleBasedAccessToCurrentResourceGroup(BuiltInRole.CONTRIBUTOR)
                    .create();

            System.out.println("Created virtual machine using MSI credentials");
            Utils.print(virtualMachine);

            //=============================================================
            // Delete the VM using MSI credentials

            System.out.println("Deleting the virtual machine using MSI credentials");

            azure.virtualMachines().deleteById(virtualMachine.id());

            System.out.println("Deleted virtual machine");

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageVirtualMachineFromMSIEnabledVirtualMachine() {
    }
}
