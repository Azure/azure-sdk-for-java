// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

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
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMA", 15);
        final String availSetName1 = Utils.randomResourceName(azureResourceManager, "av1", 15);
        final String availSetName2 = Utils.randomResourceName(azureResourceManager, "av2", 15);
        final String vm1Name = Utils.randomResourceName(azureResourceManager, "vm1", 15);
        final String vm2Name = Utils.randomResourceName(azureResourceManager, "vm2", 15);
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnet", 15);

        final String userName = "tirekicker";
        final String password = Utils.password();
        final String sshPublicKey = Utils.sshPublicKey();

        try {

            //=============================================================
            // Create an availability set

            System.out.println("Creating an availability set");

            AvailabilitySet availSet1 = azureResourceManager.availabilitySets().define(availSetName1)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withFaultDomainCount(2)
                    .withUpdateDomainCount(4)
                    .withSku(AvailabilitySetSkuTypes.ALIGNED)
                    .withTag("cluster", "Windowslinux")
                    .withTag("tag1", "tag1val")
                    .create();

            System.out.println("Created first availability set: " + availSet1.id());
            Utils.print(availSet1);

            //=============================================================
            // Define a virtual network for the VMs in this availability set

            Creatable<Network> networkDefinition = azureResourceManager.networks().define(vnetName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withAddressSpace("10.0.0.0/28");


            //=============================================================
            // Create a Windows VM in the new availability set

            System.out.println("Creating a Windows VM in the availability set");

            VirtualMachine vm1 = azureResourceManager.virtualMachines().define(vm1Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork(networkDefinition)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                    .withAdminUsername(userName)
                    .withAdminPassword(password)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .withExistingAvailabilitySet(availSet1)
                    .create();


            System.out.println("Created first VM:" + vm1.id());
            Utils.print(vm1);


            //=============================================================
            // Create a Linux VM in the same availability set

            System.out.println("Creating a Linux VM in the availability set");

            VirtualMachine vm2 = azureResourceManager.virtualMachines().define(vm2Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork(networkDefinition)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshPublicKey)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .withExistingAvailabilitySet(availSet1)
                    .create();

            System.out.println("Created second VM: " + vm2.id());
            Utils.print(vm2);


            //=============================================================
            // Update - Tag the availability set

            availSet1 = availSet1.update()
                    .withTag("server1", "nginx")
                    .withTag("server2", "iis")
                    .withoutTag("tag1")
                    .apply();

            System.out.println("Tagged availability set: " + availSet1.id());


            //=============================================================
            // Create another availability set

            System.out.println("Creating an availability set");

            AvailabilitySet availSet2 = azureResourceManager.availabilitySets().define(availSetName2)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .create();

            System.out.println("Created second availability set: " + availSet2.id());
            Utils.print(availSet2);


            //=============================================================
            // List availability sets

            String resourceGroupName = availSet1.resourceGroupName();

            System.out.println("Printing list of availability sets  =======");

            for (AvailabilitySet availabilitySet : azureResourceManager.availabilitySets().listByResourceGroup(resourceGroupName)) {
                Utils.print(availabilitySet);
            }


            //=============================================================
            // Delete an availability set

            System.out.println("Deleting an availability set: " + availSet2.id());

            azureResourceManager.availabilitySets().deleteById(availSet2.id());

            System.out.println("Deleted availability set: " + availSet2.id());
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
     * @param args parameters
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

    private ManageAvailabilitySet() {
    }
}
