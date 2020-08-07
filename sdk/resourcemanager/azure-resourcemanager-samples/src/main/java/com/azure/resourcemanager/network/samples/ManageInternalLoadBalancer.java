// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import org.apache.commons.lang.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Azure Network sample for managing internal load balancers -
 * <p>
 * High-level ...
 * <p>
 * - Create an internal load balancer that receives network traffic on
 * port 1521 (Oracle SQL Node Port) and sends load-balanced traffic
 * to two virtual machines
 * <p>
 * - Create NAT rules for SSH and TELNET access to virtual
 * machines behind the load balancer
 * <p>
 * - Create a health probe
 * <p>
 * Details ...
 * <p>
 * Create an internal facing load balancer with ...
 * - A frontend private IP address
 * - One backend address pool which contains network interfaces for the virtual
 * machines to receive 1521 (Oracle SQL Node Port) network traffic from the load balancer
 * - One load balancing rule fto map port 1521 on the load balancer to
 * ports in the backend address pool
 * - One probe which contains HTTP health probe used to check availability
 * of virtual machines in the backend address pool
 * - Two inbound NAT rules which contain rules that map a public port on the load
 * balancer to a port for a specific virtual machine in the backend address pool
 * - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23
 * <p>
 * Create two network interfaces in the backend subnet ...
 * - And associate network interfaces to backend pools and NAT rules
 * <p>
 * Create two virtual machines in the backend subnet ...
 * - And assign network interfaces
 * <p>
 * Update an existing load balancer, configure TCP idle timeout
 * Create another load balancer
 * List load balancers
 * Remove an existing load balancer.
 */
public final class ManageInternalLoadBalancer {
    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgNEML", 15);

        final String vnetName = azure.sdkContext().randomResourceName("vnet", 24);

        final String loadBalancerName3 = azure.sdkContext().randomResourceName("intlb3" + "-", 18);
        final String loadBalancerName4 = azure.sdkContext().randomResourceName("intlb4" + "-", 18);
        final String privateFrontEndName = loadBalancerName3 + "-BE";

        final String backendPoolName3 = loadBalancerName3 + "-BAP3";

        final int orcaleSQLNodePort = 1521;
        final String httpProbe = "httpProbe";
        final String tcpLoadBalancingRule = "tcpRule";
        final String natRule6000to22forVM3 = "nat6000to22forVM3";
        final String natRule6001to23forVM3 = "nat6001to23forVM3";
        final String natRule6002to22forVM4 = "nat6002to22forVM4";
        final String natRule6003to23forVM4 = "nat6003to23forVM4";

        final String networkInterfaceName3 = azure.sdkContext().randomResourceName("nic3", 24);
        final String networkInterfaceName4 = azure.sdkContext().randomResourceName("nic4", 24);

        final String availSetName = azure.sdkContext().randomResourceName("av2", 24);
        final String vmName3 = azure.sdkContext().randomResourceName("lVM3", 24);
        final String vmName4 = azure.sdkContext().randomResourceName("lVM4", 24);
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
            // Create an internal load balancer
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

            System.out.println("Creating an internal facing load balancer with ...");
            System.out.println("- A private IP address");
            System.out.println("- One backend address pool which contain network interfaces for the virtual\n"
                    + "  machines to receive 1521 network traffic from the load balancer");
            System.out.println("- One load balancing rules for 1521 to map public ports on the load\n"
                    + "  balancer to ports in the backend address pool");
            System.out.println("- One probe which contains HTTP health probe used to check availability\n"
                    + "  of virtual machines in the backend address pool");
            System.out.println("- Two inbound NAT rules which contain rules that map a port on the load\n"
                    + "  balancer to a port for a specific virtual machine in the backend address pool\n"
                    + "  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23");

            LoadBalancer loadBalancer3 = azure.loadBalancers().define(loadBalancerName3)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)

                    // Add one rule that uses above backend and probe
                    .defineLoadBalancingRule(tcpLoadBalancingRule)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(orcaleSQLNodePort)
                    .toBackend(backendPoolName3)
                    .withProbe(httpProbe)
                    .attach()

                    // Add two nat pools to enable direct VM connectivity for
                    //  SSH to port 22 and TELNET to port 23
                    .defineInboundNatRule(natRule6000to22forVM3)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6000)
                    .toBackendPort(22)
                    .attach()

                    .defineInboundNatRule(natRule6001to23forVM3)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6001)
                    .toBackendPort(23)
                    .attach()

                    .defineInboundNatRule(natRule6002to22forVM4)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6002)
                    .toBackendPort(22)
                    .attach()

                    .defineInboundNatRule(natRule6003to23forVM4)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6003)
                    .toBackendPort(23)
                    .attach()

                    // Explicitly define the frontend
                    .definePrivateFrontend(privateFrontEndName)
                    .withExistingSubnet(network, "Back-end")
                    .withPrivateIpAddressStatic("172.16.3.5")
                    .attach()

                    // Add one probes - one per rule
                    .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()

                    .create();

            // Print load balancer details
            System.out.println("Created an internal load balancer");
            Utils.print(loadBalancer3);

            //=============================================================
            // Define two network interfaces in the backend subnet
            // associate network interfaces to NAT rules, backend pools

            Creatable<NetworkInterface> networkInterface3Creatable = azure.networkInterfaces().define(networkInterfaceName3)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Back-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingLoadBalancerBackend(loadBalancer3, backendPoolName3)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer3, natRule6000to22forVM3)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer3, natRule6001to23forVM3);

            Creatable<NetworkInterface> networkInterface4Creatable = azure.networkInterfaces().define(networkInterfaceName4)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet("Back-end")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withExistingLoadBalancerBackend(loadBalancer3, backendPoolName3)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer3, natRule6002to22forVM4)
                    .withExistingLoadBalancerInboundNatRule(loadBalancer3, natRule6003to23forVM4);

            //=============================================================
            // Define an availability set

            Creatable<AvailabilitySet> availSet2Definition = azure.availabilitySets().define(availSetName)
                    .withRegion(Region.US_EAST)
                    .withNewResourceGroup(rgName)
                    .withFaultDomainCount(2)
                    .withUpdateDomainCount(4)
                    .withSku(AvailabilitySetSkuTypes.ALIGNED);

            //=============================================================
            // Create two virtual machines and assign network interfaces

            System.out.println("Creating two virtual machines in the frontend subnet ...");
            System.out.println("- And assigning network interfaces");

            List<Creatable<VirtualMachine>> virtualMachineCreateables2 = new ArrayList<Creatable<VirtualMachine>>();

            Creatable<VirtualMachine> virtualMachine3Creatable = azure.virtualMachines().define(vmName3)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetworkInterface(networkInterface3Creatable)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .withNewAvailabilitySet(availSet2Definition);

            virtualMachineCreateables2.add(virtualMachine3Creatable);

            Creatable<VirtualMachine> virtualMachine4Creatable = azure.virtualMachines().define(vmName4)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetworkInterface(networkInterface4Creatable)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .withNewAvailabilitySet(availSet2Definition);

            virtualMachineCreateables2.add(virtualMachine4Creatable);

            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            Collection<VirtualMachine> virtualMachines = azure.virtualMachines().create(virtualMachineCreateables2).values();

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

            loadBalancer3.update()
                    .updateLoadBalancingRule(tcpLoadBalancingRule)
                    .withIdleTimeoutInMinutes(15)
                    .parent()
                    .apply();

            System.out.println("Update the load balancer with a TCP idle timeout to 15 minutes");


            //=============================================================
            // Create another internal load balancer
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

            System.out.println("Creating another internal facing load balancer with ...");
            System.out.println("- A private IP address");
            System.out.println("- One backend address pool which contain network interfaces for the virtual\n"
                    + "  machines to receive 1521 network traffic from the load balancer");
            System.out.println("- One load balancing rules for 1521 to map public ports on the load\n"
                    + "  balancer to ports in the backend address pool");
            System.out.println("- One probe which contains HTTP health probe used to check availability\n"
                    + "  of virtual machines in the backend address pool");
            System.out.println("- Two inbound NAT rules which contain rules that map a port on the load\n"
                    + "  balancer to a port for a specific virtual machine in the backend address pool\n"
                    + "  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23");

            LoadBalancer loadBalancer4 = azure.loadBalancers().define(loadBalancerName4)
                    .withRegion(Region.US_EAST)
                    .withExistingResourceGroup(rgName)

                    // Add one rule that uses above backend and probe
                    .defineLoadBalancingRule(tcpLoadBalancingRule)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(orcaleSQLNodePort)
                    .toBackend(backendPoolName3)
                    .withProbe(httpProbe)
                    .attach()

                    // Add two nat pools to enable direct VM connectivity for
                    //  SSH to port 22 and TELNET to port 23
                    .defineInboundNatRule(natRule6000to22forVM3)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6000)
                    .toBackendPort(22)
                    .attach()

                    .defineInboundNatRule(natRule6001to23forVM3)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6001)
                    .toBackendPort(23)
                    .attach()

                    .defineInboundNatRule(natRule6002to22forVM4)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6002)
                    .toBackendPort(22)
                    .attach()

                    .defineInboundNatRule(natRule6003to23forVM4)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(privateFrontEndName)
                    .fromFrontendPort(6003)
                    .toBackendPort(23)
                    .attach()

                    // Explicitly define the frontend
                    .definePrivateFrontend(privateFrontEndName)
                    .withExistingSubnet(network, "Back-end")
                    .withPrivateIpAddressStatic("172.16.3.15")
                    .attach()

                    // Add one probes - one per rule
                    .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()

                    .create();

            // Print load balancer details
            System.out.println("Created an internal load balancer");
            Utils.print(loadBalancer4);

            //=============================================================
            // List load balancers

            PagedIterable<LoadBalancer> loadBalancers = azure.loadBalancers().list();

            System.out.println("Walking through the list of load balancers");

            for (LoadBalancer loadBalancer : loadBalancers) {
                Utils.print(loadBalancer);
                System.out.println();
            }


            //=============================================================
            // Remove a load balancer

            System.out.println("Deleting load balancer " + loadBalancerName4
                    + "(" + loadBalancer4.id() + ")");
            azure.loadBalancers().deleteById(loadBalancer4.id());
            System.out.println("Deleted load balancer" + loadBalancerName4);

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
     *
     * @param args parameters.\
     */

    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

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

    private ManageInternalLoadBalancer() {

    }

}
