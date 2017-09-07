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
import com.microsoft.azure.management.network.LoadBalancerSkuType;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import org.junit.Assert;

import com.microsoft.azure.management.compute.AvailabilitySet;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
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
import com.microsoft.azure.management.network.LoadBalancerPrivateFrontend;
import com.microsoft.azure.management.network.LoadBalancerProbe;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.network.LoadBalancerTcpProbe;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Test of load balancer management.
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
        private final ComputeManager computeManager;

        /**
         * Test of a load balancer with a NAT pool.
         * @param computeManager compute manager
         */
        public InternetWithNatPool(ComputeManager computeManager) {
            initializeResourceNames();
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);
            ensurePIPs(resources.manager().publicIPAddresses());
            PublicIPAddress pip0 = resources.manager().publicIPAddresses().getByResourceGroup(GROUP_NAME, PIP_NAMES[0]);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)

                    // Load balancing rules
                    .defineLoadBalancingRule("rule1")
                        .withProtocol(TransportProtocol.TCP)    // Required
                        .fromExistingPublicIPAddress(pip0)
                        .fromFrontendPort(81)
                        .toBackend("backend1")
                        .toBackendPort(82)                    // Optionals
                        .withProbe("tcpProbe1")
                        .withIdleTimeoutInMinutes(10)
                        .withLoadDistribution(LoadDistribution.SOURCE_IP)
                        .attach()

                    // Inbound NAT pools
                    .defineInboundNatPool("natpool1")
                        .withProtocol(TransportProtocol.TCP)
                        .fromExistingPublicIPAddress(pip0)
                        .fromFrontendPortRange(2000, 2001)
                        .toBackendPort(8080)
                        .attach()

                   // Probes (Optional)
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

                    // Backends
                    .defineBackend("backend1")
                        .withExistingVirtualMachines(existingVMs)
                        .attach()

                    .create();

            // Verify frontends
            Assert.assertEquals(1, lb.frontends().size());
            Assert.assertEquals(1, lb.publicFrontends().size());
            Assert.assertEquals(0, lb.privateFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip0.id().equalsIgnoreCase(publicFrontend.publicIPAddressId()));

            // Verify backends
            Assert.assertEquals(1, lb.backends().size());

            // Verify probes
            Assert.assertEquals(1, lb.httpProbes().size());
            Assert.assertTrue(lb.httpProbes().containsKey("httpProbe1"));
            Assert.assertEquals(1, lb.tcpProbes().size());
            Assert.assertTrue(lb.tcpProbes().containsKey("tcpProbe1"));

            // Verify rules
            Assert.assertEquals(1, lb.loadBalancingRules().size());
            Assert.assertTrue(lb.loadBalancingRules().containsKey("rule1"));
            LoadBalancingRule rule = lb.loadBalancingRules().get("rule1");
            Assert.assertNotNull(rule.backend());
            Assert.assertTrue(rule.probe().name().equalsIgnoreCase("tcpProbe1"));

            // Verify inbound NAT pools
            Assert.assertTrue(lb.inboundNatPools().containsKey("natpool1"));
            Assert.assertEquals(1, lb.inboundNatPools().size());
            LoadBalancerInboundNatPool inboundNatPool = lb.inboundNatPools().get("natpool1");
            Assert.assertEquals(2000, inboundNatPool.frontendPortRangeStart());
            Assert.assertEquals(2001, inboundNatPool.frontendPortRangeEnd());
            Assert.assertEquals(8080, inboundNatPool.backendPort());

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            resource =  resource.update()
                    .withoutBackend("backend1")
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatPool("natpool1")
                    .withoutProbe("httpProbe1")
                    .withoutProbe("tcpProbe1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();

            resource.refresh();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assert.assertEquals(1, resource.frontends().size());
            Assert.assertEquals(1,  resource.publicFrontends().size());
            Assert.assertEquals(0,  resource.privateFrontends().size());

            // Verify probes
            Assert.assertFalse(resource.httpProbes().containsKey("httpProbe1"));
            Assert.assertFalse(resource.httpProbes().containsKey("tcpProbe1"));
            Assert.assertEquals(0, resource.httpProbes().size());
            Assert.assertEquals(0, resource.tcpProbes().size());

            // Verify backends
            Assert.assertEquals(0, resource.backends().size());

            // Verify rules
            Assert.assertFalse(resource.loadBalancingRules().containsKey("rule1"));
            Assert.assertEquals(0, resource.loadBalancingRules().size());

            // Verify NAT pools
            Assert.assertFalse(resource.inboundNatPools().containsKey("natpool1"));

            return resource;
        }
    }

    /**
     * Internet-facing LB test with NAT rules.
     */
    public static class InternetWithNatRule extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;

        /**
         * Tests an Internet-facing load balancer with NAT rules.
         * @param computeManager compute manager
         */
        public InternetWithNatRule(ComputeManager computeManager) {
            initializeResourceNames();
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);
            ensurePIPs(resources.manager().publicIPAddresses());
            PublicIPAddress pip = resources.manager().publicIPAddresses().getByResourceGroup(GROUP_NAME, PIP_NAMES[0]);
            NetworkInterface nic1 = existingVMs[0].getPrimaryNetworkInterface();
            NetworkInterface nic2 = existingVMs[1].getPrimaryNetworkInterface();

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                .withRegion(TestLoadBalancer.REGION)
                .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)

                // Load balancing rules
                .defineLoadBalancingRule("rule1")
                    .withProtocol(TransportProtocol.TCP)    // Required
                    .fromExistingPublicIPAddress(pip)
                    .fromFrontendPort(81)
                    .toBackend("backend1")
                    .toBackendPort(82)                     // Optionals
                    .withProbe("tcpProbe1")
                    .withIdleTimeoutInMinutes(10)
                    .withLoadDistribution(LoadDistribution.SOURCE_IP)
                    .attach()

                // Inbound NAT rules
                .defineInboundNatRule("natrule1")
                    .withProtocol(TransportProtocol.TCP)
                    .fromExistingPublicIPAddress(pip)   // Implicitly uses the same frontend because the PIP is the same
                    .fromFrontendPort(88)
                    .attach()

                // Probes (Optional)
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

                .create();

            String backendName = lb.backends().values().iterator().next().name();
            String frontendName = lb.frontends().values().iterator().next().name();

            // Connect NICs explicitly
            nic1.update()
                .withExistingLoadBalancerBackend(lb, backendName)
                .withExistingLoadBalancerInboundNatRule(lb,  "natrule1")
                .apply();
            TestNetworkInterface.printNic(nic1);
            Assert.assertTrue(nic1.primaryIPConfiguration().listAssociatedLoadBalancerBackends().get(0).name()
                    .equalsIgnoreCase(backendName));
            Assert.assertTrue(nic1.primaryIPConfiguration().listAssociatedLoadBalancerInboundNatRules().get(0).name()
                    .equalsIgnoreCase("natrule1"));

            nic2.update()
                .withExistingLoadBalancerBackend(lb, backendName)
                .apply();
            TestNetworkInterface.printNic(nic2);
            Assert.assertTrue(nic2.primaryIPConfiguration().listAssociatedLoadBalancerBackends().get(0).name()
                    .equalsIgnoreCase(backendName));

            // Verify frontends
            Assert.assertEquals(1, lb.frontends().size());
            Assert.assertEquals(1, lb.publicFrontends().size());
            Assert.assertEquals(0, lb.privateFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().get(frontendName);
            Assert.assertNotNull(frontend);
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIPAddressId()));

            pip.refresh();
            Assert.assertTrue(pip.getAssignedLoadBalancerFrontend().name().equalsIgnoreCase(frontendName));
            TestPublicIPAddress.printPIP(pip.refresh());

            // Verify backends
            Assert.assertTrue(lb.backends().containsKey(backendName));
            Assert.assertEquals(1, lb.backends().size());

            // Verify probes
            Assert.assertTrue(lb.httpProbes().containsKey("httpProbe1"));
            Assert.assertEquals(1, lb.httpProbes().size());
            Assert.assertTrue(lb.tcpProbes().containsKey("tcpProbe1"));
            Assert.assertEquals(1, lb.tcpProbes().size());

            // Verify rules
            Assert.assertEquals(1, lb.loadBalancingRules().size());
            Assert.assertTrue(lb.loadBalancingRules().containsKey("rule1"));
            LoadBalancingRule rule = lb.loadBalancingRules().get("rule1");
            Assert.assertTrue(rule.backend().name().equalsIgnoreCase(backendName));
            Assert.assertTrue(rule.frontend().name().equalsIgnoreCase(frontendName));
            Assert.assertTrue(rule.probe().name().equalsIgnoreCase("tcpProbe1"));

            // Verify inbound NAT rules
            Assert.assertEquals(1, lb.inboundNatRules().size());
            Assert.assertTrue(lb.inboundNatRules().containsKey("natrule1"));
            LoadBalancerInboundNatRule inboundNatRule = lb.inboundNatRules().get("natrule1");
            Assert.assertTrue(inboundNatRule.frontend().name().equalsIgnoreCase(frontendName));
            Assert.assertEquals(88, inboundNatRule.frontendPort());
            Assert.assertEquals(88, inboundNatRule.backendPort());

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            String backendName = resource.backends().values().iterator().next().name();
            String frontendName = resource.frontends().values().iterator().next().name();

            List<NetworkInterface> nics = new ArrayList<>();
            for (String nicId : resource.backends().get(backendName).backendNicIPConfigurationNames().keySet()) {
                nics.add(resource.manager().networkInterfaces().getById(nicId));
            }
            NetworkInterface nic1 = nics.get(0);
            NetworkInterface nic2 = nics.get(1);

            // Remove the NIC associations
            nic1.update()
                .withoutLoadBalancerBackends()
                .withoutLoadBalancerInboundNatRules()
                .apply();
            Assert.assertTrue(nic1.primaryIPConfiguration().listAssociatedLoadBalancerBackends().size() == 0);

            nic2.update()
                .withoutLoadBalancerBackends()
                .withoutLoadBalancerInboundNatRules()
                .apply();
            Assert.assertTrue(nic2.primaryIPConfiguration().listAssociatedLoadBalancerBackends().size() == 0);

            // Update the load balancer
            ensurePIPs(resource.manager().publicIPAddresses());
            PublicIPAddress pip = resource.manager().publicIPAddresses().getByResourceGroup(GROUP_NAME, PIP_NAMES[1]);
            resource =  resource.update()
                    .updatePublicFrontend(frontendName)
                        .withExistingPublicIPAddress(pip)
                        .parent()
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatRule("natrule1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));
            Assert.assertEquals(0, resource.inboundNatRules().size());

            // Verify frontends
            LoadBalancerFrontend frontend = resource.frontends().get(frontendName);
            Assert.assertEquals(1,  resource.publicFrontends().size());
            Assert.assertEquals(0,  resource.privateFrontends().size());
            Assert.assertNotNull(frontend);
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIPAddressId()));

            return resource;
        }
    }

    /**
     * Internet-facing minimalistic LB test without LB rules, only a NAT rule.
     */
    public static class InternetNatOnly extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;

        /**
         * Tests an Internet-facing load balancer with a NAT rule only.
         * @param computeManager compute manager
         */
        public InternetNatOnly(ComputeManager computeManager) {
            initializeResourceNames();
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), computeManager, 2);
            Creatable<PublicIPAddress> pipDef = resources.manager().publicIPAddresses().define(PIP_NAMES[0])
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    .withLeafDomainLabel(PIP_NAMES[0]);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // Inbound NAT rule
                    .defineInboundNatRule("natrule1")
                        .withProtocol(TransportProtocol.TCP)
                        .fromNewPublicIPAddress(pipDef)
                        .fromFrontendPort(88)
                        .toBackendPort(80)
                        .attach()
                    // Backend
                    .defineBackend("backend1")
                        .withExistingVirtualMachines(existingVMs)
                        .attach()
                    .create();

            // Verify frontends
            Assert.assertEquals(1, lb.frontends().size());
            Assert.assertEquals(1, lb.publicFrontends().size());
            Assert.assertEquals(0,  lb.privateFrontends().size());
            LoadBalancerPublicFrontend frontend = lb.publicFrontends().values().iterator().next();
            Assert.assertNotNull(frontend);
            Assert.assertNotNull(frontend.publicIPAddressId());

            // Verify probes
            Assert.assertTrue(lb.tcpProbes().isEmpty());
            Assert.assertTrue(lb.httpProbes().isEmpty());

            // Verify LB rules
            Assert.assertEquals(0, lb.loadBalancingRules().size());

            // Verify NAT rules
            Assert.assertEquals(1, lb.inboundNatRules().size());
            LoadBalancerInboundNatRule natRule = lb.inboundNatRules().get("natrule1");
            Assert.assertNotNull(natRule);
            Assert.assertEquals(TransportProtocol.TCP, natRule.protocol());
            Assert.assertNotNull(natRule.frontend());
            Assert.assertTrue(natRule.frontend().isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) natRule.frontend();
            PublicIPAddress pip = publicFrontend.getPublicIPAddress();
            Assert.assertNotNull(pip);
            Assert.assertEquals(pip.name(), PIP_NAMES[0]);
            Assert.assertEquals(pip.leafDomainLabel(), PIP_NAMES[0]);
            Assert.assertEquals(88, natRule.frontendPort());

            // Verify backends
            Assert.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assert.assertNotNull(backend);
            Assert.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assert.assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            LoadBalancerBackend backend = resource.backends().values().iterator().next();
            Assert.assertNotNull(backend);
            LoadBalancerInboundNatRule natRule = resource.inboundNatRules().values().iterator().next();
            Assert.assertNotNull(natRule);
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) natRule.frontend();
            PublicIPAddress pip = resource.manager().publicIPAddresses().define(PIP_NAMES[1])
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    .withLeafDomainLabel(PIP_NAMES[1])
                    .create();

            resource =  resource.update()
                    .updatePublicFrontend(publicFrontend.name())
                        .withExistingPublicIPAddress(pip)
                        .parent()
                    .defineBackend("backend2")
                        .attach()
                    .withoutBackend(backend.name())
                    .withoutInboundNatRule("natrule1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assert.assertEquals(1, resource.frontends().size());
            Assert.assertEquals(1, resource.publicFrontends().size());
            Assert.assertEquals(0,  resource.privateFrontends().size());
            LoadBalancerFrontend frontend = resource.frontends().get(publicFrontend.name());
            Assert.assertTrue(frontend.isPublic());
            publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIPAddressId()));
            Assert.assertEquals(0, publicFrontend.loadBalancingRules().size());

            // Verify probes
            Assert.assertTrue(resource.tcpProbes().isEmpty());
            Assert.assertTrue(resource.httpProbes().isEmpty());

            // Verify backends
            Assert.assertTrue(resource.backends().containsKey("backend2"));
            Assert.assertTrue(!resource.backends().containsKey(backend.name()));

            // Verify NAT rules
            Assert.assertTrue(resource.inboundNatRules().isEmpty());

            // Verify load balancing rules
            Assert.assertEquals(0, resource.loadBalancingRules().size());

            return resource;
        }
    }

    /**
     * Internet-facing minimalistic LB test.
     */
    public static class InternetMinimal extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;

        /**
         * Tests an Internet-facing load balancer with minimum inputs.
         * @param computeManager compute manager
         */
        public InternetMinimal(ComputeManager computeManager) {
            initializeResourceNames();
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);
            String pipDnsLabel = SdkContext.randomResourceName("pip", 20);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // LB rule
                    .defineLoadBalancingRule("lbrule1")
                        .withProtocol(TransportProtocol.TCP)
                        .fromNewPublicIPAddress(pipDnsLabel)
                        .fromFrontendPort(80)
                        .toExistingVirtualMachines(existingVMs)
                        .attach()
                    .create();

            // Verify frontends
            Assert.assertEquals(1, lb.frontends().size());
            Assert.assertEquals(1, lb.publicFrontends().size());
            Assert.assertEquals(0, lb.privateFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assert.assertEquals(1, frontend.loadBalancingRules().size());
            Assert.assertTrue("lbrule1".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            PublicIPAddress pip = publicFrontend.getPublicIPAddress();
            Assert.assertNotNull(pip);
            Assert.assertTrue(pip.leafDomainLabel().equalsIgnoreCase(pipDnsLabel));

            // Verify TCP probes
            Assert.assertEquals(0, lb.tcpProbes().size());

            // Verify rules
            Assert.assertEquals(1, lb.loadBalancingRules().size());
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("lbrule1");
            Assert.assertNotNull(lbrule.frontend());
            Assert.assertEquals(80, lbrule.backendPort());
            Assert.assertEquals(80, lbrule.frontendPort());
            Assert.assertNull(lbrule.probe());
            Assert.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assert.assertNotNull(lbrule.backend());

            // Verify backends
            Assert.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assert.assertNotNull(backend);
            Assert.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assert.assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            ensurePIPs(resource.manager().publicIPAddresses());
            PublicIPAddress pip = resource.manager().publicIPAddresses().getByResourceGroup(TestLoadBalancer.GROUP_NAME, PIP_NAMES[0]);
            Assert.assertNotNull(pip);
            LoadBalancerBackend backend = resource.backends().values().iterator().next();
            Assert.assertNotNull(backend);
            LoadBalancingRule lbRule = resource.loadBalancingRules().get("lbrule1");
            Assert.assertNotNull(lbRule);

            resource =  resource.update()
                    .updatePublicFrontend(lbRule.frontend().name())
                        .withExistingPublicIPAddress(pip)
                        .parent()
                    .defineTcpProbe("tcpprobe")
                        .withPort(22)
                        .attach()
                    .defineHttpProbe("httpprobe")
                        .withRequestPath("/foo")
                        .withNumberOfProbes(3)
                        .withPort(443)
                        .attach()
                    .updateLoadBalancingRule("lbrule1")
                        .toBackendPort(8080)
                        .withIdleTimeoutInMinutes(11)
                        .withProbe("tcpprobe")
                        .parent()
                    .defineLoadBalancingRule("lbrule2")
                        .withProtocol(TransportProtocol.UDP)
                        .fromFrontend(lbRule.frontend().name())
                        .fromFrontendPort(22)
                        .toBackend("backend2")
                        .withProbe("httpprobe")
                        .attach()
                    .withoutBackend(backend.name())
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assert.assertEquals(1, resource.frontends().size());
            Assert.assertEquals(1, resource.publicFrontends().size());
            Assert.assertEquals(0, resource.privateFrontends().size());
            LoadBalancerFrontend frontend = lbRule.frontend();
            Assert.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assert.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIPAddressId()));
            Assert.assertEquals(2, publicFrontend.loadBalancingRules().size());

            // Verify probes
            LoadBalancerTcpProbe tcpProbe = resource.tcpProbes().get("tcpprobe");
            Assert.assertNotNull(tcpProbe);
            Assert.assertEquals(22, tcpProbe.port());
            Assert.assertEquals(1, tcpProbe.loadBalancingRules().size());
            Assert.assertTrue(tcpProbe.loadBalancingRules().containsKey("lbrule1"));

            LoadBalancerHttpProbe httpProbe = resource.httpProbes().get("httpprobe");
            Assert.assertNotNull(httpProbe);
            Assert.assertEquals(3, httpProbe.numberOfProbes());
            Assert.assertTrue("/foo".equalsIgnoreCase(httpProbe.requestPath()));
            Assert.assertTrue(httpProbe.loadBalancingRules().containsKey("lbrule2"));

            // Verify backends
            Assert.assertEquals(1, resource.backends().size());
            Assert.assertTrue(resource.backends().containsKey("backend2"));
            Assert.assertTrue(!resource.backends().containsKey(backend.name()));

            // Verify load balancing rules
            lbRule = resource.loadBalancingRules().get("lbrule1");
            Assert.assertNotNull(lbRule);
            Assert.assertNull(lbRule.backend());
            Assert.assertEquals(8080, lbRule.backendPort());
            Assert.assertNotNull(lbRule.frontend());
            Assert.assertEquals(11,  lbRule.idleTimeoutInMinutes());
            Assert.assertNotNull(lbRule.probe());
            Assert.assertEquals(tcpProbe.name(), lbRule.probe().name());

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assert.assertNotNull(lbRule);
            Assert.assertEquals(22, lbRule.frontendPort());
            Assert.assertNotNull(lbRule.frontend());
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
        private final ComputeManager computeManager;
        private Network network;

        /**
         * Tests an internal load balancer with minimum inputs.
         * @param computeManager compute manager
         */
        public InternalMinimal(ComputeManager computeManager) {
            initializeResourceNames();
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);

            // Must use the same VNet as the VMs
            this.network = existingVMs[0].getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // LB rule
                    .defineLoadBalancingRule("lbrule1")
                        .withProtocol(TransportProtocol.TCP)
                        .fromExistingSubnet(network, "subnet1")
                        .fromFrontendPort(80)
                        .toExistingVirtualMachines(existingVMs)
                        .attach()
                    .create();

            // Verify frontends
            Assert.assertEquals(1, lb.frontends().size());
            Assert.assertEquals(1, lb.privateFrontends().size());
            Assert.assertEquals(0, lb.publicFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assert.assertEquals(1, frontend.loadBalancingRules().size());
            Assert.assertFalse(frontend.isPublic());
            Assert.assertTrue("lbrule1".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assert.assertTrue(network.id().equalsIgnoreCase(privateFrontend.networkId()));
            Assert.assertNotNull(privateFrontend.privateIPAddress());
            Assert.assertTrue("subnet1".equalsIgnoreCase(privateFrontend.subnetName()));
            Assert.assertEquals(IPAllocationMethod.DYNAMIC, privateFrontend.privateIPAllocationMethod());

            // Verify TCP probes
            Assert.assertEquals(0, lb.tcpProbes().size());

            // Verify rules
            Assert.assertEquals(1, lb.loadBalancingRules().size());
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("lbrule1");
            Assert.assertNotNull(lbrule);
            Assert.assertNotNull(lbrule.frontend());
            Assert.assertEquals(80, lbrule.backendPort());
            Assert.assertEquals(80, lbrule.frontendPort());
            Assert.assertNull(lbrule.probe());
            Assert.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assert.assertNotNull(lbrule.backend());

            // Verify backends
            Assert.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assert.assertNotNull(backend);

            Assert.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assert.assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            LoadBalancerBackend backend = resource.backends().values().iterator().next();
            Assert.assertNotNull(backend);
            LoadBalancingRule lbRule = resource.loadBalancingRules().get("lbrule1");
            Assert.assertNotNull(lbRule);
            resource =  resource.update()
                    .updatePrivateFrontend(lbRule.frontend().name())
                        .withExistingSubnet(this.network, "subnet2")
                        .withPrivateIPAddressStatic("10.0.0.13")
                        .parent()
                    .defineTcpProbe("tcpprobe")
                        .withPort(22)
                        .attach()
                    .defineHttpProbe("httpprobe")
                        .withRequestPath("/foo")
                        .withNumberOfProbes(3)
                        .withPort(443)
                        .attach()
                    .updateLoadBalancingRule("lbrule1")
                        .toBackendPort(8080)
                        .withIdleTimeoutInMinutes(11)
                        .withProbe("tcpprobe")
                        .parent()
                    .defineLoadBalancingRule("lbrule2")
                        .withProtocol(TransportProtocol.UDP)
                        .fromFrontend(lbRule.frontend().name())
                        .fromFrontendPort(22)
                        .toBackend("backend2")
                        .withProbe("httpprobe")
                        .attach()
                    .withoutBackend(backend.name())
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assert.assertEquals(1, resource.frontends().size());
            Assert.assertEquals(1,  resource.privateFrontends().size());
            Assert.assertEquals(0, resource.publicFrontends().size());
            LoadBalancerFrontend frontend = resource.frontends().get(lbRule.frontend().name());
            Assert.assertNotNull(frontend);
            Assert.assertFalse(frontend.isPublic());
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assert.assertTrue("subnet2".equalsIgnoreCase(privateFrontend.subnetName()));
            Assert.assertEquals(IPAllocationMethod.STATIC, privateFrontend.privateIPAllocationMethod());
            Assert.assertTrue("10.0.0.13".equalsIgnoreCase(privateFrontend.privateIPAddress()));
            Assert.assertEquals(2, privateFrontend.loadBalancingRules().size());

            // Verify probes
            Assert.assertEquals(1, resource.tcpProbes().size());
            LoadBalancerTcpProbe tcpProbe = resource.tcpProbes().get("tcpprobe");
            Assert.assertNotNull(tcpProbe);
            Assert.assertEquals(22,  tcpProbe.port());
            Assert.assertTrue(tcpProbe.loadBalancingRules().containsKey("lbrule1"));

            LoadBalancerHttpProbe httpProbe = resource.httpProbes().get("httpprobe");
            Assert.assertNotNull(httpProbe);
            Assert.assertEquals(3, httpProbe.numberOfProbes());
            Assert.assertTrue("/foo".equalsIgnoreCase(httpProbe.requestPath()));
            Assert.assertTrue(httpProbe.loadBalancingRules().containsKey("lbrule2"));

            // Verify backends
            Assert.assertEquals(1, resource.backends().size());
            Assert.assertTrue(resource.backends().containsKey("backend2"));
            Assert.assertTrue(!resource.backends().containsKey(backend.name()));

            // Verify load balancing rules
            lbRule = resource.loadBalancingRules().get("lbrule1");
            Assert.assertNotNull(lbRule);
            Assert.assertNull(lbRule.backend());
            Assert.assertEquals(8080, lbRule.backendPort());
            Assert.assertNotNull(lbRule.frontend());
            Assert.assertEquals(11,  lbRule.idleTimeoutInMinutes());
            Assert.assertNotNull(lbRule.probe());
            Assert.assertEquals(tcpProbe.name(), lbRule.probe().name());

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assert.assertNotNull(lbRule);
            Assert.assertEquals(22, lbRule.frontendPort());
            Assert.assertNotNull(lbRule.frontend());
            Assert.assertTrue("httpprobe".equalsIgnoreCase(lbRule.probe().name()));
            Assert.assertEquals(TransportProtocol.UDP, lbRule.protocol());
            Assert.assertNotNull(lbRule.backend());
            Assert.assertTrue("backend2".equalsIgnoreCase(lbRule.backend().name()));

            return resource;
        }
    }

    /**
     * Basic SKU load balancer with zoned private
     */
    public static class InternalWithZone extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;
        private Network network;

        /**
         * Tests an internal load balancer with zoned front-end.
         * @param computeManager compute manager
         */
        public InternalWithZone(ComputeManager computeManager) {
            REGION = Region.US_EAST2;
            initializeResourceNames();
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            // Basic SKU (default) LB requires the VMs to be in the same availability set
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);

            // Must use the same VNet as the VMs
            this.network = existingVMs[0].getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // LB rule
                    .defineLoadBalancingRule("lbrule1")
                        .withProtocol(TransportProtocol.TCP)
                        .fromFrontend("frontend-1")
                        .fromFrontendPort(80)
                        .toExistingVirtualMachines(existingVMs)
                        .attach()
                    // Private zoned front-end
                    .definePrivateFrontend("frontend-1")
                        .withExistingSubnet(network, "subnet1")
                        .withAvailabilityZone("1")
                        .attach()
                    .withSku(LoadBalancerSkuType.BASIC)
                    .create();

            // Verify frontends
            Assert.assertEquals(1, lb.frontends().size());
            Assert.assertEquals(1, lb.privateFrontends().size());
            Assert.assertEquals(0, lb.publicFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assert.assertEquals(1, frontend.loadBalancingRules().size());
            Assert.assertFalse(frontend.isPublic());
            Assert.assertTrue("lbrule1".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assert.assertTrue(network.id().equalsIgnoreCase(privateFrontend.networkId()));
            Assert.assertNotNull(privateFrontend.privateIPAddress());
            Assert.assertTrue("subnet1".equalsIgnoreCase(privateFrontend.subnetName()));
            Assert.assertEquals(IPAllocationMethod.DYNAMIC, privateFrontend.privateIPAllocationMethod());
            // Verify frontend zone
            Assert.assertNotNull(privateFrontend.availabilityZones());
            Assert.assertFalse(privateFrontend.availabilityZones().isEmpty());
            Assert.assertTrue(privateFrontend.availabilityZones().contains("1"));

            // Verify TCP probes
            Assert.assertEquals(0, lb.tcpProbes().size());

            // Verify rules
            Assert.assertEquals(1, lb.loadBalancingRules().size());
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("lbrule1");
            Assert.assertNotNull(lbrule);
            Assert.assertNotNull(lbrule.frontend());
            Assert.assertEquals(80, lbrule.backendPort());
            Assert.assertEquals(80, lbrule.frontendPort());
            Assert.assertNull(lbrule.probe());
            Assert.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assert.assertNotNull(lbrule.backend());

            // Verify backends
            Assert.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assert.assertNotNull(backend);

            Assert.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assert.assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            // Once zone associated with a private front-end, it cannot be removed, updated or new
            // one cannot be added.
            //
            return resource;
        }
    }

    // Create VNet for the LB
    private static Map<String, PublicIPAddress> ensurePIPs(PublicIPAddresses pips) throws Exception {
        List<Creatable<PublicIPAddress>> creatablePips = new ArrayList<>();
        for (int i = 0; i < PIP_NAMES.length; i++) {
            creatablePips.add(pips.define(PIP_NAMES[i])
                    .withRegion(REGION)
                    .withNewResourceGroup(GROUP_NAME)
                    .withLeafDomainLabel(PIP_NAMES[i]));
        }

        return pips.create(creatablePips);
    }

    // Ensure VMs for the LB
    private static VirtualMachine[] ensureVMs(Networks networks, ComputeManager computeManager, int count) throws Exception {
        // Create a network for the VMs
        Network network = networks.define("net" + TEST_ID)
                .withRegion(REGION)
                .withNewResourceGroup(GROUP_NAME)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        Creatable<AvailabilitySet> availabilitySetDefinition = computeManager.availabilitySets().define("as" + TEST_ID)
                .withRegion(REGION)
                .withExistingResourceGroup(GROUP_NAME)
                .withSku(AvailabilitySetSkuTypes.MANAGED);

        // Create the requested number of VM definitions
        String userName = "testuser" + TEST_ID;
        List<Creatable<VirtualMachine>> vmDefinitions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String vmName = SdkContext.randomResourceName("vm", 15);

            Creatable<VirtualMachine> vm = computeManager.virtualMachines().define(vmName)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet(network.subnets().values().iterator().next().name())
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword("Abcdef.123456")
                    .withNewAvailabilitySet(availabilitySetDefinition)
                    .withSize(VirtualMachineSizeTypes.STANDARD_A1);

            vmDefinitions.add(vm);
        }

        CreatedResources<VirtualMachine> createdVMs2 = computeManager.virtualMachines().create(vmDefinitions);
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
            .append(resource.publicIPAddressIds().size());
        for (String pipId : resource.publicIPAddressIds()) {
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
                .append("\n\t\t\tFloating IP enabled? ").append(rule.floatingIPEnabled())
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
                info.append("\n\t\t\tPublic IP Address ID: ").append(((LoadBalancerPublicFrontend) frontend).publicIPAddressId());
            } else {
                info.append("\n\t\t\tVirtual network ID: ").append(((LoadBalancerPrivateFrontend) frontend).networkId())
                    .append("\n\t\t\tSubnet name: ").append(((LoadBalancerPrivateFrontend) frontend).subnetName())
                    .append("\n\t\t\tPrivate IP address: ").append(((LoadBalancerPrivateFrontend) frontend).privateIPAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ").append(((LoadBalancerPrivateFrontend) frontend).privateIPAllocationMethod());
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
                .append("\n\t\t\tBackend NIC IP config name: ").append(natRule.backendNicIPConfigurationName())
                .append("\n\t\t\tFloating IP? ").append(natRule.floatingIPEnabled())
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
                .append(backend.backendNicIPConfigurationNames().entrySet().size());
            for (Entry<String, String> entry : backend.backendNicIPConfigurationNames().entrySet()) {
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
