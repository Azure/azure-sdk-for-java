// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Azure Compute sample for managing virtual machine scale set -
 *  - Create a zone resilient public ip address
 *  - Create a zone resilient load balancer with
 *         - the existing zone resilient ip address
 *         - two load balancing rule which is applied to two different backend pools
 *  - Create two zone aware virtual machine scale set each associated with one backend pool.
 */
public final class ManageZonalVirtualMachineScaleSet {
    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_EAST2;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String loadBalancerName = Utils.randomResourceName(azureResourceManager, "extlb", 15);
        final String publicIPName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";
        final String vmssName1 = Utils.randomResourceName(azureResourceManager, "vmss1", 15);
        final String vmssName2 = Utils.randomResourceName(azureResourceManager, "vmss2", 15);

        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();

        try {
            ResourceGroup resourceGroup = azureResourceManager.resourceGroups()
                    .define(rgName)
                    .withRegion(region)
                    .create();

            //=============================================================
            // Create a zone resilient PublicIP address

            System.out.println("Creating a zone resilient public ip address");

            PublicIpAddress publicIPAddress = azureResourceManager.publicIpAddresses()
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

            LoadBalancer loadBalancer = azureResourceManager.loadBalancers()
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
                    .withExistingPublicIpAddress(publicIPAddress)   // Frontend with PIP means internet-facing load-balancer
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

            Network network = azureResourceManager
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
            // Create a zone aware virtual machine scale set

            System.out.println("Creating a zone aware virtual machine scale set");

            // HTTP goes to this virtual machine scale set
            //
            VirtualMachineScaleSet virtualMachineScaleSet1 = azureResourceManager.virtualMachineScaleSets()
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
                    .withSsh(sshPublicKey)
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .create();

            System.out.println("Created zone aware virtual machine scale set: " + virtualMachineScaleSet1.id());

            //=============================================================
            // Create a zone aware virtual machine scale set

            System.out.println("Creating second zone aware virtual machine scale set");

            // HTTPS goes to this virtual machine scale set
            //
            VirtualMachineScaleSet virtualMachineScaleSet2 = azureResourceManager.virtualMachineScaleSets()
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
                    .withSsh(sshPublicKey)
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .create();

            System.out.println("Created zone aware virtual machine scale set: " + virtualMachineScaleSet2.id());

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

    private ManageZonalVirtualMachineScaleSet() {

    }
}
