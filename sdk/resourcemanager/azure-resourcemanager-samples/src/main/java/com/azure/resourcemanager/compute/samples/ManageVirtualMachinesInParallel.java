// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.apache.commons.lang.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create N virtual machines in parallel.
 */
public final class ManageVirtualMachinesInParallel {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final int vmCount = 10;
        final Region region = Region.US_SOUTH_CENTRAL;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOPP", 24);
        final String networkName = Utils.randomResourceName(azureResourceManager, "vnetCOMV", 24);
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "stgCOMV", 20);
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();
        try {
            // Create a resource group [Where all resources gets created]
            ResourceGroup resourceGroup = azureResourceManager.resourceGroups()
                    .define(rgName)
                    .withRegion(region)
                    .create();

            // Prepare Creatable Network definition [Where all the virtual machines get added to]
            Creatable<Network> creatableNetwork = azureResourceManager.networks().define(networkName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("172.16.0.0/16");

            // Prepare Creatable Storage account definition [For storing VMs disk]
            Creatable<StorageAccount> creatableStorageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);

            // Prepare a batch of Creatable Virtual Machines definitions
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

            for (int i = 0; i < vmCount; i++) {
                Creatable<VirtualMachine> creatableVirtualMachine = azureResourceManager.virtualMachines().define("VM-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withNewPrimaryNetwork(creatableNetwork)
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withSsh(sshPublicKey)
                        .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                        .withNewStorageAccount(creatableStorageAccount);
                creatableVirtualMachines.add(creatableVirtualMachine);
            }

            StopWatch stopwatch = new StopWatch();
            System.out.println("Creating the virtual machines");
            stopwatch.start();

            Collection<VirtualMachine> virtualMachines = azureResourceManager.virtualMachines().create(creatableVirtualMachines).values();

            stopwatch.stop();
            System.out.println("Created virtual machines");

            for (VirtualMachine virtualMachine : virtualMachines) {
                System.out.println(virtualMachine.id());
            }

            System.out.println("Virtual Machines create: (took " + (stopwatch.getTime() / 1000) + " seconds) ");
            return true;
        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
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

    private ManageVirtualMachinesInParallel() {
    }
}
