/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.ImageReference;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetVM;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuTypes;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNicIPConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machine scale sets with un-managed disks -
 *  - Create a virtual machine scale set behind an Internet facing load balancer
 *  - Install Apache Web servers in virtual machines in the virtual machine scale set
 *  - List the network interfaces associated with the virtual machine scale set
 *  - List scale set virtual machine instances and SSH collection string
 *  - Stop a virtual machine scale set
 *  - Start a virtual machine scale set
 *  - Update a virtual machine scale set
 *    - Double the no. of virtual machines
 *  - Restart a virtual machine scale set
 */
public final class ManageVirtualMachineScaleSetWithUnmanagedDisks {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_EAST2;
        final String rgName = SdkContext.randomResourceName("rgCOVS", 15);
        final String vnetName = SdkContext.randomResourceName("vnet", 24);
        final String loadBalancerName1 = SdkContext.randomResourceName("intlb" + "-", 18);
        final String publicIpName = "pip-" + loadBalancerName1;
        final String frontendName = loadBalancerName1 + "-FE1";
        final String backendPoolName1 = loadBalancerName1 + "-BAP1";
        final String backendPoolName2 = loadBalancerName1 + "-BAP2";

        final String httpProbe = "httpProbe";
        final String httpsProbe = "httpsProbe";
        final String httpLoadBalancingRule = "httpRule";
        final String httpsLoadBalancingRule = "httpsRule";
        final String natPool50XXto22 = "natPool50XXto22";
        final String natPool60XXto23 = "natPool60XXto23";

        final String vmssName =  SdkContext.randomResourceName("vmss", 24);
        final String storageAccountName1 = SdkContext.randomResourceName("stg1", 24);
        final String storageAccountName2 = SdkContext.randomResourceName("stg2", 24);
        final String storageAccountName3 = SdkContext.randomResourceName("stg3", 24);

        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

        final String apacheInstallScript = "https://raw.githubusercontent.com/anuchandy/azure-libraries-for-java/master/azure-samples/src/main/resources/install_apache.sh";
        final String installCommand = "bash install_apache.sh";
        List<String> fileUris = new ArrayList<>();
        fileUris.add(apacheInstallScript);
        try {
            //=============================================================
            // Create a virtual network with a frontend subnet
            System.out.println("Creating virtual network with a frontend subnet ...");

            Network network = azure.networks().define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("172.16.0.0/16")
                    .defineSubnet("Front-end")
                        .withAddressPrefix("172.16.1.0/24")
                        .attach()
                    .create();

            System.out.println("Created a virtual network");
            // Print the virtual network details
            Utils.print(network);

            //=============================================================
            // Create a public IP address
            System.out.println("Creating a public IP address...");

            PublicIPAddress publicIPAddress = azure.publicIPAddresses().define(publicIpName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLeafDomainLabel(publicIpName)
                    .create();

            System.out.println("Created a public IP address");
            // Print the virtual network details
            Utils.print(publicIPAddress);

            //=============================================================
            // Create an Internet facing load balancer with
            // One frontend IP address
            // Two backend address pools which contain network interfaces for the virtual
            //  machines to receive HTTP and HTTPS network traffic from the load balancer
            // Two load balancing rules for HTTP and HTTPS to map public ports on the load
            //  balancer to ports in the backend address pool
            // Two probes which contain HTTP and HTTPS health probes used to check availability
            //  of virtual machines in the backend address pool
            // Three inbound NAT rules which contain rules that map a public port on the load
            //  balancer to a port for a specific virtual machine in the backend address pool
            //  - this provides direct VM connectivity for SSH to port 22 and TELNET to port 23

            System.out.println("Creating a Internet facing load balancer with ...");
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

            LoadBalancer loadBalancer1 = azure.loadBalancers().define(loadBalancerName1)
                    .withRegion(region)
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

                    // Add nat pools to enable direct VM connectivity for
                    //  SSH to port 22 and TELNET to port 23
                    .defineInboundNatPool(natPool50XXto22)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPortRange(5000, 5099)
                        .toBackendPort(22)
                        .attach()
                    .defineInboundNatPool(natPool60XXto23)
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend(frontendName)
                        .fromFrontendPortRange(6000, 6099)
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
            // Create a virtual machine scale set with three virtual machines
            // And, install Apache Web servers on them

            System.out.println("Creating virtual machine scale set with three virtual machines"
                    + " in the frontend subnet ...");

            Date t1 = new Date();

            ImageReference imageReference = new ImageReference()
                    .withPublisher("Canonical")
                    .withOffer("UbuntuServer")
                    .withSku("18.04-LTS")
                    .withVersion("latest");

            VirtualMachineScaleSet virtualMachineScaleSet = azure.virtualMachineScaleSets().define(vmssName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
                    .withExistingPrimaryNetworkSubnet(network, "Front-end")
                    .withExistingPrimaryInternetFacingLoadBalancer(loadBalancer1)
                    .withPrimaryInternetFacingLoadBalancerBackends(backendPoolName1, backendPoolName2)
                    .withPrimaryInternetFacingLoadBalancerInboundNatPools(natPool50XXto22, natPool60XXto23)
                    .withoutPrimaryInternalLoadBalancer()
                    .withSpecificLinuxImageVersion(imageReference)
                    .withRootUsername(userName)
                    .withSsh(sshKey)
                    .withUnmanagedDisks()
                    .withNewStorageAccount(storageAccountName1)
                    .withNewStorageAccount(storageAccountName2)
                    .withNewStorageAccount(storageAccountName3)
                    .withCapacity(3)
                    // Use a VM extension to install Apache Web servers
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", fileUris)
                        .withPublicSetting("commandToExecute", installCommand)
                        .attach()
                    .create();

            Date t2 = new Date();
            System.out.println("Created a virtual machine scale set with "
                    + "3 Linux VMs & Apache Web servers on them: (took "
                    + ((t2.getTime() - t1.getTime()) / 1000) + " seconds) ");
            System.out.println();

            // Print virtual machine scale set details
            // Utils.print(virtualMachineScaleSet);

            //=============================================================
            // List virtual machine scale set network interfaces

            System.out.println("Listing scale set network interfaces ...");
            PagedList<VirtualMachineScaleSetNetworkInterface> vmssNics = virtualMachineScaleSet.listNetworkInterfaces();
            for (VirtualMachineScaleSetNetworkInterface vmssNic : vmssNics) {
                System.out.println(vmssNic.id());
            }

            //=============================================================
            // List virtual machine scale set instance network interfaces and SSH connection string

            System.out.println("Listing scale set virtual machine instance network interfaces and SSH connection string...");
            for (VirtualMachineScaleSetVM instance : virtualMachineScaleSet.virtualMachines().list()) {
                System.out.println("Scale set virtual machine instance #" + instance.instanceId());
                System.out.println(instance.id());
                PagedList<VirtualMachineScaleSetNetworkInterface> networkInterfaces = instance.listNetworkInterfaces();
                // Pick the first NIC
                VirtualMachineScaleSetNetworkInterface networkInterface = networkInterfaces.get(0);
                for (VirtualMachineScaleSetNicIPConfiguration ipConfig :networkInterface.ipConfigurations().values()) {
                    if (ipConfig.isPrimary()) {
                        List<LoadBalancerInboundNatRule> natRules = ipConfig.listAssociatedLoadBalancerInboundNatRules();
                        for (LoadBalancerInboundNatRule natRule : natRules) {
                            if (natRule.backendPort() == 22) {
                                System.out.println("SSH connection string: " + userName + "@" + publicIPAddress.fqdn() + ":" + natRule.frontendPort());
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            //=============================================================
            // Stop the virtual machine scale set

            System.out.println("Stopping virtual machine scale set ...");
            virtualMachineScaleSet.powerOff();
            System.out.println("Stopped virtual machine scale set");


            //=============================================================
            // Start the virtual machine scale set

            System.out.println("Starting virtual machine scale set ...");
            virtualMachineScaleSet.start();
            System.out.println("Started virtual machine scale set");


            //=============================================================
            // Update the virtual machine scale set
            // - double the no. of virtual machines

            System.out.println("Updating virtual machine scale set "
                    + "- double the no. of virtual machines ...");

            virtualMachineScaleSet.update()
                    .withCapacity(6)
                    .apply();

            System.out.println("Doubled the no. of virtual machines in "
                    + "the virtual machine scale set");


            //=============================================================
            // re-start virtual machine scale set

            System.out.println("re-starting virtual machine scale set ...");
            virtualMachineScaleSet.restart();
            System.out.println("re-started virtual machine scale set");

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
     * @param args parameters
     */

    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            System.out.println(System.getenv("AZURE_AUTH_LOCATION"));
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

    private ManageVirtualMachineScaleSetWithUnmanagedDisks() {

    }
}
