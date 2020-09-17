// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.AvailabilitySetSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerHttpProbe;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.LoadBalancerPrivateFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerProbe;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.LoadBalancerTcpProbe;
import com.azure.resourcemanager.network.models.LoadBalancers;
import com.azure.resourcemanager.network.models.LoadBalancingRule;
import com.azure.resourcemanager.network.models.LoadDistribution;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.Networks;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.jupiter.api.Assertions;

/** Test of load balancer management. */
public class TestLoadBalancer {
    String testId = "";
    Region region = Region.US_NORTH_CENTRAL;
    String groupName = "";
    String lbName = "";
    String[] pipNames = null;

    private void initializeResourceNames(SdkContext sdkContext) {
        testId = sdkContext.randomResourceName("", 8);
        groupName = "rg" + testId;
        lbName = "lb" + testId;
        pipNames = new String[] {"pipa" + testId, "pipb" + testId};
    }

    /** Internet-facing LB test with NAT pool test. */
    public class InternetWithNatPool extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;

        /**
         * Test of a load balancer with a NAT pool.
         *
         * @param computeManager compute manager
         */
        public InternetWithNatPool(ComputeManager computeManager) {
            initializeResourceNames(computeManager.sdkContext());
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);
            ensurePIPs(resources.manager().publicIpAddresses());
            PublicIpAddress pip0 = resources.manager().publicIpAddresses().getByResourceGroup(groupName, pipNames[0]);

            // Create a load balancer
            LoadBalancer lb =
                resources
                    .define(lbName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)

                    // Load balancing rules
                    .defineLoadBalancingRule("rule1")
                    .withProtocol(TransportProtocol.TCP) // Required
                    .fromExistingPublicIPAddress(pip0)
                    .fromFrontendPort(81)
                    .toBackend("backend1")
                    .toBackendPort(82) // Optionals
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
                    .withPort(25) // Required
                    .withIntervalInSeconds(15) // Optionals
                    .withNumberOfProbes(5)
                    .attach()
                    .defineHttpProbe("httpProbe1")
                    .withRequestPath("/") // Required
                    .withIntervalInSeconds(13) // Optionals
                    .withNumberOfProbes(4)
                    .attach()

                    // Backends
                    .defineBackend("backend1")
                    .withExistingVirtualMachines(existingVMs)
                    .attach()
                    .create();

            // Verify frontends
            Assertions.assertEquals(1, lb.frontends().size());
            Assertions.assertEquals(1, lb.publicFrontends().size());
            Assertions.assertEquals(0, lb.privateFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assertions.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assertions.assertTrue(pip0.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            // Verify backends
            Assertions.assertEquals(1, lb.backends().size());

            // Verify probes
            Assertions.assertEquals(1, lb.httpProbes().size());
            Assertions.assertTrue(lb.httpProbes().containsKey("httpProbe1"));
            Assertions.assertEquals(1, lb.tcpProbes().size());
            Assertions.assertTrue(lb.tcpProbes().containsKey("tcpProbe1"));

            // Verify rules
            Assertions.assertEquals(1, lb.loadBalancingRules().size());
            Assertions.assertTrue(lb.loadBalancingRules().containsKey("rule1"));
            LoadBalancingRule rule = lb.loadBalancingRules().get("rule1");
            Assertions.assertNotNull(rule.backend());
            Assertions.assertTrue(rule.probe().name().equalsIgnoreCase("tcpProbe1"));

            // Verify inbound NAT pools
            Assertions.assertTrue(lb.inboundNatPools().containsKey("natpool1"));
            Assertions.assertEquals(1, lb.inboundNatPools().size());
            LoadBalancerInboundNatPool inboundNatPool = lb.inboundNatPools().get("natpool1");
            Assertions.assertEquals(2000, inboundNatPool.frontendPortRangeStart());
            Assertions.assertEquals(2001, inboundNatPool.frontendPortRangeEnd());
            Assertions.assertEquals(8080, inboundNatPool.backendPort());

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            resource =
                resource
                    .update()
                    .withoutBackend("backend1")
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatPool("natpool1")
                    .withoutProbe("httpProbe1")
                    .withoutProbe("tcpProbe1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();

            resource.refresh();
            Assertions.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assertions.assertEquals(1, resource.frontends().size());
            Assertions.assertEquals(1, resource.publicFrontends().size());
            Assertions.assertEquals(0, resource.privateFrontends().size());

            // Verify probes
            Assertions.assertFalse(resource.httpProbes().containsKey("httpProbe1"));
            Assertions.assertFalse(resource.httpProbes().containsKey("tcpProbe1"));
            Assertions.assertEquals(0, resource.httpProbes().size());
            Assertions.assertEquals(0, resource.tcpProbes().size());

            // Verify backends
            Assertions.assertEquals(0, resource.backends().size());

            // Verify rules
            Assertions.assertFalse(resource.loadBalancingRules().containsKey("rule1"));
            Assertions.assertEquals(0, resource.loadBalancingRules().size());

            // Verify NAT pools
            Assertions.assertFalse(resource.inboundNatPools().containsKey("natpool1"));

            return resource;
        }
    }

    /** Internet-facing LB test with NAT rules. */
    public class InternetWithNatRule extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;

        /**
         * Tests an Internet-facing load balancer with NAT rules.
         *
         * @param computeManager compute manager
         */
        public InternetWithNatRule(ComputeManager computeManager) {
            initializeResourceNames(computeManager.sdkContext());
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);
            ensurePIPs(resources.manager().publicIpAddresses());
            PublicIpAddress pip = resources.manager().publicIpAddresses().getByResourceGroup(groupName, pipNames[0]);
            NetworkInterface nic1 = existingVMs[0].getPrimaryNetworkInterface();
            NetworkInterface nic2 = existingVMs[1].getPrimaryNetworkInterface();

            // Create a load balancer
            LoadBalancer lb =
                resources
                    .define(lbName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)

                    // Load balancing rules
                    .defineLoadBalancingRule("rule1")
                    .withProtocol(TransportProtocol.TCP) // Required
                    .fromExistingPublicIPAddress(pip)
                    .fromFrontendPort(81)
                    .toBackend("backend1")
                    .toBackendPort(82) // Optionals
                    .withProbe("tcpProbe1")
                    .withIdleTimeoutInMinutes(10)
                    .withLoadDistribution(LoadDistribution.SOURCE_IP)
                    .attach()

                    // Inbound NAT rules
                    .defineInboundNatRule("natrule1")
                    .withProtocol(TransportProtocol.TCP)
                    .fromExistingPublicIPAddress(pip) // Implicitly uses the same frontend because the PIP is the same
                    .fromFrontendPort(88)
                    .attach()

                    // Probes (Optional)
                    .defineTcpProbe("tcpProbe1")
                    .withPort(25) // Required
                    .withIntervalInSeconds(15) // Optionals
                    .withNumberOfProbes(5)
                    .attach()
                    .defineHttpProbe("httpProbe1")
                    .withRequestPath("/") // Required
                    .withIntervalInSeconds(13) // Optionals
                    .withNumberOfProbes(4)
                    .attach()
                    .create();

            String backendName = lb.backends().values().iterator().next().name();
            String frontendName = lb.frontends().values().iterator().next().name();

            // Connect NICs explicitly
            nic1
                .update()
                .withExistingLoadBalancerBackend(lb, backendName)
                .withExistingLoadBalancerInboundNatRule(lb, "natrule1")
                .apply();
            TestNetworkInterface.printNic(nic1);
            Assertions
                .assertTrue(
                    nic1
                        .primaryIPConfiguration()
                        .listAssociatedLoadBalancerBackends()
                        .get(0)
                        .name()
                        .equalsIgnoreCase(backendName));
            Assertions
                .assertTrue(
                    nic1
                        .primaryIPConfiguration()
                        .listAssociatedLoadBalancerInboundNatRules()
                        .get(0)
                        .name()
                        .equalsIgnoreCase("natrule1"));

            nic2.update().withExistingLoadBalancerBackend(lb, backendName).apply();
            TestNetworkInterface.printNic(nic2);
            Assertions
                .assertTrue(
                    nic2
                        .primaryIPConfiguration()
                        .listAssociatedLoadBalancerBackends()
                        .get(0)
                        .name()
                        .equalsIgnoreCase(backendName));

            // Verify frontends
            Assertions.assertEquals(1, lb.frontends().size());
            Assertions.assertEquals(1, lb.publicFrontends().size());
            Assertions.assertEquals(0, lb.privateFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().get(frontendName);
            Assertions.assertNotNull(frontend);
            Assertions.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assertions.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            pip.refresh();
            Assertions.assertTrue(pip.getAssignedLoadBalancerFrontend().name().equalsIgnoreCase(frontendName));
            TestPublicIPAddress.printPIP(pip.refresh());

            // Verify backends
            Assertions.assertTrue(lb.backends().containsKey(backendName));
            Assertions.assertEquals(1, lb.backends().size());

            // Verify probes
            Assertions.assertTrue(lb.httpProbes().containsKey("httpProbe1"));
            Assertions.assertEquals(1, lb.httpProbes().size());
            Assertions.assertTrue(lb.tcpProbes().containsKey("tcpProbe1"));
            Assertions.assertEquals(1, lb.tcpProbes().size());

            // Verify rules
            Assertions.assertEquals(1, lb.loadBalancingRules().size());
            Assertions.assertTrue(lb.loadBalancingRules().containsKey("rule1"));
            LoadBalancingRule rule = lb.loadBalancingRules().get("rule1");
            Assertions.assertTrue(rule.backend().name().equalsIgnoreCase(backendName));
            Assertions.assertTrue(rule.frontend().name().equalsIgnoreCase(frontendName));
            Assertions.assertTrue(rule.probe().name().equalsIgnoreCase("tcpProbe1"));

            // Verify inbound NAT rules
            Assertions.assertEquals(1, lb.inboundNatRules().size());
            Assertions.assertTrue(lb.inboundNatRules().containsKey("natrule1"));
            LoadBalancerInboundNatRule inboundNatRule = lb.inboundNatRules().get("natrule1");
            Assertions.assertTrue(inboundNatRule.frontend().name().equalsIgnoreCase(frontendName));
            Assertions.assertEquals(88, inboundNatRule.frontendPort());
            Assertions.assertEquals(88, inboundNatRule.backendPort());

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
            nic1.update().withoutLoadBalancerBackends().withoutLoadBalancerInboundNatRules().apply();
            Assertions.assertTrue(nic1.primaryIPConfiguration().listAssociatedLoadBalancerBackends().size() == 0);

            nic2.update().withoutLoadBalancerBackends().withoutLoadBalancerInboundNatRules().apply();
            Assertions.assertTrue(nic2.primaryIPConfiguration().listAssociatedLoadBalancerBackends().size() == 0);

            // Update the load balancer
            ensurePIPs(resource.manager().publicIpAddresses());
            PublicIpAddress pip = resource.manager().publicIpAddresses().getByResourceGroup(groupName, pipNames[1]);
            resource =
                resource
                    .update()
                    .updatePublicFrontend(frontendName)
                    .withExistingPublicIpAddress(pip)
                    .parent()
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatRule("natrule1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assertions.assertTrue(resource.tags().containsKey("tag1"));
            Assertions.assertEquals(0, resource.inboundNatRules().size());

            // Verify frontends
            LoadBalancerFrontend frontend = resource.frontends().get(frontendName);
            Assertions.assertEquals(1, resource.publicFrontends().size());
            Assertions.assertEquals(0, resource.privateFrontends().size());
            Assertions.assertNotNull(frontend);
            Assertions.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assertions.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));

            return resource;
        }
    }

    /** Internet-facing minimalistic LB test without LB rules, only a NAT rule. */
    public class InternetNatOnly extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;

        /**
         * Tests an Internet-facing load balancer with a NAT rule only.
         *
         * @param computeManager compute manager
         */
        public InternetNatOnly(ComputeManager computeManager) {
            initializeResourceNames(computeManager.sdkContext());
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), computeManager, 2);
            Creatable<PublicIpAddress> pipDef =
                resources
                    .manager()
                    .publicIpAddresses()
                    .define(pipNames[0])
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
                    .withLeafDomainLabel(pipNames[0]);

            // Create a load balancer
            LoadBalancer lb =
                resources
                    .define(lbName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
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
            Assertions.assertEquals(1, lb.frontends().size());
            Assertions.assertEquals(1, lb.publicFrontends().size());
            Assertions.assertEquals(0, lb.privateFrontends().size());
            LoadBalancerPublicFrontend frontend = lb.publicFrontends().values().iterator().next();
            Assertions.assertNotNull(frontend);
            Assertions.assertNotNull(frontend.publicIpAddressId());

            // Verify probes
            Assertions.assertTrue(lb.tcpProbes().isEmpty());
            Assertions.assertTrue(lb.httpProbes().isEmpty());

            // Verify LB rules
            Assertions.assertEquals(0, lb.loadBalancingRules().size());

            // Verify NAT rules
            Assertions.assertEquals(1, lb.inboundNatRules().size());
            LoadBalancerInboundNatRule natRule = lb.inboundNatRules().get("natrule1");
            Assertions.assertNotNull(natRule);
            Assertions.assertEquals(TransportProtocol.TCP, natRule.protocol());
            Assertions.assertNotNull(natRule.frontend());
            Assertions.assertTrue(natRule.frontend().isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) natRule.frontend();
            PublicIpAddress pip = publicFrontend.getPublicIpAddress();
            Assertions.assertNotNull(pip);
            Assertions.assertEquals(pip.name(), pipNames[0]);
            Assertions.assertEquals(pip.leafDomainLabel(), pipNames[0]);
            Assertions.assertEquals(88, natRule.frontendPort());

            // Verify backends
            Assertions.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assertions.assertNotNull(backend);
            Assertions.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assertions
                    .assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            LoadBalancerBackend backend = resource.backends().values().iterator().next();
            Assertions.assertNotNull(backend);
            LoadBalancerInboundNatRule natRule = resource.inboundNatRules().values().iterator().next();
            Assertions.assertNotNull(natRule);
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) natRule.frontend();
            PublicIpAddress pip =
                resource
                    .manager()
                    .publicIpAddresses()
                    .define(pipNames[1])
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
                    .withLeafDomainLabel(pipNames[1])
                    .create();

            resource =
                resource
                    .update()
                    .updatePublicFrontend(publicFrontend.name())
                    .withExistingPublicIpAddress(pip)
                    .parent()
                    .defineBackend("backend2")
                    .attach()
                    .withoutBackend(backend.name())
                    .withoutInboundNatRule("natrule1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assertions.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assertions.assertEquals(1, resource.frontends().size());
            Assertions.assertEquals(1, resource.publicFrontends().size());
            Assertions.assertEquals(0, resource.privateFrontends().size());
            LoadBalancerFrontend frontend = resource.frontends().get(publicFrontend.name());
            Assertions.assertTrue(frontend.isPublic());
            publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assertions.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));
            Assertions.assertEquals(0, publicFrontend.loadBalancingRules().size());

            // Verify probes
            Assertions.assertTrue(resource.tcpProbes().isEmpty());
            Assertions.assertTrue(resource.httpProbes().isEmpty());

            // Verify backends
            Assertions.assertTrue(resource.backends().containsKey("backend2"));
            Assertions.assertTrue(!resource.backends().containsKey(backend.name()));

            // Verify NAT rules
            Assertions.assertTrue(resource.inboundNatRules().isEmpty());

            // Verify load balancing rules
            Assertions.assertEquals(0, resource.loadBalancingRules().size());

            return resource;
        }
    }

    /** Internet-facing minimalistic LB test. */
    public class InternetMinimal extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;

        /**
         * Tests an Internet-facing load balancer with minimum inputs.
         *
         * @param computeManager compute manager
         */
        public InternetMinimal(ComputeManager computeManager) {
            initializeResourceNames(computeManager.sdkContext());
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);
            String pipDnsLabel = resources.manager().sdkContext().randomResourceName("pip", 20);

            // Create a load balancer
            LoadBalancer lb =
                resources
                    .define(lbName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
                    // LB rule
                    .defineLoadBalancingRule("lbrule1")
                    .withProtocol(TransportProtocol.TCP)
                    .fromNewPublicIPAddress(pipDnsLabel)
                    .fromFrontendPort(80)
                    .toExistingVirtualMachines(existingVMs)
                    .attach()
                    .create();

            // Verify frontends
            Assertions.assertEquals(1, lb.frontends().size());
            Assertions.assertEquals(1, lb.publicFrontends().size());
            Assertions.assertEquals(0, lb.privateFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assertions.assertEquals(1, frontend.loadBalancingRules().size());
            Assertions
                .assertTrue(
                    "lbrule1".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            Assertions.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            PublicIpAddress pip = publicFrontend.getPublicIpAddress();
            Assertions.assertNotNull(pip);
            Assertions.assertTrue(pip.leafDomainLabel().equalsIgnoreCase(pipDnsLabel));

            // Verify TCP probes
            Assertions.assertEquals(0, lb.tcpProbes().size());

            // Verify rules
            Assertions.assertEquals(1, lb.loadBalancingRules().size());
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("lbrule1");
            Assertions.assertNotNull(lbrule.frontend());
            Assertions.assertEquals(80, lbrule.backendPort());
            Assertions.assertEquals(80, lbrule.frontendPort());
            Assertions.assertNull(lbrule.probe());
            Assertions.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assertions.assertNotNull(lbrule.backend());

            // Verify backends
            Assertions.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assertions.assertNotNull(backend);
            Assertions.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assertions
                    .assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            ensurePIPs(resource.manager().publicIpAddresses());
            PublicIpAddress pip = resource.manager().publicIpAddresses().getByResourceGroup(groupName, pipNames[0]);
            Assertions.assertNotNull(pip);
            LoadBalancerBackend backend = resource.backends().values().iterator().next();
            Assertions.assertNotNull(backend);
            LoadBalancingRule lbRule = resource.loadBalancingRules().get("lbrule1");
            Assertions.assertNotNull(lbRule);

            resource =
                resource
                    .update()
                    .updatePublicFrontend(lbRule.frontend().name())
                    .withExistingPublicIpAddress(pip)
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
            Assertions.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assertions.assertEquals(1, resource.frontends().size());
            Assertions.assertEquals(1, resource.publicFrontends().size());
            Assertions.assertEquals(0, resource.privateFrontends().size());
            LoadBalancerFrontend frontend = lbRule.frontend();
            Assertions.assertTrue(frontend.isPublic());
            LoadBalancerPublicFrontend publicFrontend = (LoadBalancerPublicFrontend) frontend;
            Assertions.assertTrue(pip.id().equalsIgnoreCase(publicFrontend.publicIpAddressId()));
            Assertions.assertEquals(2, publicFrontend.loadBalancingRules().size());

            // Verify probes
            LoadBalancerTcpProbe tcpProbe = resource.tcpProbes().get("tcpprobe");
            Assertions.assertNotNull(tcpProbe);
            Assertions.assertEquals(22, tcpProbe.port());
            Assertions.assertEquals(1, tcpProbe.loadBalancingRules().size());
            Assertions.assertTrue(tcpProbe.loadBalancingRules().containsKey("lbrule1"));

            LoadBalancerHttpProbe httpProbe = resource.httpProbes().get("httpprobe");
            Assertions.assertNotNull(httpProbe);
            Assertions.assertEquals(3, httpProbe.numberOfProbes());
            Assertions.assertTrue("/foo".equalsIgnoreCase(httpProbe.requestPath()));
            Assertions.assertTrue(httpProbe.loadBalancingRules().containsKey("lbrule2"));

            // Verify backends
            Assertions.assertEquals(1, resource.backends().size());
            Assertions.assertTrue(resource.backends().containsKey("backend2"));
            Assertions.assertTrue(!resource.backends().containsKey(backend.name()));

            // Verify load balancing rules
            lbRule = resource.loadBalancingRules().get("lbrule1");
            Assertions.assertNotNull(lbRule);
            Assertions.assertNull(lbRule.backend());
            Assertions.assertEquals(8080, lbRule.backendPort());
            Assertions.assertNotNull(lbRule.frontend());
            Assertions.assertEquals(11, lbRule.idleTimeoutInMinutes());
            Assertions.assertNotNull(lbRule.probe());
            Assertions.assertEquals(tcpProbe.name(), lbRule.probe().name());

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assertions.assertNotNull(lbRule);
            Assertions.assertEquals(22, lbRule.frontendPort());
            Assertions.assertNotNull(lbRule.frontend());
            Assertions.assertTrue("httpprobe".equalsIgnoreCase(lbRule.probe().name()));
            Assertions.assertEquals(TransportProtocol.UDP, lbRule.protocol());
            Assertions.assertNotNull(lbRule.backend());
            Assertions.assertTrue("backend2".equalsIgnoreCase(lbRule.backend().name()));

            return resource;
        }
    }

    /** Internal minimalistic LB test. */
    public class InternalMinimal extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;
        private Network network;

        /**
         * Tests an internal load balancer with minimum inputs.
         *
         * @param computeManager compute manager
         */
        public InternalMinimal(ComputeManager computeManager) {
            initializeResourceNames(computeManager.sdkContext());
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);

            // Must use the same VNet as the VMs
            this.network = existingVMs[0].getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();

            // Create a load balancer
            LoadBalancer lb =
                resources
                    .define(lbName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
                    // LB rule
                    .defineLoadBalancingRule("lbrule1")
                    .withProtocol(TransportProtocol.TCP)
                    .fromExistingSubnet(network, "subnet1")
                    .fromFrontendPort(80)
                    .toExistingVirtualMachines(existingVMs)
                    .attach()
                    .create();

            // Verify frontends
            Assertions.assertEquals(1, lb.frontends().size());
            Assertions.assertEquals(1, lb.privateFrontends().size());
            Assertions.assertEquals(0, lb.publicFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assertions.assertEquals(1, frontend.loadBalancingRules().size());
            Assertions.assertFalse(frontend.isPublic());
            Assertions
                .assertTrue(
                    "lbrule1".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assertions.assertTrue(network.id().equalsIgnoreCase(privateFrontend.networkId()));
            Assertions.assertNotNull(privateFrontend.privateIpAddress());
            Assertions.assertTrue("subnet1".equalsIgnoreCase(privateFrontend.subnetName()));
            Assertions.assertEquals(IpAllocationMethod.DYNAMIC, privateFrontend.privateIpAllocationMethod());

            // Verify TCP probes
            Assertions.assertEquals(0, lb.tcpProbes().size());

            // Verify rules
            Assertions.assertEquals(1, lb.loadBalancingRules().size());
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("lbrule1");
            Assertions.assertNotNull(lbrule);
            Assertions.assertNotNull(lbrule.frontend());
            Assertions.assertEquals(80, lbrule.backendPort());
            Assertions.assertEquals(80, lbrule.frontendPort());
            Assertions.assertNull(lbrule.probe());
            Assertions.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assertions.assertNotNull(lbrule.backend());

            // Verify backends
            Assertions.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assertions.assertNotNull(backend);

            Assertions.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assertions
                    .assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
            }

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            LoadBalancerBackend backend = resource.backends().values().iterator().next();
            Assertions.assertNotNull(backend);
            LoadBalancingRule lbRule = resource.loadBalancingRules().get("lbrule1");
            Assertions.assertNotNull(lbRule);
            resource =
                resource
                    .update()
                    .updatePrivateFrontend(lbRule.frontend().name())
                    .withExistingSubnet(this.network, "subnet2")
                    .withPrivateIpAddressStatic("10.0.0.13")
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
            Assertions.assertTrue(resource.tags().containsKey("tag1"));

            // Verify frontends
            Assertions.assertEquals(1, resource.frontends().size());
            Assertions.assertEquals(1, resource.privateFrontends().size());
            Assertions.assertEquals(0, resource.publicFrontends().size());
            LoadBalancerFrontend frontend = resource.frontends().get(lbRule.frontend().name());
            Assertions.assertNotNull(frontend);
            Assertions.assertFalse(frontend.isPublic());
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assertions.assertTrue("subnet2".equalsIgnoreCase(privateFrontend.subnetName()));
            Assertions.assertEquals(IpAllocationMethod.STATIC, privateFrontend.privateIpAllocationMethod());
            Assertions.assertTrue("10.0.0.13".equalsIgnoreCase(privateFrontend.privateIpAddress()));
            Assertions.assertEquals(2, privateFrontend.loadBalancingRules().size());

            // Verify probes
            Assertions.assertEquals(1, resource.tcpProbes().size());
            LoadBalancerTcpProbe tcpProbe = resource.tcpProbes().get("tcpprobe");
            Assertions.assertNotNull(tcpProbe);
            Assertions.assertEquals(22, tcpProbe.port());
            Assertions.assertTrue(tcpProbe.loadBalancingRules().containsKey("lbrule1"));

            LoadBalancerHttpProbe httpProbe = resource.httpProbes().get("httpprobe");
            Assertions.assertNotNull(httpProbe);
            Assertions.assertEquals(3, httpProbe.numberOfProbes());
            Assertions.assertTrue("/foo".equalsIgnoreCase(httpProbe.requestPath()));
            Assertions.assertTrue(httpProbe.loadBalancingRules().containsKey("lbrule2"));

            // Verify backends
            Assertions.assertEquals(1, resource.backends().size());
            Assertions.assertTrue(resource.backends().containsKey("backend2"));
            Assertions.assertTrue(!resource.backends().containsKey(backend.name()));

            // Verify load balancing rules
            lbRule = resource.loadBalancingRules().get("lbrule1");
            Assertions.assertNotNull(lbRule);
            Assertions.assertNull(lbRule.backend());
            Assertions.assertEquals(8080, lbRule.backendPort());
            Assertions.assertNotNull(lbRule.frontend());
            Assertions.assertEquals(11, lbRule.idleTimeoutInMinutes());
            Assertions.assertNotNull(lbRule.probe());
            Assertions.assertEquals(tcpProbe.name(), lbRule.probe().name());

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assertions.assertNotNull(lbRule);
            Assertions.assertEquals(22, lbRule.frontendPort());
            Assertions.assertNotNull(lbRule.frontend());
            Assertions.assertTrue("httpprobe".equalsIgnoreCase(lbRule.probe().name()));
            Assertions.assertEquals(TransportProtocol.UDP, lbRule.protocol());
            Assertions.assertNotNull(lbRule.backend());
            Assertions.assertTrue("backend2".equalsIgnoreCase(lbRule.backend().name()));

            return resource;
        }
    }

    /** Basic SKU load balancer with zoned private */
    public class InternalWithZone extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final ComputeManager computeManager;
        private Network network;

        /**
         * Tests an internal load balancer with zoned front-end.
         *
         * @param computeManager compute manager
         */
        public InternalWithZone(ComputeManager computeManager) {
            region = Region.US_EAST2;
            initializeResourceNames(computeManager.sdkContext());
            this.computeManager = computeManager;
        }

        @Override
        public void print(LoadBalancer resource) {
            printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            // Basic SKU (default) LB requires the VMs to be in the same availability set
            VirtualMachine[] existingVMs = ensureVMs(resources.manager().networks(), this.computeManager, 2);

            // Must use the same VNet as the VMs
            this.network = existingVMs[0].getPrimaryNetworkInterface().primaryIPConfiguration().getNetwork();

            // Create a load balancer
            LoadBalancer lb =
                resources
                    .define(lbName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
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
                    .withAvailabilityZone(AvailabilityZoneId.ZONE_1)
                    .attach()
                    .withSku(LoadBalancerSkuType.BASIC)
                    .create();

            // Verify frontends
            Assertions.assertEquals(1, lb.frontends().size());
            Assertions.assertEquals(1, lb.privateFrontends().size());
            Assertions.assertEquals(0, lb.publicFrontends().size());
            LoadBalancerFrontend frontend = lb.frontends().values().iterator().next();
            Assertions.assertEquals(1, frontend.loadBalancingRules().size());
            Assertions.assertFalse(frontend.isPublic());
            Assertions
                .assertTrue(
                    "lbrule1".equalsIgnoreCase(frontend.loadBalancingRules().values().iterator().next().name()));
            LoadBalancerPrivateFrontend privateFrontend = (LoadBalancerPrivateFrontend) frontend;
            Assertions.assertTrue(network.id().equalsIgnoreCase(privateFrontend.networkId()));
            Assertions.assertNotNull(privateFrontend.privateIpAddress());
            Assertions.assertTrue("subnet1".equalsIgnoreCase(privateFrontend.subnetName()));
            Assertions.assertEquals(IpAllocationMethod.DYNAMIC, privateFrontend.privateIpAllocationMethod());
            // Verify frontend zone
            Assertions.assertNotNull(privateFrontend.availabilityZones());
            Assertions.assertFalse(privateFrontend.availabilityZones().isEmpty());
            Assertions.assertTrue(privateFrontend.availabilityZones().contains(AvailabilityZoneId.ZONE_1));

            // Verify TCP probes
            Assertions.assertEquals(0, lb.tcpProbes().size());

            // Verify rules
            Assertions.assertEquals(1, lb.loadBalancingRules().size());
            LoadBalancingRule lbrule = lb.loadBalancingRules().get("lbrule1");
            Assertions.assertNotNull(lbrule);
            Assertions.assertNotNull(lbrule.frontend());
            Assertions.assertEquals(80, lbrule.backendPort());
            Assertions.assertEquals(80, lbrule.frontendPort());
            Assertions.assertNull(lbrule.probe());
            Assertions.assertEquals(TransportProtocol.TCP, lbrule.protocol());
            Assertions.assertNotNull(lbrule.backend());

            // Verify backends
            Assertions.assertEquals(1, lb.backends().size());
            LoadBalancerBackend backend = lb.backends().values().iterator().next();
            Assertions.assertNotNull(backend);

            Assertions.assertEquals(2, backend.backendNicIPConfigurationNames().size());
            for (VirtualMachine vm : existingVMs) {
                Assertions
                    .assertTrue(backend.backendNicIPConfigurationNames().containsKey(vm.primaryNetworkInterfaceId()));
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
    private Map<String, PublicIpAddress> ensurePIPs(PublicIpAddresses pips) throws Exception {
        List<Creatable<PublicIpAddress>> creatablePips = new ArrayList<>();
        for (int i = 0; i < pipNames.length; i++) {
            creatablePips
                .add(
                    pips
                        .define(pipNames[i])
                        .withRegion(region)
                        .withNewResourceGroup(groupName)
                        .withLeafDomainLabel(pipNames[i]));
        }

        return pips.create(creatablePips);
    }

    // Ensure VMs for the LB
    private VirtualMachine[] ensureVMs(Networks networks, ComputeManager computeManager, int count) throws Exception {
        // Create a network for the VMs
        Network network =
            networks
                .define("net" + testId)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        Creatable<AvailabilitySet> availabilitySetDefinition =
            computeManager
                .availabilitySets()
                .define("as" + testId)
                .withRegion(region)
                .withExistingResourceGroup(groupName)
                .withSku(AvailabilitySetSkuTypes.ALIGNED);

        // Create the requested number of VM definitions
        String userName = "testuser" + testId;
        List<Creatable<VirtualMachine>> vmDefinitions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String vmName = computeManager.sdkContext().randomResourceName("vm", 15);

            Creatable<VirtualMachine> vm =
                computeManager
                    .virtualMachines()
                    .define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(groupName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet(network.subnets().values().iterator().next().name())
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
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
        info
            .append("Load balancer: ")
            .append(resource.id())
            .append("Name: ")
            .append(resource.name())
            .append("\n\tResource group: ")
            .append(resource.resourceGroupName())
            .append("\n\tRegion: ")
            .append(resource.region())
            .append("\n\tTags: ")
            .append(resource.tags())
            .append("\n\tBackends: ")
            .append(resource.backends().keySet().toString());

        // Show public IP addresses
        info.append("\n\tPublic IP address IDs: ").append(resource.publicIpAddressIds().size());
        for (String pipId : resource.publicIpAddressIds()) {
            info.append("\n\t\tPIP id: ").append(pipId);
        }

        // Show TCP probes
        info.append("\n\tTCP probes: ").append(resource.tcpProbes().size());
        for (LoadBalancerTcpProbe probe : resource.tcpProbes().values()) {
            info
                .append("\n\t\tProbe name: ")
                .append(probe.name())
                .append("\n\t\t\tPort: ")
                .append(probe.port())
                .append("\n\t\t\tInterval in seconds: ")
                .append(probe.intervalInSeconds())
                .append("\n\t\t\tRetries before unhealthy: ")
                .append(probe.numberOfProbes());

            // Show associated load balancing rules
            info.append("\n\t\t\tReferenced from load balancing rules: ").append(probe.loadBalancingRules().size());
            for (LoadBalancingRule rule : probe.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show HTTP probes
        info.append("\n\tHTTP probes: ").append(resource.httpProbes().size());
        for (LoadBalancerHttpProbe probe : resource.httpProbes().values()) {
            info
                .append("\n\t\tProbe name: ")
                .append(probe.name())
                .append("\n\t\t\tPort: ")
                .append(probe.port())
                .append("\n\t\t\tInterval in seconds: ")
                .append(probe.intervalInSeconds())
                .append("\n\t\t\tRetries before unhealthy: ")
                .append(probe.numberOfProbes())
                .append("\n\t\t\tHTTP request path: ")
                .append(probe.requestPath());

            // Show associated load balancing rules
            info.append("\n\t\t\tReferenced from load balancing rules: ").append(probe.loadBalancingRules().size());
            for (LoadBalancingRule rule : probe.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show load balancing rules
        info.append("\n\tLoad balancing rules: ").append(resource.loadBalancingRules().size());
        for (LoadBalancingRule rule : resource.loadBalancingRules().values()) {
            info
                .append("\n\t\tLB rule name: ")
                .append(rule.name())
                .append("\n\t\t\tProtocol: ")
                .append(rule.protocol())
                .append("\n\t\t\tFloating IP enabled? ")
                .append(rule.floatingIPEnabled())
                .append("\n\t\t\tIdle timeout in minutes: ")
                .append(rule.idleTimeoutInMinutes())
                .append("\n\t\t\tLoad distribution method: ")
                .append(rule.loadDistribution().toString());

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
        info.append("\n\tFrontends: ").append(resource.frontends().size());
        for (LoadBalancerFrontend frontend : resource.frontends().values()) {
            info
                .append("\n\t\tFrontend name: ")
                .append(frontend.name())
                .append("\n\t\t\tInternet facing: ")
                .append(frontend.isPublic());
            if (frontend.isPublic()) {
                info
                    .append("\n\t\t\tPublic IP Address ID: ")
                    .append(((LoadBalancerPublicFrontend) frontend).publicIpAddressId());
            } else {
                info
                    .append("\n\t\t\tVirtual network ID: ")
                    .append(((LoadBalancerPrivateFrontend) frontend).networkId())
                    .append("\n\t\t\tSubnet name: ")
                    .append(((LoadBalancerPrivateFrontend) frontend).subnetName())
                    .append("\n\t\t\tPrivate IP address: ")
                    .append(((LoadBalancerPrivateFrontend) frontend).privateIpAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ")
                    .append(((LoadBalancerPrivateFrontend) frontend).privateIpAllocationMethod());
            }

            // Inbound NAT pool references
            info.append("\n\t\t\tReferenced inbound NAT pools: ").append(frontend.inboundNatPools().size());
            for (LoadBalancerInboundNatPool pool : frontend.inboundNatPools().values()) {
                info.append("\n\t\t\t\tName: ").append(pool.name());
            }

            // Inbound NAT rule references
            info.append("\n\t\t\tReferenced inbound NAT rules: ").append(frontend.inboundNatRules().size());
            for (LoadBalancerInboundNatRule rule : frontend.inboundNatRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }

            // Load balancing rule references
            info.append("\n\t\t\tReferenced load balancing rules: ").append(frontend.loadBalancingRules().size());
            for (LoadBalancingRule rule : frontend.loadBalancingRules().values()) {
                info.append("\n\t\t\t\tName: ").append(rule.name());
            }
        }

        // Show inbound NAT rules
        info.append("\n\tInbound NAT rules: ").append(resource.inboundNatRules().size());
        for (LoadBalancerInboundNatRule natRule : resource.inboundNatRules().values()) {
            info
                .append("\n\t\tInbound NAT rule name: ")
                .append(natRule.name())
                .append("\n\t\t\tProtocol: ")
                .append(natRule.protocol().toString())
                .append("\n\t\t\tFrontend: ")
                .append(natRule.frontend().name())
                .append("\n\t\t\tFrontend port: ")
                .append(natRule.frontendPort())
                .append("\n\t\t\tBackend port: ")
                .append(natRule.backendPort())
                .append("\n\t\t\tBackend NIC ID: ")
                .append(natRule.backendNetworkInterfaceId())
                .append("\n\t\t\tBackend NIC IP config name: ")
                .append(natRule.backendNicIpConfigurationName())
                .append("\n\t\t\tFloating IP? ")
                .append(natRule.floatingIPEnabled())
                .append("\n\t\t\tIdle timeout in minutes: ")
                .append(natRule.idleTimeoutInMinutes());
        }

        // Show inbound NAT pools
        info.append("\n\tInbound NAT pools: ").append(resource.inboundNatPools().size());
        for (LoadBalancerInboundNatPool natPool : resource.inboundNatPools().values()) {
            info
                .append("\n\t\tInbound NAT pool name: ")
                .append(natPool.name())
                .append("\n\t\t\tProtocol: ")
                .append(natPool.protocol().toString())
                .append("\n\t\t\tFrontend: ")
                .append(natPool.frontend().name())
                .append("\n\t\t\tFrontend port range: ")
                .append(natPool.frontendPortRangeStart())
                .append("-")
                .append(natPool.frontendPortRangeEnd())
                .append("\n\t\t\tBackend port: ")
                .append(natPool.backendPort());
        }

        // Show backends
        info.append("\n\tBackends: ").append(resource.backends().size());
        for (LoadBalancerBackend backend : resource.backends().values()) {
            info.append("\n\t\tBackend name: ").append(backend.name());

            // Show assigned backend NICs
            info.append("\n\t\t\tReferenced NICs: ").append(backend.backendNicIPConfigurationNames().entrySet().size());
            for (Entry<String, String> entry : backend.backendNicIPConfigurationNames().entrySet()) {
                info
                    .append("\n\t\t\t\tNIC ID: ")
                    .append(entry.getKey())
                    .append(" - IP Config: ")
                    .append(entry.getValue());
            }

            // Show assigned virtual machines
            Set<String> vmIds = backend.getVirtualMachineIds();
            info.append("\n\t\t\tReferenced virtual machine ids: ").append(vmIds.size());
            for (String vmId : vmIds) {
                info.append("\n\t\t\t\tVM ID: ").append(vmId);
            }

            // Show assigned load balancing rules
            info
                .append("\n\t\t\tReferenced load balancing rules: ")
                .append(new ArrayList<String>(backend.loadBalancingRules().keySet()));
        }

        System.out.println(info.toString());
    }
}
