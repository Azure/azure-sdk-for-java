/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create N virtual machines in parallel.
 */
public final class ManageVirtualMachinesInParallel {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final int vmCount = 10;
        final Region region = Region.US_EAST;
        final String rgName = SdkContext.randomResourceName("rgCOPP", 24);
        final String networkName = SdkContext.randomResourceName("vnetCOMV", 24);
        final String storageAccountName = SdkContext.randomResourceName("stgCOMV", 20);
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";
        try {
            // Create a resource group [Where all resources gets created]
            ResourceGroup resourceGroup = azure.resourceGroups()
                    .define(rgName)
                    .withRegion(region)
                    .create();

            // Prepare Creatable Network definition [Where all the virtual machines get added to]
            Creatable<Network> creatableNetwork = azure.networks().define(networkName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("172.16.0.0/16");

            // Prepare Creatable Storage account definition [For storing VMs disk]
            Creatable<StorageAccount> creatableStorageAccount = azure.storageAccounts().define(storageAccountName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);

            // Prepare a batch of Creatable Virtual Machines definitions
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

            for (int i = 0; i < vmCount; i++) {
                Creatable<VirtualMachine> creatableVirtualMachine = azure.virtualMachines().define("VM-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withNewPrimaryNetwork(creatableNetwork)
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withRootPassword(password)
                        .withSize(VirtualMachineSizeTypes.STANDARD_DS3_V2)
                        .withNewStorageAccount(creatableStorageAccount);
                creatableVirtualMachines.add(creatableVirtualMachine);
            }

            StopWatch stopwatch = new StopWatch();
            System.out.println("Creating the virtual machines");
            stopwatch.start();

            Collection<VirtualMachine> virtualMachines = azure.virtualMachines().create(creatableVirtualMachines).values();

            stopwatch.stop();
            System.out.println("Created virtual machines");

            for (VirtualMachine virtualMachine : virtualMachines) {
                System.out.println(virtualMachine.id());
            }

            System.out.println("Virtual Machines create: (took " + (stopwatch.getTime() / 1000) + " seconds) ");
            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
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
                    .withLogLevel(LogLevel.BASIC)
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

    private ManageVirtualMachinesInParallel() {
    }
}