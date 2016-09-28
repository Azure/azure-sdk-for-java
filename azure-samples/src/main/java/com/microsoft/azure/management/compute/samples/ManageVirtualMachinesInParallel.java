/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.storage.StorageAccount;
import okhttp3.logging.HttpLoggingInterceptor;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create N virtual machines in parallel.
 */
public final class ManageVirtualMachinesInParallel {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final int vmCount = 2;
        final String rgName = ResourceNamer.randomResourceName("rgCOMV", 24);
        final String networkName = ResourceNamer.randomResourceName("vnetCOMV", 24);
        final String storageAccountName = ResourceNamer.randomResourceName("stgCOMV", 20);
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
                // Create a resource group [Where all resources gets created]
                ResourceGroup resourceGroup = azure.resourceGroups()
                        .define(rgName)
                        .withRegion(Region.US_EAST)
                        .create();

                // Prepare Creatable Network definition [Where all the virtual machines get added to]
                Network.DefinitionStages.WithCreate creatableNetwork = azure.networks()
                        .define(networkName)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup)
                        .withAddressSpace("172.16.0.0/16");

                // Prepare Creatable Storage account definition [For storing VMs disk]
                StorageAccount.DefinitionStages.WithCreate creatableStorageAccount = azure.storageAccounts()
                        .define(storageAccountName)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup);

                // Prepare a batch of Creatable Virtual Machines definitions
                List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

                for (int i = 0; i < vmCount; i++) {
                    VirtualMachine.DefinitionStages.WithCreate creatableVirtualMachine = azure.virtualMachines()
                            .define("VM-" + i)
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(resourceGroup)
                            .withNewPrimaryNetwork(creatableNetwork)
                            .withPrimaryPrivateIpAddressDynamic()
                            .withoutPrimaryPublicIpAddress()
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUserName(userName)
                            .withPassword(password)
                            .withSize(VirtualMachineSizeTypes.STANDARD_DS3_V2)
                            .withNewStorageAccount(creatableStorageAccount);
                    creatableVirtualMachines.add(creatableVirtualMachine);
                }

                Date t1 = new Date();
                System.out.println("Creating the virtual machines");

                CreatedResources<VirtualMachine> virtualMachines = azure.virtualMachines().create(creatableVirtualMachines);

                Date t2 = new Date();
                System.out.println("Created virtual machines");

                for (VirtualMachine virtualMachine : virtualMachines) {
                    System.out.println(virtualMachine.id());
                }

                System.out.println("Virtual Machines create: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) ");
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

    private ManageVirtualMachinesInParallel() {
    }
}