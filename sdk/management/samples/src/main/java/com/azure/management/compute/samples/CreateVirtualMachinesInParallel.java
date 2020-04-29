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
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Azure compute sample for creating multiple virtual machines in parallel.
 */
public final class CreateVirtualMachinesInParallel {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgCOPD", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

        Map<Region, Integer> virtualMachinesByLocation = new HashMap<Region, Integer>();

        // debug target
        /**
         virtualMachinesByLocation.put(Region.US_EAST, 5);
         virtualMachinesByLocation.put(Region.US_SOUTH_CENTRAL, 5);
         */

        // final demo target
        virtualMachinesByLocation.put(Region.US_EAST, 4);
        virtualMachinesByLocation.put(Region.US_SOUTH_CENTRAL, 4);
        virtualMachinesByLocation.put(Region.US_WEST, 4);
        virtualMachinesByLocation.put(Region.US_NORTH_CENTRAL, 4);
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
            ResourceGroup resourceGroup = azure.resourceGroups().define(rgName)
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
                String networkName = SdkContext.randomResourceName("vnetCOPD-", 20);
                Creatable<Network> networkCreatable = azure.networks().define(networkName)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withAddressSpace("172.16.0.0/16");

                //=============================================================
                // Create 1 storage creatable per region (For storing VMs disk)
                //
                String storageAccountName = SdkContext.randomResourceName("stgcopd", 20);
                Creatable<StorageAccount> storageAccountCreatable = azure.storageAccounts().define(storageAccountName)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup);

                String linuxVMNamePrefix = SdkContext.randomResourceName("vm-", 15);
                for (int i = 1; i <= vmCount; i++) {

                    //=============================================================
                    // Create 1 public IP address creatable
                    //
                    Creatable<PublicIPAddress> publicIPAddressCreatable = azure.publicIPAddresses()
                            .define(String.format("%s-%d", linuxVMNamePrefix, i))
                                .withRegion(region)
                                .withExistingResourceGroup(resourceGroup)
                                .withLeafDomainLabel(SdkContext.randomResourceName("pip", 10));

                    publicIpCreatableKeys.add(publicIPAddressCreatable.key());

                    //=============================================================
                    // Create 1 virtual machine creatable
                    Creatable<VirtualMachine> virtualMachineCreatable = azure.virtualMachines()
                            .define(String.format("%s-%d", linuxVMNamePrefix, i))
                                .withRegion(region)
                                .withExistingResourceGroup(resourceGroup)
                                .withNewPrimaryNetwork(networkCreatable)
                                .withPrimaryPrivateIPAddressDynamic()
                                .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
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

            StopWatch stopwatch = new StopWatch();
            System.out.println("Creating the virtual machines");
            stopwatch.start();

            CreatedResources<VirtualMachine> virtualMachines = azure.virtualMachines().create(creatableVirtualMachines);

            stopwatch.stop();
            System.out.println("Created virtual machines");

            for (VirtualMachine virtualMachine : virtualMachines.values()) {
                System.out.println(virtualMachine.id());
            }

            System.out.println("Virtual Machines created: (took " + (stopwatch.getTime() / 1000) + " seconds to create) == " + virtualMachines.size() + " == virtual machines");

            List<String> publicIpResourceIds = new ArrayList<>();
            for (String publicIpCreatableKey : publicIpCreatableKeys) {
                PublicIPAddress pip = (PublicIPAddress) virtualMachines.createdRelatedResource(publicIpCreatableKey);
                publicIpResourceIds.add(pip.id());
            }

            //=============================================================
            // Create 1 Traffic Manager Profile
            //
            String trafficManagerName = SdkContext.randomResourceName("tra", 15);
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

            System.out.println("Creating a traffic manager profile for the VMs");
            stopwatch.reset();
            stopwatch.start();

            TrafficManagerProfile trafficManagerProfile = profileWithCreate.create();

            stopwatch.stop();
            System.out.println("Created a traffic manager profile (took " + (stopwatch.getTime() / 1000) + " seconds to create): " + trafficManagerProfile.id());
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
            //
            System.out.println("AZURE_AUTH_LOCATION_2=" + System.getenv("AZURE_AUTH_LOCATION_2"));
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION_2"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(LogLevel.NONE)
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

    private CreateVirtualMachinesInParallel() {

    }
}
