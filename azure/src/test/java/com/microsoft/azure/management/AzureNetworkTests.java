/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.Access;
import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewayOperationalState;
import com.microsoft.azure.management.network.Direction;
import com.microsoft.azure.management.network.FlowLogSettings;
import com.microsoft.azure.management.network.NetworkWatcher;
import com.microsoft.azure.management.network.NextHop;
import com.microsoft.azure.management.network.NextHopType;
import com.microsoft.azure.management.network.PacketCapture;
import com.microsoft.azure.management.network.PcProtocol;
import com.microsoft.azure.management.network.Protocol;
import com.microsoft.azure.management.network.SecurityGroupView;
import com.microsoft.azure.management.network.Topology;
import com.microsoft.azure.management.network.VerificationIPFlow;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AzureNetworkTests extends TestBase {
    private Azure azure;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        Azure.Authenticated azureAuthed = Azure.authenticate(restClient, defaultSubscription, domain);
        azure = azureAuthed.withSubscription(defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {

    }

    /**
     * Tests the network security group implementation.
     * @throws Exception
     */
    @Test
    public void testNetworkSecurityGroups() throws Exception {
        new TestNSG().runTest(azure.networkSecurityGroups(), azure.resourceGroups());
    }

    /**
     * Tests the inbound NAT rule support in load balancers.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatRules() throws Exception {
        new TestLoadBalancer.InternetWithNatRule(
            azure.virtualMachines(),
            azure.availabilitySets())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the inbound NAT pool support in load balancers.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatPools() throws Exception {
        new TestLoadBalancer.InternetWithNatPool(
            azure.virtualMachines(),
            azure.availabilitySets())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum Internet-facing load balancer with a load balancing rule only
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternetMinimum() throws Exception {
        new TestLoadBalancer.InternetMinimal(
            azure.virtualMachines(),
            azure.availabilitySets())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum Internet-facing load balancer with a NAT rule only
     * @throws Exception
     */
    @Test
    public void testLoadBalancersNatOnly() throws Exception {
        new TestLoadBalancer.InternetNatOnly(azure.virtualMachines().manager())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests the minimum internal load balancer.
     * @throws Exception
     */
    @Test
    public void testLoadBalancersInternalMinimum() throws Exception {
        new TestLoadBalancer.InternalMinimal(
            azure.virtualMachines(),
            azure.availabilitySets())
            .runTest(azure.loadBalancers(), azure.resourceGroups());
    }

    /**
     * Tests a complex internal application gateway
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternalComplex() throws Exception {
        new TestApplicationGateway.PrivateComplex()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests a minimal internal application gateway
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternalMinimal() throws Exception {
        new TestApplicationGateway.PrivateMinimal()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    @Test
    public void testApplicationGatewaysInParallel() throws Exception {
        String rgName = SdkContext.randomResourceName("rg", 13);
        Region region = Region.US_EAST;
        Creatable<ResourceGroup> resourceGroup = azure.resourceGroups().define(rgName)
            .withRegion(region);
        List<Creatable<ApplicationGateway>> agCreatables = new ArrayList<>();

        agCreatables.add(azure.applicationGateways().define(SdkContext.randomResourceName("ag", 13))
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(resourceGroup)
            .defineRequestRoutingRule("rule1")
            .fromPrivateFrontend()
            .fromFrontendHttpPort(80)
            .toBackendHttpPort(8080)
            .toBackendIPAddress("10.0.0.1")
            .toBackendIPAddress("10.0.0.2")
            .attach());

        agCreatables.add(azure.applicationGateways().define(SdkContext.randomResourceName("ag", 13))
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(resourceGroup)
            .defineRequestRoutingRule("rule1")
            .fromPrivateFrontend()
            .fromFrontendHttpPort(80)
            .toBackendHttpPort(8080)
            .toBackendIPAddress("10.0.0.3")
            .toBackendIPAddress("10.0.0.4")
            .attach());

        CreatedResources<ApplicationGateway> created = azure.applicationGateways().create(agCreatables);
        List<ApplicationGateway> ags = new ArrayList<>();
        List<String> agIds = new ArrayList<>();
        for (Creatable<ApplicationGateway> creatable : agCreatables) {
            ApplicationGateway ag = created.get(creatable.key());
            Assert.assertNotNull(ag);
            ags.add(ag);
            agIds.add(ag.id());
        }

        azure.applicationGateways().stop(agIds);

        for (ApplicationGateway ag : ags) {
            Assert.assertEquals(ApplicationGatewayOperationalState.STOPPED, ag.refresh().operationalState());
        }

        azure.applicationGateways().start(agIds);

        for (ApplicationGateway ag : ags) {
            Assert.assertEquals(ApplicationGatewayOperationalState.RUNNING, ag.refresh().operationalState());
        }

        azure.applicationGateways().deleteByIds(agIds);
        for (String id : agIds) {
            Assert.assertNull(azure.applicationGateways().getById(id));
        }

        azure.resourceGroups().beginDeleteByName(rgName);
    }

    /**
     * Tests a minimal Internet-facing application gateway.
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingMinimal() throws Exception {
        new TestApplicationGateway.PublicMinimal()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests a complex Internet-facing application gateway.
     * @throws Exception
     */
    @Test
    public void testAppGatewaysInternetFacingComplex() throws Exception {
        new TestApplicationGateway.PublicComplex()
            .runTest(azure.applicationGateways(),  azure.resourceGroups());
    }

    /**
     * Tests the public IP address implementation.
     * @throws Exception
     */
    @Test
    public void testPublicIPAddresses() throws Exception {
        new TestPublicIPAddress().runTest(azure.publicIPAddresses(), azure.resourceGroups());
    }

    /**
     * Tests the availability set implementation.
     * @throws Exception
     */
    @Test
    public void testAvailabilitySets() throws Exception {
        new TestAvailabilitySet().runTest(azure.availabilitySets(), azure.resourceGroups());
    }

    /**
     * Tests the virtual network implementation.
     * @throws Exception
     */
    @Test
    public void testNetworks() throws Exception {
        new TestNetwork.WithSubnets()
            .runTest(azure.networks(), azure.resourceGroups());
    }

    /**
     * Tests route tables.
     * @throws Exception
     */
    @Test
    public void testRouteTables() throws Exception {
        new TestRouteTables.Minimal()
            .runTest(azure.routeTables(), azure.resourceGroups());
    }

    /**
     * Tests the network interface implementation.
     * @throws Exception
     */
    @Test
    public void testNetworkInterfaces() throws Exception {
        new TestNetworkInterface().runTest(azure.networkInterfaces(), azure.resourceGroups());
    }

    /**
     * Tests the network watcher implementation.
     * @throws Exception
     */
    @Test
    public void testNetworkWatchers() throws Exception {
        new TestNetworkWatcher().runTest(azure.networkWatchers(), azure.resourceGroups());
    }

    /**
     * Tests the network watcher functions.
     * @throws Exception
     */
    @Test
    public void testNetworkWatcherFunctions() throws Exception {
        TestNetworkWatcher tnw = new TestNetworkWatcher();

        NetworkWatcher nw = tnw.createResource(azure.networkWatchers());

        // pre-create VMs to show topology on
        VirtualMachine[] virtualMachines = tnw.ensureNetwork(azure.networkWatchers().manager().networks(),
            azure.virtualMachines(), azure.networkInterfaces());

        Topology topology = nw.getTopology(virtualMachines[0].resourceGroupName());
        Assert.assertEquals(11, topology.resources().size());
        Assert.assertTrue(topology.resources().containsKey(virtualMachines[0].getPrimaryNetworkInterface().networkSecurityGroupId()));
        Assert.assertEquals(4, topology.resources().get(virtualMachines[0].primaryNetworkInterfaceId()).associations().size());

        SecurityGroupView sgViewResult = nw.getSecurityGroupView(virtualMachines[0].id());
        Assert.assertEquals(1, sgViewResult.networkInterfaces().size());
        Assert.assertEquals(virtualMachines[0].primaryNetworkInterfaceId(), sgViewResult.networkInterfaces().keySet().iterator().next());

        FlowLogSettings flowLogSettings = nw.getFlowLogSettings(virtualMachines[0].getPrimaryNetworkInterface().networkSecurityGroupId());
        StorageAccount storageAccount = tnw.ensureStorageAccount(azure.storageAccounts());
        flowLogSettings.update()
            .withLogging()
            .withStorageAccount(storageAccount.id())
            .withRetentionPolicyDays(5)
            .withRetentionPolicyEnabled()
            .apply();
        Assert.assertEquals(true, flowLogSettings.enabled());
        Assert.assertEquals(5, flowLogSettings.retentionDays());
        Assert.assertEquals(storageAccount.id(), flowLogSettings.storageId());

//        Troubleshooting troubleshooting = nw.troubleshoot(<virtual_network_gateway_id> or <virtual_network_gateway_connaction_id>,
//                storageAccount.id(), "");
        NextHop nextHop = nw.nextHop().withTargetResourceId(virtualMachines[0].id())
            .withSourceIPAddress("10.0.0.4")
            .withDestinationIPAddress("8.8.8.8")
            .execute();
        Assert.assertEquals("System Route", nextHop.routeTableId());
        Assert.assertEquals(NextHopType.INTERNET, nextHop.nextHopType());
        Assert.assertNull(nextHop.nextHopIpAddress());

        VerificationIPFlow verificationIPFlow = nw.verifyIPFlow()
            .withTargetResourceId(virtualMachines[0].id())
            .withDirection(Direction.OUTBOUND)
            .withProtocol(Protocol.TCP)
            .withLocalIPAddress("10.0.0.4")
            .withRemoteIPAddress("8.8.8.8")
            .withLocalPort("443")
            .withRemotePort("443")
            .execute();
        Assert.assertEquals(Access.ALLOW, verificationIPFlow.access());
        Assert.assertEquals("defaultSecurityRules/AllowInternetOutBound", verificationIPFlow.ruleName());

        // test packet capture
        List<PacketCapture> packetCaptures = nw.packetCaptures().list();
        Assert.assertEquals(0, packetCaptures.size());
        PacketCapture packetCapture = nw.packetCaptures()
            .define("NewPacketCapture")
            .withTarget(virtualMachines[0].id())
            .withExistingStorageAccount(storageAccount)
            .withTimeLimitInSeconds(1500)
            .definePacketCaptureFilter()
                .withProtocol(PcProtocol.TCP)
                .withLocalIPAddresses(Arrays.asList("127.0.0.1", "127.0.0.5"))
                .attach()
            .create();
        packetCaptures = nw.packetCaptures().list();
        Assert.assertEquals(1, packetCaptures.size());
        Assert.assertEquals("NewPacketCapture", packetCapture.name());
        Assert.assertEquals(1500, packetCapture.timeLimitInSeconds());
        Assert.assertEquals(PcProtocol.TCP, packetCapture.filters().get(0).protocol());
        Assert.assertEquals("127.0.0.1;127.0.0.5", packetCapture.filters().get(0).localIPAddress());
//        Assert.assertEquals("Running", packetCapture.getStatus().packetCaptureStatus().toString());
        packetCapture.stop();
        Assert.assertEquals("Stopped", packetCapture.getStatus().packetCaptureStatus().toString());
        nw.packetCaptures().deleteByName(packetCapture.name());

        azure.virtualMachines().deleteById(virtualMachines[1].id());
        topology.refresh();
        Assert.assertEquals(10, topology.resources().size());

        azure.resourceGroups().deleteByName(nw.resourceGroupName());
        azure.resourceGroups().deleteByName(tnw.groupName());
    }

}
