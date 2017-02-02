/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.microsoft.azure.management.compute.AvailabilitySetSkuTypes;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import org.junit.Assert;

import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerFrontend;
import com.microsoft.azure.management.network.LoadBalancerHttpProbe;
import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.LoadBalancerPublicFrontend;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.LoadDistribution;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.ProbeProtocol;
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerProbe;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Test of virtual network management.
 */
public class TestLoadBalancer {
    static String TEST_ID = "";
    static Region REGION = Region.US_NORTH_CENTRAL;
    static String GROUP_NAME = "";
    static String LB_NAME = "";
    static String[] PIP_NAMES = null;

    private static void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        GROUP_NAME = "rg" + TEST_ID;
        LB_NAME = "lb" + TEST_ID;
        PIP_NAMES = new String[]{"pipa" + TEST_ID, "pipb" + TEST_ID};
    }
    /**
     * Internet-facing LB test with NAT pool test.
     */
    public static class InternetWithNatPool extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final PublicIpAddresses pips;
        private final VirtualMachines vms;
        private final Networks networks;
        private final AvailabilitySets availabilitySets;

        /**
         * Test of a load balancer with a NAT pool.
         * @param pips public IPs
         * @param vms virtual machines
         * @param networks virtual networks
         * @param availabilitySets availability sets
         */
        public InternetWithNatPool(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks,
                AvailabilitySets availabilitySets) {
            initializeResourceNames();
            this.pips = pips;
            this.vms = vms;
            this.networks = networks;
            this.availabilitySets = availabilitySets;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, this.availabilitySets, 2);
            ensurePIPs(pips);
            PublicIpAddress pip0 = pips.getByGroup(GROUP_NAME, PIP_NAMES[0]);
            PublicIpAddress pip1 = pips.getByGroup(GROUP_NAME, PIP_NAMES[1]);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)

                    // Frontends
                    .withExistingPublicIpAddress(pip0)
                    .definePublicFrontend("frontend1")
                        .withExistingPublicIpAddress(pip1)
                        .attach()

                    // Backends
                    .withExistingVirtualMachines(existingVMs)
                    .defineBackend("backend1")
                        .attach()

                    // Probes
                    .defineTcpProbe("tcpProbe1")
                        .withPort(25)               // Required
                        .withIntervalInSeconds(15)  // Optionals
                        .withNumberOfProbes(5)
                        .attach()
                    .defineHttpProbe("httpProbe1")
                        .withRequestPath("/")       // Required
                        .withIntervalInSeconds(13)  // Optionals
                        .withNumberOfProbes(4)
                        .attach()

                    // Load balancing rules
                    .defineLoadBalancingRule("rule1")
                        .withProtocol(TransportProtocol.TCP)    // Required
                        .withFrontend("frontend1")
                        .withFrontendPort(81)
                        .withProbe("tcpProbe1")
                        .withBackend("backend1")
                        .withBackendPort(82)                    // Optionals
                        .withIdleTimeoutInMinutes(10)
                        .withLoadDistribution(LoadDistribution.SOURCE_IP)
                        .attach()

                    // Inbound NAT pools
                    .defineInboundNatPool("natpool1")
                        .withProtocol(TransportProtocol.TCP)
                        .withFrontend("frontend1")
                        .withFrontendPortRange(2000, 2001)
                        .withBackendPort(8080)
                        .attach()

                    .create();

            // Verify frontends
            Assert.assertTrue(lb.frontends().size() == 2);

            LoadBalancerFrontend frontend = lb.frontends().get("frontend1");
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip1.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            frontend = lb.frontends().get("default");
            Assert.assertTrue(frontend.isPublic());
            publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip0.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            // Verify backends
            Assert.assertTrue(lb.backends().containsKey("default"));
            Assert.assertTrue(lb.backends().containsKey("backend1"));
            Assert.assertTrue(lb.backends().size() == 2);

            // Verify probes
            Assert.assertTrue(lb.httpProbes().containsKey("httpProbe1"));
            Assert.assertTrue(lb.tcpProbes().containsKey("tcpProbe1"));
            Assert.assertTrue(!lb.httpProbes().containsKey("default"));
            Assert.assertTrue(!lb.tcpProbes().containsKey("default"));

            // Verify rules
            Assert.assertTrue(lb.loadBalancingRules().containsKey("rule1"));
            Assert.assertTrue(!lb.loadBalancingRules().containsKey("default"));
            Assert.assertTrue(lb.loadBalancingRules().values().size() == 1);
            LoadBalancingRule rule = lb.loadBalancingRules().get("rule1");
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));
            Assert.assertTrue(rule.frontend().name().equalsIgnoreCase("frontend1"));
            Assert.assertTrue(rule.probe().name().equalsIgnoreCase("tcpProbe1"));

            // Verify inbound NAT pools
            Assert.assertTrue(lb.inboundNatPools().containsKey("natpool1"));
            Assert.assertTrue(lb.inboundNatPools().size() == 1);
            LoadBalancerInboundNatPool inboundNatPool = lb.inboundNatPools().get("natpool1");
            Assert.assertTrue(inboundNatPool.frontend().name().equalsIgnoreCase("frontend1"));
            Assert.assertTrue(inboundNatPool.frontendPortRangeStart() == 2000);
            Assert.assertTrue(inboundNatPool.frontendPortRangeEnd() == 2001);
            Assert.assertTrue(inboundNatPool.backendPort() == 8080);

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            resource =  resource.update()
                    .withoutFrontend("default")
                    .withoutBackend("default")
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatPool("natpool1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assert.assertFalse(resource.frontends().containsKey("default"));

            // Verify backends
            Assert.assertFalse(resource.backends().containsKey("default"));

            // Verify rules
            Assert.assertFalse(resource.loadBalancingRules().containsKey("rule1"));

            // Verify NAT pools
            Assert.assertFalse(resource.inboundNatPools().containsKey("natpool1"));

            return resource;
        }
    }

    /**
     * Internet-facing LB test with NAT rules.
     */
    public static class InternetWithNatRule extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final PublicIpAddresses pips;
        private final VirtualMachines vms;
        private final Networks networks;
        private final AvailabilitySets availabilitySets;

        /**
         * Tests an Internet-facing load balancer with NAT rules.
         * @param pips public IP addresses
         * @param vms virtual machines
         * @param networks virtual networks
         * @param availabilitySets availability sets
         */
        public InternetWithNatRule(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks,
                AvailabilitySets availabilitySets) {
            initializeResourceNames();
            this.pips = pips;
            this.vms = vms;
            this.networks = networks;
            this.availabilitySets = availabilitySets;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, this.availabilitySets, 2);
            ensurePIPs(pips);
            PublicIpAddress pip = pips.getByGroup(GROUP_NAME, PIP_NAMES[0]);
            NetworkInterface nic1 = existingVMs[0].getPrimaryNetworkInterface();
            NetworkInterface nic2 = existingVMs[1].getPrimaryNetworkInterface();

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)

                    // Frontends
                    .definePublicFrontend("frontend1")
                        .withExistingPublicIpAddress(pip)
                        .attach()

                    // Backends
                    .defineBackend("backend1")
                        .attach()

                    // Probes
                    .defineTcpProbe("tcpProbe1")
                        .withPort(25)               // Required
                        .withIntervalInSeconds(15)  // Optionals
                        .withNumberOfProbes(5)
                        .attach()
                    .defineHttpProbe("httpProbe1")
                        .withRequestPath("/")       // Required
                        .withIntervalInSeconds(13)  // Optionals
                        .withNumberOfProbes(4)
                        .attach()

                    // Load balancing rules
                    .defineLoadBalancingRule("rule1")
                        .withProtocol(TransportProtocol.TCP)    // Required
                        .withFrontend("frontend1")
                        .withFrontendPort(81)
                        .withProbe("tcpProbe1")
                        .withBackend("backend1")
                        .withBackendPort(82)                    // Optionals
                        .withIdleTimeoutInMinutes(10)
                        .withLoadDistribution(LoadDistribution.SOURCE_IP)
                        .attach()

                    // Inbound NAT rules
                    .defineInboundNatRule("natrule1")
                        .withProtocol(TransportProtocol.TCP)
                        .withFrontend("frontend1")
                        .withFrontendPort(88)
                        .attach()
                    .create();

            // Connect NICs explicitly
            nic1.update()
                .withExistingLoadBalancerBackend(lb, "backend1")
                .withExistingLoadBalancerInboundNatRule(lb,  "natrule1")
                .apply();
            TestNetworkInterface.printNic(nic1);
            Assert.assertTrue(nic1.primaryIpConfiguration().listAssociatedLoadBalancerBackends().get(0).name()
                    .equalsIgnoreCase("backend1"));
            Assert.assertTrue(nic1.primaryIpConfiguration().listAssociatedLoadBalancerInboundNatRules().get(0).name()
                    .equalsIgnoreCase("natrule1"));

            nic2.update()
                .withExistingLoadBalancerBackend(lb, "backend1")
                .apply();
            TestNetworkInterface.printNic(nic2);
            Assert.assertTrue(nic2.primaryIpConfiguration().listAssociatedLoadBalancerBackends().get(0).name()
                    .equalsIgnoreCase("backend1"));

            // Verify frontends
            Assert.assertTrue(lb.frontends().size() == 1);
            LoadBalancerFrontend frontend = lb.frontends().get("frontend1");
            Assert.assertNotNull(frontend);
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            pip.refresh();
            Assert.assertTrue(pip.getAssignedLoadBalancerFrontend().name().equalsIgnoreCase("frontend1"));
            TestPublicIpAddress.printPIP(pip.refresh());

            // Verify backends
            Assert.assertTrue(lb.backends().containsKey("backend1"));
            Assert.assertTrue(lb.backends().size() == 1);

            // Verify probes
            Assert.assertTrue(lb.httpProbes().containsKey("httpProbe1"));
            Assert.assertTrue(lb.tcpProbes().containsKey("tcpProbe1"));
            Assert.assertTrue(!lb.httpProbes().containsKey("default"));
            Assert.assertTrue(!lb.tcpProbes().containsKey("default"));

            // Verify rules
            Assert.assertTrue(lb.loadBalancingRules().containsKey("rule1"));
            Assert.assertTrue(!lb.loadBalancingRules().containsKey("default"));
            Assert.assertTrue(lb.loadBalancingRules().values().size() == 1);
            LoadBalancingRule rule = lb.loadBalancingRules().get("rule1");
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase("backend1"));
            Assert.assertTrue(rule.frontend().name().equalsIgnoreCase("frontend1"));
            Assert.assertTrue(rule.probe().name().equalsIgnoreCase("tcpProbe1"));

            // Verify inbound NAT rules
            Assert.assertTrue(lb.inboundNatRules().containsKey("natrule1"));
            Assert.assertTrue(lb.inboundNatRules().size() == 1);
            LoadBalancerInboundNatRule inboundNatRule = lb.inboundNatRules().get("natrule1");
            Assert.assertTrue(inboundNatRule.frontend().name().equalsIgnoreCase("frontend1"));
            Assert.assertTrue(inboundNatRule.frontendPort() == 88);
            Assert.assertTrue(inboundNatRule.backendPort() == 88);

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            List<NetworkInterface> nics = new ArrayList<>();
            for (String nicId : resource.backends().get("backend1").backendNicIpConfigurationNames().keySet()) {
                nics.add(this.networks.manager().networkInterfaces().getById(nicId));
            }
            NetworkInterface nic1 = nics.get(0);
            NetworkInterface nic2 = nics.get(1);

            // Remove the NIC associations
            nic1.update()
                .withoutLoadBalancerBackends()
                .withoutLoadBalancerInboundNatRules()
                .apply();
            Assert.assertTrue(nic1.primaryIpConfiguration().listAssociatedLoadBalancerBackends().size() == 0);

            nic2.update()
                .withoutLoadBalancerBackends()
                .withoutLoadBalancerInboundNatRules()
                .apply();
            Assert.assertTrue(nic2.primaryIpConfiguration().listAssociatedLoadBalancerBackends().size() == 0);

            // Update the load balancer
            ensurePIPs(pips);
            PublicIpAddress pip = pips.getByGroup(GROUP_NAME, PIP_NAMES[1]);
            resource =  resource.update()
                    .updateInternetFrontend("frontend1")
                        .withExistingPublicIpAddress(pip)
                        .parent()
                    .withoutFrontend("default")
                    .withoutBackend("default")
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatRule("natrule1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertEquals(0, resource.inboundNatRules().size());

            // Verify frontends
            LoadBalancerFrontend frontend = resource.frontends().get("frontend1");
            Assert.assertNotNull(frontend);
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            return resource;
        }
    }

    /**
     * Internet-facing minimalistic LB test.
     */
    public static class InternetMinimal extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final PublicIpAddresses pips;
        private final VirtualMachines vms;
        private final Networks networks;
        private final AvailabilitySets availabilitySets;

        /**
         * Tests an Internet-facing load balancer with minimum inputs.
         * @param pips public IP addresses
         * @param vms virtual machines
         * @param networks virtual networks
         * @param availabilitySets availability sets
         */
        public InternetMinimal(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks,
                AvailabilitySets availabilitySets) {
            initializeResourceNames();
            this.pips = pips;
            this.vms = vms;
            this.networks = networks;
            this.availabilitySets = availabilitySets;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, this.availabilitySets, 2);
            ensurePIPs(pips);
            PublicIpAddress pip = pips.getByGroup(GROUP_NAME, PIP_NAMES[0]);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // Frontend (default)
                    .withExistingPublicIpAddress(pip)
                    // Backend (default)
                    .withExistingVirtualMachines(existingVMs)
                    // Probe (default)
                    .withTcpProbe(22)
                    // LB rule (default)
                    .withLoadBalancingRule(80, TransportProtocol.TCP)
                    .create();

            // Verify frontends
            Assert.assertTrue(lb.frontends().containsKey("default"));
            LoadBalancerFrontend frontend = lb.frontends().get("default");
            Assert.assertEquals(1, frontend.loadBalancingRules().size());
            Assert.assertTrue("default".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            // Verify TCP probes
            Assert.assertTrue(lb.tcpProbes().containsKey("default"));
            Assert.assertEquals(1, lb.tcpProbes().size());
            LoadBalancerTcpProbe tcpProbe = lb.tcpProbes().get("default");
            Assert.assertTrue(tcpProbe.loadBalancingRules().containsKey("default"));
            Assert.assertEquals(1, tcpProbe.loadBalancingRules().size());
            Assert.assertEquals(22, tcpProbe.port());
            Assert.assertEquals(ProbeProtocol.TCP, tcpProbe.protocol());

            // Verify rules
            Assert.assertEquals(1, lb.loadBalancingRules().size());
            Assert.assertTrue(lb.loadBalancingRules().containsKey("default"));
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("default");
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.frontend().name()));
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.probe().name()));
            Assert.assertEquals(80, lbrule.backendPort());
            Assert.assertNotNull(lbrule.frontend());
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.frontend().name()));
            Assert.assertEquals(80, lbrule.frontendPort());
            Assert.assertNotNull(lbrule.probe());
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.probe().name()));
            Assert.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assert.assertNotNull(lbrule.backend());
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.backend().name()));

            // Verify backends
            Assert.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().get("default");
            Assert.assertNotNull(backend);
            Assert.assertEquals(2, backend.backendNicIpConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assert.assertTrue(backend.backendNicIpConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            ensurePIPs(pips);
            PublicIpAddress pip = pips.getByGroup(GROUP_NAME, PIP_NAMES[1]);
            resource =  resource.update()
                    .withExistingPublicIpAddress(pip)
                    .updateTcpProbe("default")
                        .withPort(22)
                        .parent()
                    .defineHttpProbe("httpprobe")
                        .withRequestPath("/foo")
                        .withNumberOfProbes(3)
                        .withPort(443)
                        .attach()
                    .updateLoadBalancingRule("default")
                        .withBackendPort(8080)
                        .withIdleTimeoutInMinutes(11)
                        .parent()
                    .defineLoadBalancingRule("lbrule2")
                        .withProtocol(TransportProtocol.UDP)
                        .withFrontend("default")
                        .withFrontendPort(22)
                        .withProbe("httpprobe")
                        .withBackend("backend2")
                        .attach()
                    .defineBackend("backend2")
                        .attach()
                    .withoutBackend("default")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assert.assertEquals(1, resource.frontends().size());
            LoadBalancerFrontend frontend = resource.frontends().get("default");
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));
            Assert.assertEquals(2, publicFrontend.loadBalancingRules().size());

            // Verify probes
            LoadBalancerTcpProbe tcpProbe = resource.tcpProbes().get("default");
            Assert.assertNotNull(tcpProbe);
            Assert.assertEquals(22, tcpProbe.port());

            LoadBalancerHttpProbe httpProbe = resource.httpProbes().get("httpprobe");
            Assert.assertNotNull(httpProbe);
            Assert.assertEquals(3, httpProbe.numberOfProbes());
            Assert.assertTrue("/foo".equalsIgnoreCase(httpProbe.requestPath()));
            Assert.assertTrue(httpProbe.loadBalancingRules().containsKey("lbrule2"));

            // Verify backends
            Assert.assertTrue(resource.backends().containsKey("backend2"));
            Assert.assertTrue(!resource.backends().containsKey("default"));

            // Verify load balancing rules
            LoadBalancingRule lbRule = resource.loadBalancingRules().get("default");
            Assert.assertNotNull(lbRule);
            Assert.assertNull(lbRule.backend());
            Assert.assertEquals(8080, lbRule.backendPort());
            Assert.assertTrue("default".equalsIgnoreCase(lbRule.frontend().name()));
            Assert.assertEquals(11,  lbRule.idleTimeoutInMinutes());

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assert.assertNotNull(lbRule);
            Assert.assertEquals(22, lbRule.frontendPort());
            Assert.assertNotNull(lbRule.frontend());
            Assert.assertTrue("default".equalsIgnoreCase(lbRule.frontend().name()));
            Assert.assertTrue("httpprobe".equalsIgnoreCase(lbRule.probe().name()));
            Assert.assertEquals(TransportProtocol.UDP, lbRule.protocol());
            Assert.assertNotNull(lbRule.backend());
            Assert.assertTrue("backend2".equalsIgnoreCase(lbRule.backend().name()));

            return resource;
        }
    }

    /**
     * Internal minimalistic LB test.
     */
    public static class InternalMinimal extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final VirtualMachines vms;
        private final Networks networks;
        private final AvailabilitySets availabilitySets;
        private Network network;

        /**
         * Tests an internal load balancer with minimum inputs.
         * @param vms virtual machines
         * @param networks virtual networks
         * @param availabilitySets availability sets
         */
        public InternalMinimal(
                VirtualMachines vms,
                Networks networks,
                AvailabilitySets availabilitySets) {
            initializeResourceNames();
            this.vms = vms;
            this.networks = networks;
            this.availabilitySets = availabilitySets;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, this.availabilitySets, 2);

            // Must use the same VNet as the VMs
            this.network = existingVMs[0].getPrimaryNetworkInterface().primaryIpConfiguration().getNetwork();

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // Frontend (default)
                    .withFrontendSubnet(network, "subnet1")
                    // Backend (default)
                    .withExistingVirtualMachines(existingVMs)
                    .defineBackend("foo")
                    .attach()
                    // Probe (default)
                    .withTcpProbe(22)
                    // LB rule (default)
                    .withLoadBalancingRule(80, TransportProtocol.TCP)
                    .create();

            // Verify frontends
            Assert.assertTrue(lb.frontends().containsKey("default"));
            LoadBalancerFrontend frontend = lb.frontends().get("default");
            Assert.assertEquals(1, frontend.loadBalancingRules().size());
            Assert.assertTrue("default".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            Assert.assertFalse(frontend.isPublic());
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assert.assertTrue(network.id().equalsIgnoreCase(privateFrontend.networkId()));
            Assert.assertNotNull(privateFrontend.privateIpAddress());
            Assert.assertTrue("subnet1".equalsIgnoreCase(privateFrontend.subnetName()));
            Assert.assertEquals(IPAllocationMethod.DYNAMIC, privateFrontend.privateIpAllocationMethod());

            // Verify TCP probes
            Assert.assertTrue(lb.tcpProbes().containsKey("default"));
            Assert.assertEquals(1, lb.tcpProbes().size());
            LoadBalancerTcpProbe tcpProbe = lb.tcpProbes().get("default");
            Assert.assertTrue(tcpProbe.loadBalancingRules().containsKey("default"));
            Assert.assertEquals(1, tcpProbe.loadBalancingRules().size());
            Assert.assertEquals(22, tcpProbe.port());
            Assert.assertEquals(ProbeProtocol.TCP, tcpProbe.protocol());

            // Verify rules
            Assert.assertEquals(1, lb.loadBalancingRules().size());
            Assert.assertTrue(lb.loadBalancingRules().containsKey("default"));
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("default");
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.frontend().name()));
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.probe().name()));
            Assert.assertEquals(80, lbrule.backendPort());
            Assert.assertNotNull(lbrule.frontend());
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.frontend().name()));
            Assert.assertEquals(80, lbrule.frontendPort());
            Assert.assertNotNull(lbrule.probe());
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.probe().name()));
            Assert.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assert.assertNotNull(lbrule.backend());
            Assert.assertTrue("default".equalsIgnoreCase(lbrule.backend().name()));

            // Verify backends
            Assert.assertEquals(2, lb.backends().size());

            LoadBalancerBackend backend = lb.backends().get("foo");
            Assert.assertNotNull(backend);
            Assert.assertTrue(backend.backendNicIpConfigurationNames().isEmpty());

            backend = lb.backends().get("default");
            Assert.assertNotNull(backend);
            Assert.assertEquals(2, backend.backendNicIpConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assert.assertTrue(backend.backendNicIpConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            resource =  resource.update()
                    .updateInternalFrontend("default")
                        .withExistingSubnet(this.network, "subnet2")
                        .withPrivateIpAddressStatic("10.0.0.13")
                        .parent()
                    .updateTcpProbe("default")
                        .withPort(22)
                        .parent()
                    .defineHttpProbe("httpprobe")
                        .withRequestPath("/foo")
                        .withNumberOfProbes(3)
                        .withPort(443)
                        .attach()
                    .updateLoadBalancingRule("default")
                        .withBackendPort(8080)
                        .withIdleTimeoutInMinutes(11)
                        .parent()
                    .defineLoadBalancingRule("lbrule2")
                        .withProtocol(TransportProtocol.UDP)
                        .withFrontend("default")
                        .withFrontendPort(22)
                        .withProbe("httpprobe")
                        .withBackend("backend2")
                        .attach()
                    .defineBackend("backend2")
                        .attach()
                    .withoutBackend("default")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assert.assertEquals(1, resource.frontends().size());
            LoadBalancerFrontend frontend = resource.frontends().get("default");
            Assert.assertFalse(frontend.isPublic());
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assert.assertTrue("subnet2".equalsIgnoreCase(privateFrontend.subnetName()));
            Assert.assertEquals(IPAllocationMethod.STATIC, privateFrontend.privateIpAllocationMethod());
            Assert.assertTrue("10.0.0.13".equalsIgnoreCase(privateFrontend.privateIpAddress()));
            Assert.assertEquals(2, privateFrontend.loadBalancingRules().size());

            // Verify probes
            LoadBalancerTcpProbe tcpProbe = resource.tcpProbes().get("default");
            Assert.assertNotNull(tcpProbe);
            Assert.assertEquals(22,  tcpProbe.port());

            LoadBalancerHttpProbe httpProbe = resource.httpProbes().get("httpprobe");
            Assert.assertNotNull(httpProbe);
            Assert.assertEquals(3, httpProbe.numberOfProbes());
            Assert.assertTrue("/foo".equalsIgnoreCase(httpProbe.requestPath()));
            Assert.assertTrue(httpProbe.loadBalancingRules().containsKey("lbrule2"));

            // Verify backends
            Assert.assertTrue(resource.backends().containsKey("backend2"));
            Assert.assertTrue(!resource.backends().containsKey("default"));

            // Verify load balancing rules
            LoadBalancingRule lbRule = resource.loadBalancingRules().get("default");
            Assert.assertNotNull(lbRule);
            Assert.assertNull(lbRule.backend());
            Assert.assertEquals(8080, lbRule.backendPort());
            Assert.assertTrue("default".equalsIgnoreCase(lbRule.frontend().name()));
            Assert.assertEquals(11,  lbRule.idleTimeoutInMinutes());

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assert.assertNotNull(lbRule);
            Assert.assertEquals(22, lbRule.frontendPort());
            Assert.assertNotNull(lbRule.frontend());
            Assert.assertTrue("default".equalsIgnoreCase(lbRule.frontend().name()));
            Assert.assertTrue("httpprobe".equalsIgnoreCase(lbRule.probe().name()));
            Assert.assertEquals(TransportProtocol.UDP, lbRule.protocol());
            Assert.assertNotNull(lbRule.backend());
            Assert.assertTrue("backend2".equalsIgnoreCase(lbRule.backend().name()));

            return resource;
        }
    }

    // Create VNet for the LB
    private static Map<String, PublicIpAddress> ensurePIPs(PublicIpAddresses pips) throws Exception {
        List<Creatable<PublicIpAddress>> creatablePips = new ArrayList<>();
        for (int i = 0; i < PIP_NAMES.length; i++) {
            creatablePips.add(pips.define(PIP_NAMES[i])
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withLeafDomainLabel(PIP_NAMES[i]));
        }

        return pips.create(creatablePips);
    }

    // Ensure VMs for the LB
    private static VirtualMachine[] ensureVMs(Networks networks, VirtualMachines vms, AvailabilitySets availabilitySets, int count) throws Exception {
        // Create a network for the VMs
        Network network = networks.define("net" + TEST_ID)
                .withRegion(REGION)
                .withNewResourceGroup(GROUP_NAME)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        Creatable<AvailabilitySet> availabilitySetDefinition = availabilitySets.define("as" + TEST_ID)
                .withRegion(REGION)
                .withExistingResourceGroup(GROUP_NAME)
                .withSku(AvailabilitySetSkuTypes.MANAGED);

        // Create the requested number of VM definitions
        String userName = "testuser" + TEST_ID;
        List<Creatable<VirtualMachine>> vmDefinitions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String vmName = SdkContext.randomResourceName("vm", 15);

            Creatable<VirtualMachine> vm = vms.define(vmName)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet(network.subnets().values().iterator().next().name())
                    .withPrimaryPrivateIpAddressDynamic()
                    .withoutPrimaryPublicIpAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword("Abcdef.123456")
                    .withNewAvailabilitySet(availabilitySetDefinition)
                    .withSize(VirtualMachineSizeTypes.STANDARD_A1);

            vmDefinitions.add(vm);
        }

        CreatedResources<VirtualMachine> createdVMs2 = vms.create(vmDefinitions);
        VirtualMachine[] array = new VirtualMachine[createdVMs2.size()];
        for (int index = 0; index < createdVMs2.size(); index++) {
            array[index] = createdVMs2.get(vmDefinitions.get(index).key());
        }
        return array;
    }

    // Print LB info
    static void printLB(LoadBalancer resource) {
        StringBuilder info = new StringBuilder();
        info.append("Load balancer: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tBackends: ").append(resource.backends().keySet().toString());

        // Show public IP addresses
        info.append("\n\tPublic IP address IDs: ")
            .append(resource.publicIpAddressIds().size());
        for (String pipId : resource.publicIpAddressIds()) {
            info.append("\n\t\tPIP id: ").append(pipId);
        }

        // Show TCP probes
        info.append("\n\tTCP probes: ")
            .append(resource.tcpProbes().size());
        for (LoadBalancerTcpProbe probe : resource.tcpProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                .append("\n\t\t\tPort: ").append(probe.port())
                .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes());

            // Show associated load balancing rules
            info.append("\n\t\t\tReferenced from load balancing rules: ")
                .append(probe.loadBalancingRules().size());
            for (LoadBalancingRule rule : probe.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show HTTP probes
        info.append("\n\tHTTP probes: ")
            .append(resource.httpProbes().size());
        for (LoadBalancerHttpProbe probe : resource.httpProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                .append("\n\t\t\tPort: ").append(probe.port())
                .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes())
                .append("\n\t\t\tHTTP request path: ").append(probe.requestPath());

            // Show associated load balancing rules
            info.append("\n\t\t\tReferenced from load balancing rules: ")
                .append(probe.loadBalancingRules().size());
            for (LoadBalancingRule rule : probe.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show load balancing rules
        info.append("\n\tLoad balancing rules: ")
            .append(resource.loadBalancingRules().size());
        for (LoadBalancingRule rule : resource.loadBalancingRules().values()) {
            info.append("\n\t\tLB rule name: ").append(rule.name())
                .append("\n\t\t\tProtocol: ").append(rule.protocol())
                .append("\n\t\t\tFloating IP enabled? ").append(rule.floatingIpEnabled())
                .append("\n\t\t\tIdle timeout in minutes: ").append(rule.idleTimeoutInMinutes())
                .append("\n\t\t\tLoad distribution method: ").append(rule.loadDistribution().toString());

            LoadBalancerFrontend frontend = rule.frontend();
            info.append("\n\t\t\tFrontend: ");
            if (frontend != null) {
                info.append(frontend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tFrontend port: ").append(rule.frontendPort());

            LoadBalancerBackend backend = rule.backend();
            info.append("\n\t\t\tBackend: ");
            if (backend != null) {
                info.append(backend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tBackend port: ").append(rule.backendPort());

            LoadBalancerProbe probe = rule.probe();
            info.append("\n\t\t\tProbe: ");
            if (probe == null) {
                info.append("(None)");
            } else {
                info.append(probe.name()).append(" [").append(probe.protocol().toString()).append("]");
            }
        }

        // Show frontends
        info.append("\n\tFrontends: ")
            .append(resource.frontends().size());
        for (LoadBalancerFrontend frontend : resource.frontends().values()) {
            info.append("\n\t\tFrontend name: ").append(frontend.name())
                .append("\n\t\t\tInternet facing: ").append(frontend.isPublic());
            if (frontend.isPublic()) {
                info.append("\n\t\t\tPublic IP Address ID: ").append(((LoadBalancerPublicFrontend) frontend).publicIpAddressId());
            } else {
                info.append("\n\t\t\tVirtual network ID: ").append(((LoadBalancerPrivateFrontend) frontend).networkId())
                    .append("\n\t\t\tSubnet name: ").append(((LoadBalancerPrivateFrontend) frontend).subnetName())
                    .append("\n\t\t\tPrivate IP address: ").append(((LoadBalancerPrivateFrontend) frontend).privateIpAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ").append(((LoadBalancerPrivateFrontend) frontend).privateIpAllocationMethod());
            }

            // Inbound NAT pool references
            info.append("\n\t\t\tReferenced inbound NAT pools: ")
                .append(frontend.inboundNatPools().size());
            for (LoadBalancerInboundNatPool pool : frontend.inboundNatPools().values()) {
                info.append("\n\t\t\t\tName: ").append(pool.name());
            }

            // Inbound NAT rule references
            info.append("\n\t\t\tReferenced inbound NAT rules: ")
                .append(frontend.inboundNatRules().size());
            for (LoadBalancerInboundNatRule rule : frontend.inboundNatRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }

            // Load balancing rule references
            info.append("\n\t\t\tReferenced load balancing rules: ")
                .append(frontend.loadBalancingRules().size());
            for (LoadBalancingRule rule : frontend.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show inbound NAT rules
        info.append("\n\tInbound NAT rules: ")
            .append(resource.inboundNatRules().size());
        for (LoadBalancerInboundNatRule natRule : resource.inboundNatRules().values()) {
            info.append("\n\t\tInbound NAT rule name: ").append(natRule.name())
                .append("\n\t\t\tProtocol: ").append(natRule.protocol().toString())
                .append("\n\t\t\tFrontend: ").append(natRule.frontend().name())
                .append("\n\t\t\tFrontend port: ").append(natRule.frontendPort())
                .append("\n\t\t\tBackend port: ").append(natRule.backendPort())
                .append("\n\t\t\tBackend NIC ID: ").append(natRule.backendNetworkInterfaceId())
                .append("\n\t\t\tBackend NIC IP config name: ").append(natRule.backendNicIpConfigurationName())
                .append("\n\t\t\tFloating IP? ").append(natRule.floatingIpEnabled())
                .append("\n\t\t\tIdle timeout in minutes: ").append(natRule.idleTimeoutInMinutes());
        }

        // Show inbound NAT pools
        info.append("\n\tInbound NAT pools: ")
            .append(resource.inboundNatPools().size());
        for (LoadBalancerInboundNatPool natPool: resource.inboundNatPools().values()) {
            info.append("\n\t\tInbound NAT pool name: ").append(natPool.name())
                .append("\n\t\t\tProtocol: ").append(natPool.protocol().toString())
                .append("\n\t\t\tFrontend: ").append(natPool.frontend().name())
                .append("\n\t\t\tFrontend port range: ")
                    .append(natPool.frontendPortRangeStart())
                    .append("-")
                    .append(natPool.frontendPortRangeEnd())
                .append("\n\t\t\tBackend port: ").append(natPool.backendPort());
            }

        // Show backends
        info.append("\n\tBackends: ")
            .append(resource.backends().size());
        for (LoadBalancerBackend backend : resource.backends().values()) {
            info.append("\n\t\tBackend name: ").append(backend.name());

            // Show assigned backend NICs
            info.append("\n\t\t\tReferenced NICs: ")
                .append(backend.backendNicIpConfigurationNames().entrySet().size());
            for (Entry<String, String> entry : backend.backendNicIpConfigurationNames().entrySet()) {
                info.append("\n\t\t\t\tNIC ID: ").append(entry.getKey())
                    .append(" - IP Config: ").append(entry.getValue());
            }

            // Show assigned virtual machines
            Set<String> vmIds = backend.getVirtualMachineIds();
            info.append("\n\t\t\tReferenced virtual machine ids: ")
                .append(vmIds.size());
            for (String vmId : vmIds) {
                info.append("\n\t\t\t\tVM ID: ").append(vmId);
            }

            // Show assigned load balancing rules
            info.append("\n\t\t\tReferenced load balancing rules: ")
                .append(new ArrayList<String>(backend.loadBalancingRules().keySet()));
        }

        System.out.println(info.toString());
    }
}
