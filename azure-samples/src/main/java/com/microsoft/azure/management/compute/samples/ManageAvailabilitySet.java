 /**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;

/**
 * Azure Compute sample for managing availability sets -
 *  - Create an availability set
 *  - Create a VM in a new availability set
 *  - Create another VM in the same availability set
 *  - Update the availability set
 *  - Create another availability set
 *  - List availability sets
 *  - Delete an availability set.
 */

public final class ManageAvailabilitySet {

    /**
     * Main entry point.
     * @param args parameters
     */
    public static void main(String[] args) {

        final String rgName = Utils.createRandomName("rgCOMA");
        final String availSetName1 = Utils.createRandomName("av1");
        final String availSetName2 = Utils.createRandomName("av2");
        final String vm1Name = Utils.createRandomName("vm1");
        final String vm2Name = Utils.createRandomName("vm2");
        final String vnetName = Utils.createRandomName("vnet");

        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            try {

                //=============================================================
                // Create an availability set

                System.out.println("Creating an availability set");

                AvailabilitySet availSet1 = azure.availabilitySets().define(availSetName1)
                        .withRegion(Region.US_EAST)
                        .withNewResourceGroup(rgName)
                        .withFaultDomainCount(2)
                        .withUpdateDomainCount(4)
                        .withTag("cluster", "Windowslinux")
                        .withTag("tag1", "tag1val")
                        .create();

                System.out.println("Created first availability set: " + availSet1.id());
                Utils.print(availSet1);

                //=============================================================
                // Define a virtual network for the VMs in this availability set

                Network.DefinitionStages.WithCreate network = azure.networks()
                        .define(vnetName)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .withAddressSpace("10.0.0.0/28");


                //=============================================================
                // Create a Windows VM in the new availability set

                System.out.println("Creating a Windows VM in the availability set");

                VirtualMachine vm1 = azure.virtualMachines().define(vm1Name)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .withNewPrimaryNetwork(network)
                        .withPrimaryPrivateIpAddressDynamic()
                        .withoutPrimaryPublicIpAddress()
                        .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                        .withAdminUserName(userName)
                        .withPassword(password)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .withExistingAvailabilitySet(availSet1)
                        .create();


                System.out.println("Created first VM:" + vm1.id());
                Utils.print(vm1);


                //=============================================================
                // Create a Linux VM in the same availability set

                System.out.println("Creating a Linux VM in the availability set");

                VirtualMachine vm2 = azure.virtualMachines().define(vm2Name)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .withNewPrimaryNetwork(network)
                        .withPrimaryPrivateIpAddressDynamic()
                        .withoutPrimaryPublicIpAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUserName(userName)
                        .withPassword(password)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .withExistingAvailabilitySet(availSet1)
                        .create();

                System.out.println("Created second VM: " + vm2.id());
                Utils.print(vm2);


                //=============================================================
                // Update - Tag the availability set

                availSet1 =  availSet1.update()
                        .withTag("server1", "nginx")
                        .withTag("server2", "iis")
                        .withoutTag("tag1")
                        .apply();

                System.out.println("Tagged availability set: " + availSet1.id());


                //=============================================================
                // Create another availability set

                System.out.println("Creating an availability set");

                AvailabilitySet availSet2 = azure.availabilitySets().define(availSetName2)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .create();

                System.out.println("Created second availability set: " + availSet2.id());
                Utils.print(availSet2);


                //=============================================================
                // List availability sets

                String resourceGroupName = availSet1.resourceGroupName();

                System.out.println("Printing list of availability sets  =======");

                for (AvailabilitySet availabilitySet : azure.availabilitySets().listByGroup(resourceGroupName)) {
                    Utils.print(availabilitySet);
                }


                //=============================================================
                // Delete an availability set

                System.out.println("Deleting an availability set: " + availSet2.id());

                azure.availabilitySets().delete(availSet2.id());

                System.out.println("Deleted availability set: " + availSet2.id());

            } catch (Exception f) {

                System.out.println(f.getMessage());
                f.printStackTrace();

            } finally {

                try {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().delete(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } catch (NullPointerException npe) {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                } catch (Exception g) {
                    g.printStackTrace();
                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private ManageAvailabilitySet() {
    }
}