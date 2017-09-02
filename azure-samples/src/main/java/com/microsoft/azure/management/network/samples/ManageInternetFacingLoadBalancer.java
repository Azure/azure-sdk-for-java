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
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.TransportProtocol;
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
 * Azure Network sample for managing Internet facing load balancers -
 *
 * High-level ...
 *
 * - Create an Internet facing load balancer that receives network traffic on
 *   port 80 &amp; 443 and sends load-balanced traffic to two virtual machines
 *
 * - Create NAT rules for SSH and TELNET access to virtual
 *   machines behind the load balancer
 *
 * - Create health probes
 *
 * Details ...
 *
 * Create an Internet facing load balancer with ...
 * - A frontend public IP address
 * - Two backend address pools which contain network interfaces for the virtual
 *   machines to receive HTTP and HTTPS network traffic from the load balancer
 * - Two load balancing rules for HTTP and HTTPS to map public ports on the load
 *   balancer to ports in the backend address pool
 * - Two probes which contain HTTP and HTTPS health probes used to check availability
 *   of virtual machines in the backend address pool
 * - Two inbound NAT rules which contain rules that map a public port on the load
 *   balancer to a port for a specific virtual machine in the backend address pool
 * - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23
 *
 * Create two network interfaces in the frontend subnet ...
 * - And associate network interfaces to backend pools and NAT rules
 *
 * Create two virtual machines in the frontend subnet ...
 * - And assign network interfaces
 *
 * Update an existing load balancer, configure TCP idle timeout
 * Create another load balancer
 * Remove an existing load balancer
 */
public final class ManageInternetFacingLoadBalancer {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgNEML", 15);

        final String vnetName = SdkContext.randomResourceName("vnet", 24);

        final String loadBalancerName1 = SdkContext.randomResourceName("intlb1" + "-", 18);
        final String loadBalancerName2 = SdkContext.randomResourceName("intlb2" + "-", 18);
        final String publicIpName1 = "pip1-" + loadBalancerName1;
        final String publicIpName2 = "pip2-" + loadBalancerName1;
        final String frontendName = loadBalancerName1 + "-FE1";
        final String backendPoolName1 = loadBalancerName1 + "-BAP1";
        final String backendPoolName2 = loadBalancerName1 + "-BAP2";

        final String httpProbe = "httpProbe";
        final String httpsProbe = "httpsProbe";
        final String httpLoadBalancingRule = "httpRule";
        final String httpsLoadBalancingRule = "httpsRule";
        final String natRule5000to22forVM1 = "nat5000to22forVM1";
        final String natRule5001to23forVM1 = "nat5001to23forVM1";
        final String natRule5002to22forVM2 = "nat5002to22forVM2";
        final String natRule5003to23forVM2 = "nat5003to23forVM2";

        final String networkInterfaceName1 = SdkContext.randomResourceName("nic1", 24);
        final String networkInterfaceName2 = SdkContext.randomResourceName("nic2", 24);

        final String availSetName = SdkContext.randomResourceName("av", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
        try {

            //=============================================================
            // Create a virtual network with a frontend and a backend subnets
            System.out.println("Creating virtual network with a frontend and a backend subnets...");

            Network network = azure.networks().define(vnetName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("172.16.0.0/16")
                    .defineSubnet("Front-end")
                        .withAddressPrefix("172.16.1.0/24")
                        .attach()
                    .defineSubnet("Back-end")
                        .withAddressPrefix("172.16.3.0/24")
                        .attach()
                    .create();

            System.out.println("Created a virtual network");

            // Print the virtual network details
            Utils.print(network);

            //=============================================================
            // Create a public IP address
            System.out.println("Creating a public IP address...");

            PublicIPAddress publicIPAddress = azure.publicIPAddresses().define(publicIpName1)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withLeafDomainLabel(publicIpName1)
                    .create();

            System.out.println("Created a public IP address");

            // Print the virtual network details
            Utils.print(publicIPAddress);

            //=============================================================
            // Create an Internet facing load balancer
            // Create a frontend IP address
            // Two backend address pools which contain network interfaces for the virtual
            //  machines to receive HTTP and HTTPS network traffic from the load balancer
            // Two load balancing rules for HTTP and HTTPS to map public ports on the load
            //  balancer to ports in the backend address pool
            // Two probes which contain HTTP and HTTPS health probes used to check availability
            //  of virtual machines in the backend address pool
            // Two inbound NAT rules which contain rules that map a public port on the load
            //  balancer to a port for a specific virtual machine in the backend address pool
            //  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23

            System.out.println("Creating a Internet facing load balancer with ...");
            System.out.println("- A frontend public IP address");
            System.out.println("- Two backend address pools which contain network interfaces for the virtual\n"
                    + "  machines to receive HTTP and HTTPS network traffic from the load balancer");
            System.out.println("- Two load balancing rules for HTTP and HTTPS to map public ports on the load\n"
                    + "  balancer to ports in the backend address pool");
            System.out.println("- Two probes which contain HTTP and HTTPS health probes used to check availability\n"
                    + "  of virtual machines in the backend address pool");
            System.out.println("- Two inbound NAT rules which contain rules that map a public port on the load\n"
                    + "  balancer to a port for a specific virtual machine in the backend address pool\n"
                    + "  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23");

            LoadBalancer loadBalancer1 = azure.loadBalancers().define(loadBalancerName1)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)

                    // Add two rules that uses above backend and probe
                    .defineLoadBalancingRule(httpLoadBalancingRule)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(80)
                        .toBackend(backendPoolName1)
                        .withProbe(httpProbe)
                        .attach()

                    .defineLoadBalancingRule(httpsLoadBalancingRule)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(443)
                        .toBackend(backendPoolName2)
                        .withProbe(httpsProbe)
                        .attach()

                    // Add two nat pools to enable direct VM connectivity for
                    //  SSH to port 22 and TELNET to port 23
                    .defineInboundNatRule(natRule5000to22forVM1)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5000)
                        .toBackendPort(22)
                        .attach()

                    .defineInboundNatRule(natRule5001to23forVM1)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5001)
                        .toBackendPort(23)
                        .attach()

                    .defineInboundNatRule(natRule5002to22forVM2)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5002)
                        .toBackendPort(22)
                        .attach()

                    .defineInboundNatRule(natRule5003to23forVM2)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5003)
                        .toBackendPort(23)
                        .attach()

                    // Explicitly define the frontend
                    .definePublicFrontend(frontendName)
                        .withExistingPublicIPAddress(publicIPAddress)
                        .attach()

                    // Add two probes one per rule
                    .defineHttpProbe(httpProbe)
                        .withRequestPath("/")
                        .withPort(80)
                        .attach()

                    .defineHttpProbe(httpsProbe)
                        .withRequestPath("/")
                        .withPort(443)
                        .attach()

                    .create();

            // Print load balancer details
            System.out.println("Created a load balancer");
            Utils.print(loadBalancer1);

            //=============================================================
            // Define two network interfaces in the frontend subnet
            // associate network interfaces to NAT rules, backend pools

            System.out.println("Creating two network interfaces in the frontend subnet ...");
            System.out.println("- And associating network interfaces to backend pools and NAT rules");

            List <Creatable<NetworkInterface>> networkInterfaceCreatables = new ArrayList<Creatable<NetworkInterface>>();

            Creatable<NetworkInterface> networkInterface1Creatable = azure.networkInterfaces().define(networkInterfaceName1)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Front-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingLoadBalancerBackend(loadBalancer1, backendPoolName1)
                    .withExistingLoadBalancerBackend(loadBalancer1, backendPoolName2)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer1, natRule5000to22forVM1)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer1, natRule5001to23forVM1);

            networkInterfaceCreatables.add(networkInterface1Creatable);

            Creatable<NetworkInterface> networkInterface2Creatable = azure.networkInterfaces().define(networkInterfaceName2)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Front-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingLoadBalancerBackend(loadBalancer1, backendPoolName1)
                    .withExistingLoadBalancerBackend(loadBalancer1, backendPoolName2)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer1, natRule5002to22forVM2)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer1, natRule5003to23forVM2);

            networkInterfaceCreatables.add(networkInterface2Creatable);


            //=============================================================
            // Define an availability set

            Creatable<AvailabilitySet> availSet1Definition = azure.availabilitySets().define(availSetName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withFaultDomainCount(2)
                    .withUpdateDomainCount(4)
                    .withSku(AvailabilitySetSkuTypes.MANAGED);


            //=============================================================
            // Create two virtual machines and assign network interfaces

            System.out.println("Creating two virtual machines in the frontend subnet ...");
            System.out.println("- And assigning network interfaces");

            List <Creatable<VirtualMachine>> virtualMachineCreateables1 = new ArrayList<Creatable<VirtualMachine>>();

            for (Creatable<NetworkInterface> nicDefinition : networkInterfaceCreatables) {
                virtualMachineCreateables1.add(azure.virtualMachines().define(SdkContext.randomResourceName("lVM1", 24))
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
                        .withNewPrimaryNetworkInterface(nicDefinition)
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withSsh(sshKey)
                        .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                        .withNewAvailabilitySet(availSet1Definition));
            }

            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            Collection<VirtualMachine> virtualMachines = azure.virtualMachines().create(virtualMachineCreateables1).values();

            stopwatch.stop();
            System.out.println("Created 2 Linux VMs: (took " + (stopwatch.getTime() / 1000) + " seconds) ");
            System.out.println();

            // Print virtual machine details
            for (VirtualMachine vm : virtualMachines) {
                Utils.print(vm);
                System.out.println();
            }

            //=============================================================
            // Update a load balancer
            //  configure TCP idle timeout to 15 minutes

            System.out.println("Updating the load balancer ...");

            loadBalancer1.update()
                    .updateLoadBalancingRule(httpLoadBalancingRule)
                        .withIdleTimeoutInMinutes(15)
                        .parent()
                    .updateLoadBalancingRule(httpsLoadBalancingRule)
                        .withIdleTimeoutInMinutes(15)
                        .parent()
                    .apply();

            System.out.println("Update the load balancer with a TCP idle timeout to 15 minutes");


            //=============================================================
            // Create another public IP address
            System.out.println("Creating another public IP address...");

            PublicIPAddress publicIPAddress2 = azure.publicIPAddresses().define(publicIpName2)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withLeafDomainLabel(publicIpName2)
                    .create();

            System.out.println("Created another public IP address");
            // Print the virtual network details
            Utils.print(publicIPAddress2);


            //=============================================================
            // Create another Internet facing load balancer
            // Create a frontend IP address
            // Two backend address pools which contain network interfaces for the virtual
            //  machines to receive HTTP and HTTPS network traffic from the load balancer
            // Two load balancing rules for HTTP and HTTPS to map public ports on the load
            //  balancer to ports in the backend address pool
            // Two probes which contain HTTP and HTTPS health probes used to check availability
            //  of virtual machines in the backend address pool
            // Two inbound NAT rules which contain rules that map a public port on the load
            //  balancer to a port for a specific virtual machine in the backend address pool
            //  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23

            System.out.println("Creating another Internet facing load balancer with ...");
            System.out.println("- A frontend IP address");
            System.out.println("- Two backend address pools which contain network interfaces for the virtual\n"
                    + "  machines to receive HTTP and HTTPS network traffic from the load balancer");
            System.out.println("- Two load balancing rules for HTTP and HTTPS to map public ports on the load\n"
                    + "  balancer to ports in the backend address pool");
            System.out.println("- Two probes which contain HTTP and HTTPS health probes used to check availability\n"
                    + "  of virtual machines in the backend address pool");
            System.out.println("- Two inbound NAT rules which contain rules that map a public port on the load\n"
                    + "  balancer to a port for a specific virtual machine in the backend address pool\n"
                    + "  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23");

            LoadBalancer loadBalancer2 = azure.loadBalancers().define(loadBalancerName2)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)

                    // Add two rules that uses above backend and probe
                    .defineLoadBalancingRule(httpLoadBalancingRule)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(80)
                        .toBackend(backendPoolName1)
                        .withProbe(httpProbe)
                        .attach()

                    .defineLoadBalancingRule(httpsLoadBalancingRule)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(443)
                        .toBackend(backendPoolName2)
                        .withProbe(httpsProbe)
                        .attach()

                    // Add two nat pools to enable direct VM connectivity for
                    //  SSH to port 22 and TELNET to port 23
                    .defineInboundNatRule(natRule5000to22forVM1)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5000)
                        .toBackendPort(22)
                        .attach()

                    .defineInboundNatRule(natRule5001to23forVM1)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5001)
                        .toBackendPort(23)
                        .attach()

                    .defineInboundNatRule(natRule5002to22forVM2)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5002)
                        .toBackendPort(22)
                        .attach()

                    .defineInboundNatRule(natRule5003to23forVM2)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPort(5003)
                        .toBackendPort(23)
                        .attach()

                    // Explicitly define the frontend
                    .definePublicFrontend(frontendName)
                        .withExistingPublicIPAddress(publicIPAddress2)
                        .attach()

                    // Add two probes one per rule
                    .defineHttpProbe(httpProbe)
                        .withRequestPath("/")
                        .withPort(80)
                        .attach()

                    .defineHttpProbe(httpsProbe)
                        .withRequestPath("/")
                        .withPort(443)
                        .attach()

                    .create();

            // Print load balancer details
            System.out.println("Created another load balancer");
            Utils.print(loadBalancer2);

            //=============================================================
            // List load balancers

            List<LoadBalancer> loadBalancers = azure.loadBalancers().list();

            System.out.println("Walking through the list of load balancers");

            for (LoadBalancer loadBalancer : loadBalancers) {
                Utils.print(loadBalancer);
            }


            //=============================================================
            // Remove a load balancer

            System.out.println("Deleting load balancer " + loadBalancerName2
                    + "(" + loadBalancer2.id() + ")");
            azure.loadBalancers().deleteById(loadBalancer2.id());
            System.out.println("Deleted load balancer" + loadBalancerName2);

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
    private ManageInternetFacingLoadBalancer() {

    }
}
