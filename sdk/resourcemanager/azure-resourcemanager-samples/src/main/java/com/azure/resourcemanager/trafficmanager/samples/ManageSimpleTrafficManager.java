// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import org.apache.commons.lang.time.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        final String rgName = azure.sdkContext().randomResourceName("rgCOPD", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
        final int vmCountPerRegion = 2;
        Set<Region> regions = new HashSet<>(Arrays.asList(
                Region.US_EAST2,
                Region.ASIA_SOUTHEAST
        ));

        try {
            //=============================================================
            // Create a shared resource group for all the resources so they can all be deleted together
            //
            ResourceGroup resourceGroup = azure.resourceGroups()
                    .define(rgName)
                    .withRegion(Region.US_EAST2)
                    .create();

            System.out.println("Created a new resource group - " + resourceGroup.id());

            // Prepare a batch of creatable VM definitions to put behind the traffic manager
            //
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

            for (Region region : regions) {
                String linuxVMNamePrefix = azure.sdkContext().randomResourceName("vm", 15);
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
            String trafficManagerName = azure.sdkContext().randomResourceName("tra", 15);
            TrafficManagerProfile.DefinitionStages.WithEndpoint profileWithEndpoint = azure.trafficManagerProfiles()
                    .define(trafficManagerName)
                        .withExistingResourceGroup(resourceGroup)
                        .withLeafDomainLabel(trafficManagerName)
                        .withPerformanceBasedRouting();

            TrafficManagerProfile.DefinitionStages.WithCreate profileWithCreate = null;
            int routingPriority = 1;
            for (VirtualMachine vm : virtualMachines) {
                String endpointName = azure.sdkContext().randomResourceName("ep", 15);
                profileWithCreate = profileWithEndpoint.defineAzureTargetEndpoint(endpointName)
                        .toResourceId(vm.getPrimaryPublicIPAddressId())
                        .withRoutingPriority(routingPriority++)
                        .attach();
            }

            stopwatch.reset();
            stopwatch.start();

            TrafficManagerProfile trafficManagerProfile = profileWithCreate.create();

            stopwatch.stop();
            System.out.printf("Created a traffic manager profile %s in %d seconds.%n", trafficManagerProfile.id(), stopwatch.getTime() / 1000);

            //=============================================================
            // Modify the traffic manager to use priority based routing
            //

            trafficManagerProfile.update()
                    .withPriorityBasedRouting()
                    .apply();

            System.out.println("Modified the traffic manager to use priority-based routing.");
            return true;
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

    private ManageSimpleTrafficManager() {
    }
}
