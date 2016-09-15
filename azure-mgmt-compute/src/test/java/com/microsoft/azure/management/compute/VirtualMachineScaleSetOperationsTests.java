package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachineScaleSetOperationsTests extends ComputeManagementTestBase {
    private static final String RG_NAME = ResourceNamer.randomResourceName("javacsmrg", 20);
    private static final String LOCATION = "eastasia";
    private static final String VMSCALESETNAME = ResourceNamer.randomResourceName("vmss", 10);

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        resourceManager.resourceGroups().delete(RG_NAME);
    }

    @Test
    public void canCreateVirtualMachineScaleSet() throws Exception {
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(LOCATION)
                .create();

        Network network = this.networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();

        LoadBalancer publicLoadBalancer = createInternetFacingLoadBalancer(resourceGroup, "1");
        List<String> backends = new ArrayList<>();
        for (String backend : publicLoadBalancer.backends().keySet()) {
            backends.add(backend);
        }
        Assert.assertTrue(backends.size() == 2);

        VirtualMachineScaleSet virtualMachineScaleSet = this.computeManager.virtualMachineScaleSets()
                .define(VMSCALESETNAME)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetwork(network)
                .withSubnet("subnet1")
                .withPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
                .withPrimaryInternetFacingLoadBalancerBackends(backends.get(0), backends.get(1))
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUserName("jvuser")
                .withPassword("123OData!@#123")
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 15))
                .withNewStorageAccount(ResourceNamer.randomResourceName("stg", 15))
                .create();

        Assert.assertNull(virtualMachineScaleSet.primaryInternalLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.primaryInternalLoadBalancerBackEnds().size() == 0);
        Assert.assertTrue(virtualMachineScaleSet.primaryInternalLoadBalancerInboundNatPools().size() == 0);

        Assert.assertNotNull(virtualMachineScaleSet.primaryInternetFacingLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.primaryInternetFacingLoadBalancerBackEnds().size() == 2);
        Assert.assertTrue(virtualMachineScaleSet.primaryInternetFacingLoadBalancerInboundNatPools().size() == 2);

        Assert.assertNotNull(virtualMachineScaleSet.primaryNetwork());

        Assert.assertEquals(virtualMachineScaleSet.vhdContainers().size(), 2);
        Assert.assertEquals(virtualMachineScaleSet.sku(), VirtualMachineScaleSetSkuTypes.STANDARD_A0);
        // Check defaults
        Assert.assertTrue(virtualMachineScaleSet.upgradeModel() == UpgradeMode.AUTOMATIC);
        Assert.assertEquals(virtualMachineScaleSet.capacity(), 2);
        // Fetch the primary Virtual network
        Network primaryNetwork = virtualMachineScaleSet.primaryNetwork();

        String inboundNatPoolToRemove = null;
        for (String inboundNatPoolName :
                virtualMachineScaleSet.primaryInternetFacingLoadBalancerInboundNatPools().keySet()) {
            inboundNatPoolToRemove = inboundNatPoolName;
            break;
        }

        LoadBalancer internalLoadBalancer = createInternalLoadBalancer(resourceGroup,
                primaryNetwork,
                "1");

        virtualMachineScaleSet
                .update()
                .withPrimaryInternalLoadBalancer(internalLoadBalancer)
                .withoutPrimaryInternalLoadBalancerNatPools(inboundNatPoolToRemove)
                .apply();

        virtualMachineScaleSet = this.computeManager
                .virtualMachineScaleSets()
                .getByGroup(RG_NAME, VMSCALESETNAME);

        Assert.assertNotNull(virtualMachineScaleSet.primaryInternetFacingLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.primaryInternetFacingLoadBalancerBackEnds().size() == 2);
        Assert.assertTrue(virtualMachineScaleSet.primaryInternetFacingLoadBalancerInboundNatPools().size() == 1);

        Assert.assertNotNull(virtualMachineScaleSet.primaryInternalLoadBalancer());
        Assert.assertTrue(virtualMachineScaleSet.primaryInternalLoadBalancerBackEnds().size() == 2);
        Assert.assertTrue(virtualMachineScaleSet.primaryInternalLoadBalancerInboundNatPools().size() == 2);
    }

    private LoadBalancer createInternetFacingLoadBalancer(ResourceGroup resourceGroup, String id) throws Exception {
        final String loadBalancerName = ResourceNamer.randomResourceName("extlb" + id + "-", 18);
        final String publicIpName = "pip-" + loadBalancerName;
        final String frontendName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";

        PublicIpAddress publicIpAddress = this.networkManager.publicIpAddresses()
                .define(publicIpName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .withLeafDomainLabel(publicIpName)
                .create();

        LoadBalancer loadBalancer = this.networkManager.loadBalancers()
                .define(loadBalancerName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .definePublicFrontend(frontendName)
                .withExistingPublicIpAddress(publicIpAddress)
                .attach()
                // Add two backend one per rule
                .defineBackend(backendPoolName1)
                .attach()
                .defineBackend(backendPoolName2)
                .attach()
                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                .withRequestPath("/")
                .attach()
                .defineHttpProbe("httpsProbe")
                .withRequestPath("/")
                .attach()
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPort(80)
                    .withProbe("httpProbe")
                    .withBackend(backendPoolName1)
                .attach()
                .defineLoadBalancingRule("httpsRule")
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPort(443)
                    .withProbe("httpsProbe")
                    .withBackend(backendPoolName2)
                .attach()
                // Add two nat pools to enable direct VM connectivity to port SSH and 23
                .defineInboundNatPool(natPoolName1)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPortRange(5000, 5099)
                    .withBackendPort(22)
                .attach()
                .defineInboundNatPool(natPoolName2)
                    .withProtocol(TransportProtocol.TCP)
                    .withFrontend(frontendName)
                    .withFrontendPortRange(6000, 6099)
                    .withBackendPort(23)
                .attach()
                .create();
        return loadBalancer;
    }

    private LoadBalancer createInternalLoadBalancer(ResourceGroup resourceGroup,
                                                    Network network, String id) throws Exception {
        final String loadBalancerName = ResourceNamer.randomResourceName("InternalLb" + id + "-", 18);
        final String privateFrontEndName = loadBalancerName + "-FE1";
        final String backendPoolName1 = loadBalancerName + "-BAP1";
        final String backendPoolName2 = loadBalancerName + "-BAP2";
        final String natPoolName1 = loadBalancerName + "-INP1";
        final String natPoolName2 = loadBalancerName + "-INP2";
        final String subnetName = "subnet1";

        LoadBalancer loadBalancer = this.networkManager.loadBalancers()
                .define(loadBalancerName)
                .withRegion(LOCATION)
                .withExistingResourceGroup(resourceGroup)
                .definePrivateFrontend(privateFrontEndName)
                    .withExistingSubnet(network, subnetName)
                .attach()
                // Add two backend one per rule
                .defineBackend(backendPoolName1)
                .attach()
                .defineBackend(backendPoolName2)
                .attach()
                // Add two probes one per rule
                .defineHttpProbe("httpProbe")
                .withRequestPath("/")
                .attach()
                .defineHttpProbe("httpsProbe")
                .withRequestPath("/")
                .attach()
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule("httpRule")
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPort(1000)
                .withProbe("httpProbe")
                .withBackend(backendPoolName1)
                .attach()
                .defineLoadBalancingRule("httpsRule")
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPort(1001)
                .withProbe("httpsProbe")
                .withBackend(backendPoolName2)
                .attach()
                // Add two nat pools to enable direct VM connectivity to port 44 and 45
                .defineInboundNatPool(natPoolName1)
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPortRange(8000, 8099)
                .withBackendPort(44)
                .attach()
                .defineInboundNatPool(natPoolName2)
                .withProtocol(TransportProtocol.TCP)
                .withFrontend(privateFrontEndName)
                .withFrontendPortRange(9000, 9099)
                .withBackendPort(45)
                .attach()
                .create();
        return loadBalancer;
    }

}
