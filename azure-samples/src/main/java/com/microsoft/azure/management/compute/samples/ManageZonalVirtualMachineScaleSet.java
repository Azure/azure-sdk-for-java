/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuTypes;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerSkuType;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPSkuType;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machine scale set -
 *  - Create a zone resilient public ip address
 *  - Create a zone resilient load balancer with
 *         - the existing zone resilient ip address
 *         - two load balancing rule which is applied to two different backend pools
 *  - Create two zone redundant virtual machine scale set each associated with one backend pool
 *  - Update the virtual machine scale set by appending new zone.
 */
public final class ManageZoneRedundantVirtualMachineScaleSet {
    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_EAST2;
        final String rgName = Utils.createRandomName("rgCOMV");
        final String loadBalancerName = Utils.createRandomName("extlb");
        final String publicIPName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";
        final String vmssName1 = Utils.createRandomName("vmss1");
        final String vmssName2 = Utils.createRandomName("vmss2");

        final String userName = "tirekicker";
        final String password = "12NewPA23w0rd!";

        try {
            ResourceGroup resourceGroup = azure.resourceGroups()
                    .define(rgName)
                    .withRegion(region)
                    .create();

            //=============================================================
            // Create a zone resilient PublicIP address

            System.out.println("Creating a zone resilient public ip address");

            PublicIPAddress publicIPAddress = azure.publicIPAddresses()
                    .define(publicIPName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withLeafDomainLabel(publicIPName)
                    // Optionals
                    .withStaticIP()
                    .withSku(PublicIPSkuType.STANDARD)
                    // Create PublicIP
                    .create();

            System.out.println("Created a zone resilient public ip address: " + publicIPAddress.id());
            Utils.print(publicIPAddress);

            //=============================================================
            // Create a zone resilient load balancer

            System.out.println("Creating a zone resilient load balancer");

            LoadBalancer loadBalancer = azure.loadBalancers()
                    .define(loadBalancerName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)

                    // Add two rules that uses above backend and probe
                    .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(80)
                    .toBackend(backendPoolName1)
                    .withProbe("httpProbe")
                    .attach()
                    .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(443)
                    .toBackend(backendPoolName2)
                    .withProbe("httpsProbe")
                    .attach()
                    // Add two nat pools to enable direct VMSS connectivity to port SSH and 23
                    .defineInboundNatPool(natPoolName1)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPortRange(5000, 5099)
                    .toBackendPort(22)
                    .attach()
                    .defineInboundNatPool(natPoolName2)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPortRange(6000, 6099)
                    .toBackendPort(23)
                    .attach()
                    // Explicitly define the frontend
                    .definePublicFrontend(frontendName)
                    .withExistingPublicIPAddress(publicIPAddress)   // Frontend with PIP means internet-facing load-balancer
                    .attach()
                    // Add two probes one per rule
                    .defineHttpProbe("httpProbe")
                    .withRequestPath("/")
                    .attach()
                    .defineHttpProbe("httpsProbe")
                    .withRequestPath("/")
                    .attach()
                    .withSku(LoadBalancerSkuType.STANDARD)
                    .create();

            System.out.println("Created a zone resilient load balancer: " + publicIPAddress.id());
            Utils.print(loadBalancer);

            List<String> backends = new ArrayList<>();
            for (String backend : loadBalancer.backends().keySet()) {
                backends.add(backend);
            }
            List<String> natpools = new ArrayList<>();
            for (String natPool : loadBalancer.inboundNatPools().keySet()) {
                natpools.add(natPool);
            }

            System.out.println("Creating network for virtual machine scale sets");

            Network network = azure
                    .networks()
                    .define("vmssvnet")
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/28")
                    .create();

            System.out.println("Created network for virtual machine scale sets");
            Utils.print(network);

            //=============================================================
            // Create a zone redundant virtual machine scale set

            System.out.println("Creating a zone redundant virtual machine scale set");

            // HTTP goes to this virtual machine scale set
            //
            VirtualMachineScaleSet virtualMachineScaleSet1 = azure.virtualMachineScaleSets()
                    .define(vmssName1)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
                    .withExistingPrimaryNetworkSubnet(network, "subnet1")
                    .withExistingPrimaryInternetFacingLoadBalancer(loadBalancer)
                    .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0))
                    .withPrimaryInternetFacingLoadBalancerInboundNatPools(natpools.get(0))
                    .withoutPrimaryInternalLoadBalancer()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_2)
                    .create();

            System.out.println("Created zone redundant virtual machine scale set");

            //=============================================================
            // Create a zone redundant virtual machine scale set

            System.out.println("Creating second zone redundant virtual machine scale set");

            // HTTPS goes to this virtual machine scale set
            //
            VirtualMachineScaleSet virtualMachineScaleSet2 = azure.virtualMachineScaleSets()
                    .define(vmssName2)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D3_V2)
                    .withExistingPrimaryNetworkSubnet(network, "subnet1")
                    .withExistingPrimaryInternetFacingLoadBalancer(loadBalancer)
                    .withPrimaryInternetFacingLoadBalancerBackends(backends.get(1))
                    .withPrimaryInternetFacingLoadBalancerInboundNatPools(natpools.get(1))
                    .withoutPrimaryInternalLoadBalancer()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword(password)
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_2)
                    .create();

            // Note: updating zone for VMSS will be supported as part of GA, this functionality is not yet deployed.

            // System.out.println("Created second zone redundant virtual machine scale set");

            //=============================================================
            // Update a zone redundant virtual machine scale set with new zone

            // System.out.println("Updating zone redundant virtual machine scale set to have additional zone");

            // virtualMachineScaleSet1.update()
            //        .withAvailabilityZone(AvailabilityZoneId.ZONE_3)
            //        .apply();

            // System.out.println("Updated zone redundant virtual machine scale set");

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

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BODY_AND_HEADERS)
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

    private ManageZoneRedundantVirtualMachineScaleSet() {

    }
}
