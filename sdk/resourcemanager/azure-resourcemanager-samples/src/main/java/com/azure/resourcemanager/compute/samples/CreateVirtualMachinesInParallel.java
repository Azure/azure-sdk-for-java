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
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.apache.commons.lang.time.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Azure compute sample for creating multiple virtual machines in parallel.
 */
public final class CreateVirtualMachinesInParallel {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOPD", 24);
        final String userName = "tirekicker";
        final String sshKey = Utils.sshPublicKey();

        Map<Region, Integer> virtualMachinesByLocation = new HashMap<Region, Integer>();

        // debug target
        /**
         virtualMachinesByLocation.put(Region.US_EAST, 5);
         virtualMachinesByLocation.put(Region.US_SOUTH_CENTRAL, 5);
         */

        // final demo target
        virtualMachinesByLocation.put(Region.US_EAST, 4);
        virtualMachinesByLocation.put(Region.US_EAST2, 4);
        virtualMachinesByLocation.put(Region.US_WEST, 4);
        virtualMachinesByLocation.put(Region.US_WEST2, 4);
        // virtualMachinesByLocation.put(Region.BRAZIL_SOUTH, 5);
        // virtualMachinesByLocation.put(Region.EUROPE_NORTH, 5);
        // virtualMachinesByLocation.put(Region.EUROPE_WEST, 5);
        // virtualMachinesByLocation.put(Region.UK_WEST, 5);
        // virtualMachinesByLocation.put(Region.ASIA_SOUTHEAST, 5);
        // virtualMachinesByLocation.put(Region.INDIA_SOUTH, 5);
        // virtualMachinesByLocation.put(Region.JAPAN_EAST, 5);
        // virtualMachinesByLocation.put(Region.JAPAN_WEST, 5);

        try {

            //=============================================================
            // Create a resource group (Where all resources gets created)
            //
            ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define(rgName)
                    .withRegion(Region.US_EAST)
                    .create();

            System.out.println("Created a new resource group - " + resourceGroup.id());

//            List<String> publicIpCreatableKeys = new ArrayList<>();
            // Prepare a batch of Creatable definitions
            //
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

            for (Map.Entry<Region, Integer> entry : virtualMachinesByLocation.entrySet()) {
                Region region = entry.getKey();
                Integer vmCount = entry.getValue();

                //=============================================================
                // Create 1 network creatable per region
                // Prepare Creatable Network definition (Where all the virtual machines get added to)
                //
                String networkName = Utils.randomResourceName(azureResourceManager, "vnetCOPD-", 20);
                Creatable<Network> networkCreatable = azureResourceManager.networks().define(networkName)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withAddressSpace("172.16.0.0/16");

                //=============================================================
                // Create 1 storage creatable per region (For storing VMs disk)
                //
                String storageAccountName = Utils.randomResourceName(azureResourceManager, "stgcopd", 20);
                Creatable<StorageAccount> storageAccountCreatable = azureResourceManager.storageAccounts().define(storageAccountName)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup);

                String linuxVMNamePrefix = Utils.randomResourceName(azureResourceManager, "vm-", 15);
                for (int i = 1; i <= vmCount; i++) {

                    //=============================================================
                    // Create 1 public IP address creatable
                    //
                    Creatable<PublicIpAddress> publicIPAddressCreatable = azureResourceManager.publicIpAddresses()
                            .define(String.format("%s-%d", linuxVMNamePrefix, i))
                                .withRegion(region)
                                .withExistingResourceGroup(resourceGroup)
                                .withLeafDomainLabel(Utils.randomResourceName(azureResourceManager, "pip", 10));

//                    publicIpCreatableKeys.add(publicIPAddressCreatable.key());

                    //=============================================================
                    // Create 1 virtual machine creatable
                    Creatable<VirtualMachine> virtualMachineCreatable = azureResourceManager.virtualMachines()
                            .define(String.format("%s-%d", linuxVMNamePrefix, i))
                                .withRegion(region)
                                .withExistingResourceGroup(resourceGroup)
                                .withNewPrimaryNetwork(networkCreatable)
                                .withPrimaryPrivateIPAddressDynamic()
                                .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
                                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                                .withRootUsername(userName)
                                .withSsh(sshKey)
                                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                                .withNewStorageAccount(storageAccountCreatable);
                    creatableVirtualMachines.add(virtualMachineCreatable);
                }
            }


            //=============================================================
            // Create !!

            StopWatch stopwatch = new StopWatch();
            System.out.println("Creating the virtual machines");
            stopwatch.start();

            CreatedResources<VirtualMachine> virtualMachines = azureResourceManager.virtualMachines().create(creatableVirtualMachines);

            stopwatch.stop();
            System.out.println("Created virtual machines");

            for (VirtualMachine virtualMachine : virtualMachines.values()) {
                System.out.println(virtualMachine.id());
            }

            System.out.println("Virtual Machines created: (took " + (stopwatch.getTime() / 1000) + " seconds to create) == " + virtualMachines.size() + " == virtual machines");

//            List<String> publicIpResourceIds = new ArrayList<>();
//            for (String publicIpCreatableKey : publicIpCreatableKeys) {
//                PublicIPAddress pip = (PublicIPAddress) virtualMachines.createdRelatedResource(publicIpCreatableKey);
//                publicIpResourceIds.add(pip.id());
//            }
//
//            //=============================================================
//            // Create 1 Traffic Manager Profile
//            //
//            String trafficManagerName = azure.internalContext().randomResourceName("tra", 15);
//            TrafficManagerProfile.DefinitionStages.WithEndpoint profileWithEndpoint = azure.trafficManagerProfiles().define(trafficManagerName)
//                    .withExistingResourceGroup(resourceGroup)
//                    .withLeafDomainLabel(trafficManagerName)
//                    .withPerformanceBasedRouting();
//
//            int endpointPriority = 1;
//            TrafficManagerProfile.DefinitionStages.WithCreate profileWithCreate = null;
//            for (String publicIpResourceId : publicIpResourceIds) {
//                String endpointName = String.format("azendpoint-%d", endpointPriority);
//                if (endpointPriority == 1) {
//                    profileWithCreate = profileWithEndpoint.defineAzureTargetEndpoint(endpointName)
//                            .toResourceId(publicIpResourceId)
//                            .withRoutingPriority(endpointPriority)
//                            .attach();
//                } else {
//                    profileWithCreate = profileWithCreate.defineAzureTargetEndpoint(endpointName)
//                            .toResourceId(publicIpResourceId)
//                            .withRoutingPriority(endpointPriority)
//                            .attach();
//                }
//                endpointPriority++;
//            }
//
//            System.out.println("Creating a traffic manager profile for the VMs");
//            stopwatch.reset();
//            stopwatch.start();
//
//            TrafficManagerProfile trafficManagerProfile = profileWithCreate.create();
//
//            stopwatch.stop();
//            System.out.println("Created a traffic manager profile (took " + (stopwatch.getTime() / 1000) + " seconds to create): " + trafficManagerProfile.id());
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
            //
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

    private CreateVirtualMachinesInParallel() {

    }
}
