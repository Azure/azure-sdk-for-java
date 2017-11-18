/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPSkuType;
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a zonal virtual machine with implicitly zoned related resources (PublicIP, Disk)
 *  - Creates a zonal PublicIP address
 *  - Creates a zonal managed data disk
 *  - Create a zonal virtual machine and associate explicitly created zonal PublicIP and data disk.
 */
public final class ManageZonalVirtualMachine {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_EAST2;
        final String rgName = Utils.createRandomName("rgCOMV");
        final String vmName1 = Utils.createRandomName("lVM1");
        final String vmName2 = Utils.createRandomName("lVM2");
        final String pipName1 = Utils.createRandomName("pip1");
        final String pipName2 = Utils.createRandomName("pip2");
        final String diskName = Utils.createRandomName("ds");
        final String userName = "tirekicker";
        final String password = "12NewPA23w0rd!";

        try {

            //=============================================================
            // Create a zonal virtual machine with implicitly zoned related resources (PublicIP, Disk)

            System.out.println("Creating a zonal VM with implicitly zoned related resources (PublicIP, Disk)");

            VirtualMachine virtualMachine1 = azure.virtualMachines()
                    .define(vmName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(pipName1)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    // Optional
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    // Create VM
                    .create();

            System.out.println("Created a zoned virtual machine: " + virtualMachine1.id());
            Utils.print(virtualMachine1);

            //=============================================================
            // Create a zonal PublicIP address

            System.out.println("Creating a zonal public ip address");

            PublicIPAddress publicIPAddress = azure.publicIPAddresses()
                    .define(pipName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    // Optional
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .withStaticIP()
                    .withSku(PublicIPSkuType.STANDARD)
                    // Create PIP
                    .create();

            System.out.println("Created a zoned public ip address: " + publicIPAddress.id());
            Utils.print(publicIPAddress);

            //=============================================================
            // Create a zonal managed data disk

            System.out.println("Creating a zonal data disk");

            Disk dataDisk = azure.disks()
                    .define(diskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(100)
                    // Optional
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    // Create Disk
                    .create();

            System.out.println("Created a zoned managed data disk: " + dataDisk.id());

            //=============================================================
            // Create a zonal virtual machine with zonal public ip and data disk


            System.out.println("Creating a zonal VM with implicitly zoned related resources (PublicIP, Disk)");

            VirtualMachine virtualMachine2 = azure.virtualMachines()
                    .define(vmName2)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingPrimaryPublicIPAddress(publicIPAddress)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    // Optional
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .withExistingDataDisk(dataDisk)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    // Create VM
                    .create();

            System.out.println("Created a zoned virtual machine: " + virtualMachine2.id());
            Utils.print(virtualMachine2);

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
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
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

    private ManageZonalVirtualMachine() {

    }
}
