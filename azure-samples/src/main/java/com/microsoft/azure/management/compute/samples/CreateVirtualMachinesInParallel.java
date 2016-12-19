/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Azure compute sample for creating multiple virtual machines in parallel.
 */
public final class CreateVirtualMachinesInParallel {

    /**
     * Main entry point.
     * @param args the parameters
     */
    public static void main(String[] args) {

        final String rgName = ResourceNamer.randomResourceName("rgCOPD", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

        Map<Region, Integer> virtualMachinesByLocation = new HashMap<Region, Integer>();
        
        // debug target
        /**
        virtualMachinesByLocation.put(Region.US_EAST, 5);
        virtualMachinesByLocation.put(Region.US_SOUTH_CENTRAL, 5);
        */

        // final demo target
        virtualMachinesByLocation.put(Region.US_EAST, 12);
        virtualMachinesByLocation.put(Region.US_SOUTH_CENTRAL, 12);
        virtualMachinesByLocation.put(Region.US_WEST, 12);
        virtualMachinesByLocation.put(Region.US_NORTH_CENTRAL, 12);
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
            // Authenticate
            //
            System.out.println("AZURE_AUTH_LOCATION_2=" + System.getenv("AZURE_AUTH_LOCATION_2"));
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION_2"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            try {
                //=============================================================
                // Create a resource group (Where all resources gets created)
                //
                ResourceGroup resourceGroup = azure.resourceGroups()
                        .define(rgName)
                        .withRegion(Region.US_EAST)
                        .create();

                System.out.println("Created a new resource group - " + resourceGroup.id());

                List<String> publicIpCreatableKeys = new ArrayList<>();
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
                    String networkName = ResourceNamer.randomResourceName("vnetCOPD-", 20);
                    Creatable<Network> networkCreatable = azure.networks()
                            .define(networkName)
                            .withRegion(region)
                            .withExistingResourceGroup(resourceGroup)
                            .withAddressSpace("172.16.0.0/16");


                    //=============================================================
                    // Create 1 storage creatable per region (For storing VMs disk)
                    //
                    String storageAccountName = ResourceNamer.randomResourceName("stgcopd", 20);
                    Creatable<StorageAccount> storageAccountCreatable = azure.storageAccounts()
                            .define(storageAccountName)
                            .withRegion(region)
                            .withExistingResourceGroup(resourceGroup);

                    String linuxVMNamePrefix = ResourceNamer.randomResourceName("vm-", 15);
                    for (int i = 1; i <= vmCount; i++) {
                        //=============================================================
                        // Create 1 public IP address creatable
                        //
                        Creatable<PublicIpAddress> publicIpAddressCreatable = azure.publicIpAddresses()
                                .define(String.format("%s-%d", linuxVMNamePrefix, i))
                                .withRegion(region)
                                .withExistingResourceGroup(resourceGroup);

                        publicIpCreatableKeys.add(publicIpAddressCreatable.key());

                        //=============================================================
                        // Create 1 virtual machine creatable
                        Creatable<VirtualMachine> virtualMachineCreatable = azure.virtualMachines()
                                .define(String.format("%s-%d", linuxVMNamePrefix, i))
                                .withRegion(region)
                                .withExistingResourceGroup(resourceGroup)
                                .withNewPrimaryNetwork(networkCreatable)
                                .withPrimaryPrivateIpAddressDynamic()
                                .withNewPrimaryPublicIpAddress(publicIpAddressCreatable)
                                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                                .withRootUsername(userName)
                                .withSsh(sshKey)
                                .withSize(VirtualMachineSizeTypes.STANDARD_DS3_V2)
                                .withNewStorageAccount(storageAccountCreatable);
                        creatableVirtualMachines.add(virtualMachineCreatable);
                    }
                }


                //=============================================================
                // Create !!

                Date t1 = new Date();
                System.out.println("Creating the virtual machines");

                CreatedResources<VirtualMachine> virtualMachines = azure.virtualMachines().create(creatableVirtualMachines);

                Date t2 = new Date();
                System.out.println("Created virtual machines");

                for (VirtualMachine virtualMachine : virtualMachines) {
                    System.out.println(virtualMachine.id());
                }

                System.out.println("Virtual Machines created: (took " + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) to create == " + virtualMachines.size() + " == virtual machines");

                List<String> publicIpResourceIds = new ArrayList<>();
                for (String publicIpCreatableKey : publicIpCreatableKeys) {
                    PublicIpAddress pip = (PublicIpAddress) virtualMachines.createdRelatedResource(publicIpCreatableKey);
                    publicIpResourceIds.add(pip.id());
                }

                //=============================================================
                // Create 1 Traffic Manager Profile
                //
                String trafficManagerName = ResourceNamer.randomResourceName("tra", 15);
                TrafficManagerProfile.DefinitionStages.WithEndpoint profileWithEndpoint = azure.trafficManagerProfiles().define(trafficManagerName)
                        .withExistingResourceGroup(resourceGroup)
                        .withLeafDomainLabel(trafficManagerName)
                        .withPerformanceBasedRouting();

                int endpointPriority = 1;
                TrafficManagerProfile.DefinitionStages.WithCreate profileWithCreate = null;
                for (String publicIpResourceId : publicIpResourceIds) {
                    String endpointName = String.format("azendpoint-%d", endpointPriority);
                    if (endpointPriority == 1) {
                        profileWithCreate = profileWithEndpoint.defineAzureTargetEndpoint(endpointName)
                                .toResourceId(publicIpResourceId)
                                .withRoutingPriority(endpointPriority)
                                .attach();
                    } else {
                        profileWithCreate = profileWithCreate.defineAzureTargetEndpoint(endpointName)
                                .toResourceId(publicIpResourceId)
                                .withRoutingPriority(endpointPriority)
                                .attach();
                    }
                    endpointPriority++;
                }

                TrafficManagerProfile trafficManagerProfile = profileWithCreate.create();
                System.out.println("Created a traffic manager profile - " + trafficManagerProfile.id());
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private CreateVirtualMachinesInParallel() {

    }
}
