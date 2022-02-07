// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

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
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_EAST2;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String vmName1 = Utils.randomResourceName(azureResourceManager, "lVM1", 15);
        final String vmName2 = Utils.randomResourceName(azureResourceManager, "lVM2", 15);
        final String pipName1 = Utils.randomResourceName(azureResourceManager, "pip1", 15);
        final String pipName2 = Utils.randomResourceName(azureResourceManager, "pip2", 15);
        final String diskName = Utils.randomResourceName(azureResourceManager, "ds", 15);
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();

        try {

            //=============================================================
            // Create a zonal virtual machine with implicitly zoned related resources (PublicIP, Disk)

            System.out.println("Creating a zonal VM with implicitly zoned related resources (PublicIP, Disk)");

            VirtualMachine virtualMachine1 = azureResourceManager.virtualMachines()
                    .define(vmName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(pipName1)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshPublicKey)
                    // Optional
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    // Create VM
                    .create();

            System.out.println("Created a zoned virtual machine: " + virtualMachine1.id());
            Utils.print(virtualMachine1);

            //=============================================================
            // Create a zonal PublicIP address

            System.out.println("Creating a zonal public ip address");

            PublicIpAddress publicIPAddress = azureResourceManager.publicIpAddresses()
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

            Disk dataDisk = azureResourceManager.disks()
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

            VirtualMachine virtualMachine2 = azureResourceManager.virtualMachines()
                    .define(vmName2)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingPrimaryPublicIPAddress(publicIPAddress)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshPublicKey)
                    // Optional
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .withExistingDataDisk(dataDisk)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    // Create VM
                    .create();

            System.out.println("Created a zoned virtual machine: " + virtualMachine2.id());
            Utils.print(virtualMachine2);

            return true;
        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
    }

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageZonalVirtualMachine() {

    }
}
