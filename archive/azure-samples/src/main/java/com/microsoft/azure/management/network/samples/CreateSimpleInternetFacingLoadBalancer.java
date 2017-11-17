/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySetSkuTypes;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;

/**
 * Azure Network sample for creating a simple Internet facing load balancer -
 *
 * Summary ...
 *
 * - This sample creates a simple Internet facing load balancer that receives network traffic on
 *   port 80 and sends load-balanced traffic to two virtual machines
 *
 * Details ...
 *
 * 1. Create two virtual machines for the backend...
 * - in the same availability set
 * - in the same virtual network
 *
 * Create an Internet facing load balancer with ...
 * - A public IP address assigned to an implicitly created frontend
 * - One backend address pool with the two virtual machines to receive HTTP network traffic from the load balancer
 * - One load balancing rule for HTTP to map public ports on the load
 *   balancer to ports in the backend address pool

 * Delete the load balancer
 */

public final class CreateSimpleInternetFacingLoadBalancer {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_EAST;
        final String resourceGroupName = SdkContext.randomResourceName("rg", 15);
        final String vnetName = SdkContext.randomResourceName("vnet", 24);
        final String loadBalancerName = SdkContext.randomResourceName("lb" + "-", 18);
        final String publicIpName = SdkContext.randomResourceName("pip", 18);
        final String httpLoadBalancingRule = "httpRule";

        final String availabilitySetName = SdkContext.randomResourceName("av", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
        try {

            //=============================================================
            // Define a common availability set for the backend virtual machines

            Creatable<AvailabilitySet> availabilitySetDefinition = azure.availabilitySets().define(availabilitySetName)
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupName)
                    .withSku(AvailabilitySetSkuTypes.MANAGED);

            //=============================================================
            // Define a common virtual network for the virtual machines

            Creatable<Network> networkDefinition = azure.networks().define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupName)
                    .withAddressSpace("10.0.0.0/28");

            //=============================================================
            // Create two virtual machines for the backend of the load balancer

            System.out.println("Creating two virtual machines in the frontend subnet ...\n"
                    + "and putting them in the shared availability set and virtual network.");

            List<Creatable<VirtualMachine>> virtualMachineDefinitions = new ArrayList<Creatable<VirtualMachine>>();

            for (int i = 0; i < 2; i++) {
                virtualMachineDefinitions.add(
                        azure.virtualMachines().define(SdkContext.randomResourceName("vm", 24))
                            .withRegion(region)
                            .withExistingResourceGroup(resourceGroupName)
                            .withNewPrimaryNetwork(networkDefinition)
                            .withPrimaryPrivateIPAddressDynamic()
                            .withoutPrimaryPublicIPAddress()
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername(userName)
                            .withSsh(sshKey)
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .withNewAvailabilitySet(availabilitySetDefinition));
            }

            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            // Create and retrieve the VMs by the interface accepted by the load balancing rule
            Collection<VirtualMachine> virtualMachines = azure.virtualMachines().create(virtualMachineDefinitions).values();

            stopwatch.stop();
            System.out.println("Created 2 Linux VMs: (took " + (stopwatch.getTime() / 1000) + " seconds)\n");

            // Print virtual machine details
            for (VirtualMachine vm : virtualMachines) {
                Utils.print((VirtualMachine) vm);
                System.out.println();
            }

            //=============================================================
            // Create an Internet facing load balancer
            // - implicitly creating a frontend with the public IP address definition provided for the load balancing rule
            // - implicitly creating a backend and assigning the created virtual machines to it
            // - creating a load balancing rule, mapping public ports on the load balancer to ports in the backend address pool

            System.out.println(
                    "Creating a Internet facing load balancer with ...\n"
                    + "- A frontend public IP address\n"
                    + "- One backend address pool with the two virtual machines\n"
                    + "- One load balancing rule for HTTP, mapping public ports on the load\n"
                    + "  balancer to ports in the backend address pool");

            LoadBalancer loadBalancer = azure.loadBalancers().define(loadBalancerName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroupName)

                    // Add a load balancing rule sending traffic from an implicitly created frontend with the public IP address
                    // to an implicitly created backend with the two virtual machines
                    .defineLoadBalancingRule(httpLoadBalancingRule)
                        .withProtocol(TransportProtocol.TCP)
                        .fromNewPublicIPAddress(publicIpName)
                        .fromFrontendPort(80)
                        .toExistingVirtualMachines(new ArrayList<HasNetworkInterfaces>(virtualMachines))    // Convert VMs to the expected interface
                        .attach()

                    .create();

            // Print load balancer details
            System.out.println("Created a load balancer");
            Utils.print(loadBalancer);

            //=============================================================
            // Update a load balancer with 15 minute idle time for the load balancing rule

            System.out.println("Updating the load balancer ...");

            loadBalancer.update()
                    .updateLoadBalancingRule(httpLoadBalancingRule)
                        .withIdleTimeoutInMinutes(15)
                        .parent()
                    .apply();

            System.out.println("Update the load balancer with a TCP idle timeout to 15 minutes");


            //=============================================================
            // Show the load balancer info

            Utils.print(loadBalancer);


            //=============================================================
            // Remove a load balancer

            System.out.println("Deleting load balancer " + loadBalancerName
                    + "(" + loadBalancer.id() + ")");
            azure.loadBalancers().deleteById(loadBalancer.id());
            System.out.println("Deleted load balancer" + loadBalancerName);

            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
            try {
                System.out.println("Deleting Resource Group: " + resourceGroupName);
                azure.resourceGroups().beginDeleteByName(resourceGroupName);
                System.out.println("Deleted Resource Group: " + resourceGroupName);
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
     * @param args parameters
     */

    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(LogLevel.BODY.withPrettyJson(true))
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
    private CreateSimpleInternetFacingLoadBalancer() {

    }
}
