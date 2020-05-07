// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.management.Azure;
import com.azure.management.compute.Disk;
import com.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.PublicIPSkuType;
import com.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.samples.Utils;

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
        final String rgName = azure.sdkContext().randomResourceName("rgCOMV", 15);
        final String vmName1 = azure.sdkContext().randomResourceName("lVM1", 15);
        final String vmName2 = azure.sdkContext().randomResourceName("lVM2", 15);
        final String pipName1 = azure.sdkContext().randomResourceName("pip1", 15);
        final String pipName2 = azure.sdkContext().randomResourceName("pip2", 15);
        final String diskName = azure.sdkContext().randomResourceName("ds", 15);
        final String userName = "tirekicker";
        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
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

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE, true);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.environment().getActiveDirectoryEndpoint())
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
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
