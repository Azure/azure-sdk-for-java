/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.trafficmanager.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Simple Azure traffic manager sample.
 *  - Create 4 VMs spread across 2 regions
 *  - Create a traffic manager in front of the VMs
 *  - Change/configure traffic manager routing method
 */
public final class ManageSimpleTrafficManager {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgCOPD", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
        final int vmCountPerRegion = 2;
        Set<Region> regions = new HashSet<>(Arrays.asList(
                Region.US_EAST,
                Region.US_WEST
        ));

        try {
            //=============================================================
            // Create a shared resource group for all the resources so they can all be deleted together
            //
            ResourceGroup resourceGroup = azure.resourceGroups()
                    .define(rgName)
                    .withRegion(Region.US_EAST)
                    .create();

            System.out.println("Created a new resource group - " + resourceGroup.id());

            // Prepare a batch of creatable VM definitions to put behind the traffic manager
            //
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

            for (Region region : regions) {
                String linuxVMNamePrefix = SdkContext.randomResourceName("vm", 15);
                for (int i = 0; i < vmCountPerRegion; i++) {
                    //=============================================================
                    // Create a virtual machine in its own virtual network
                    String vmName = String.format("%s-%d", linuxVMNamePrefix, i);
                    Creatable<VirtualMachine> vmDefinition = azure.virtualMachines().define(vmName)
                            .withRegion(region)
                            .withExistingResourceGroup(resourceGroup)
                            .withNewPrimaryNetwork("10.0.0.0/29")
                            .withPrimaryPrivateIPAddressDynamic()
                            .withNewPrimaryPublicIPAddress(vmName)
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername(userName)
                            .withSsh(sshKey)
                            .withSize(VirtualMachineSizeTypes.STANDARD_A1);
                    creatableVirtualMachines.add(vmDefinition);
                }
            }


            //=============================================================
            // Create the VMs !!

            StopWatch stopwatch = new StopWatch();
            System.out.println("Creating the virtual machines...");
            stopwatch.start();

            Collection<VirtualMachine> virtualMachines = azure.virtualMachines().create(creatableVirtualMachines).values();

            stopwatch.stop();
            System.out.println(String.format("Created virtual machines in %d seconds.", stopwatch.getTime() / 1000));

            //=============================================================
            // Create 1 traffic manager profile
            //
            String trafficManagerName = SdkContext.randomResourceName("tra", 15);
            TrafficManagerProfile.DefinitionStages.WithEndpoint profileWithEndpoint = azure.trafficManagerProfiles()
                    .define(trafficManagerName)
                        .withExistingResourceGroup(resourceGroup)
                        .withLeafDomainLabel(trafficManagerName)
                        .withPerformanceBasedRouting();

            TrafficManagerProfile.DefinitionStages.WithCreate profileWithCreate = null;
            int routingPriority = 1;
            for (VirtualMachine vm : virtualMachines) {
                String endpointName = SdkContext.randomResourceName("ep", 15);
                profileWithCreate = profileWithEndpoint.defineAzureTargetEndpoint(endpointName)
                        .toResourceId(vm.getPrimaryPublicIPAddressId())
                        .withRoutingPriority(routingPriority++)
                        .attach();
            }

            stopwatch.reset();
            stopwatch.start();

            TrafficManagerProfile trafficManagerProfile = profileWithCreate.create();

            stopwatch.stop();
            System.out.println(String.format("Created a traffic manager profile %s\n in %d seconds.", trafficManagerProfile.id(), stopwatch.getTime() / 1000));

            //=============================================================
            // Modify the traffic manager to use priority based routing
            //

            trafficManagerProfile.update()
                    .withPriorityBasedRouting()
                    .apply();

            System.out.println("Modified the traffic manager to use priority-based routing.");
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
            System.out.println("AZURE_AUTH_LOCATION=" + System.getenv("AZURE_AUTH_LOCATION"));
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            Azure azure = Azure.authenticate(credFile).withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageSimpleTrafficManager() {
    }
}
