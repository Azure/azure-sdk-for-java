/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Assert;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.Backend;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.HttpProbe;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.InboundNatRule;
import com.microsoft.azure.management.network.PublicFrontend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.LoadDistribution;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PrivateFrontend;
import com.microsoft.azure.management.network.Probe;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.TcpProbe;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;

/**
 * Test of virtual network management.
 */
public class TestLoadBalancer {
    static final long TEST_ID = System.currentTimeMillis();
    static final Region REGION = Region.US_WEST;
    static final String GROUP_NAME = "rg" + TEST_ID;
    static final String LB_NAME = "lb" + TEST_ID;
    static final String[] PIP_NAMES = {"pipa" + TEST_ID, "pipb" + TEST_ID};
    static final String[] VM_IDS = {
            "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest1",
            "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest3"
    };

    /**
     * Internet-facing LB test with NAT pool test.
     */
    public static class InternetWithNatPool extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final PublicIpAddresses pips;
        private final VirtualMachines vms;
        private final Networks networks;

        public InternetWithNatPool(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks) {
            this.pips = pips;
            this.vms = vms;
            this.networks = networks;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestLoadBalancer.VM_IDS);
            List<PublicIpAddress> existingPips = ensurePIPs(pips);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)

                    // Frontends
                    .withExistingPublicIpAddress(existingPips.get(0))
                    .definePublicFrontend("frontend1")
                        .withExistingPublicIpAddress(existingPips.get(1))
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
            Assert.assertTrue(lb.frontends().containsKey("frontend1"));
            Assert.assertTrue(lb.frontends().containsKey("default"));
            Assert.assertTrue(lb.frontends().size() == 2);

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
            InboundNatPool inboundNatPool = lb.inboundNatPools().get("natpool1");
            Assert.assertTrue(inboundNatPool.frontend().name().equalsIgnoreCase("frontend1"));
            Assert.assertTrue(inboundNatPool.frontendPortRangeStart() == 2000);
            Assert.assertTrue(inboundNatPool.frontendPortRangeEnd() == 2001);
            Assert.assertTrue(inboundNatPool.backendPort() == 8080);

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            resource =  resource.update()
                    //TODO .withExistingPublicIpAddress(pip)
                    .withoutFrontend("default")
                    .withoutBackend("default")
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatPool("natpool1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

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

        public InternetWithNatRule(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks) {
            this.pips = pips;
            this.vms = vms;
            this.networks = networks;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestLoadBalancer.VM_IDS);
            List<PublicIpAddress> existingPips = ensurePIPs(pips);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)

                    // Frontends
                    .withExistingPublicIpAddress(existingPips.get(0))
                    .definePublicFrontend("frontend1")
                        .withExistingPublicIpAddress(existingPips.get(1))
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

                    // Inbound NAT rules
                    .defineInboundNatRule("natrule1")
                        .withProtocol(TransportProtocol.TCP)
                        .withFrontend("frontend1")
                        .withFrontendPort(88)
                        .attach()
                    .create();

            // Verify frontends
            Assert.assertTrue(lb.frontends().containsKey("frontend1"));
            Assert.assertTrue(lb.frontends().containsKey("default"));
            Assert.assertTrue(lb.frontends().size() == 2);

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

            // Verify inbound NAT rules
            Assert.assertTrue(lb.inboundNatRules().containsKey("natrule1"));
            Assert.assertTrue(lb.inboundNatRules().size() == 1);
            InboundNatRule inboundNatRule = lb.inboundNatRules().get("natrule1");
            Assert.assertTrue(inboundNatRule.frontend().name().equalsIgnoreCase("frontend1"));
            Assert.assertTrue(inboundNatRule.frontendPort() == 88);
            Assert.assertTrue(inboundNatRule.backendPort() == 88);

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            resource =  resource.update()
                    //TODO .withExistingPublicIpAddress(pip)
                    .withoutFrontend("default")
                    .withoutBackend("default")
                    .withoutLoadBalancingRule("rule1")
                    .withoutInboundNatRule("natrule1")
                    .withTag("tag1", "value1")
                    .withTag("tag2", "value2")
                    .apply();
            Assert.assertTrue(resource.tags().containsKey("tag1"));

            return resource;
        }
    }

    /**
     * Internet-facing minimalistic LB test
     */
    public static class InternetMinimal extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final PublicIpAddresses pips;
        private final VirtualMachines vms;
        private final Networks networks;

        public InternetMinimal(
                PublicIpAddresses pips,
                VirtualMachines vms,
                Networks networks) {
            this.pips = pips;
            this.vms = vms;
            this.networks = networks;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestLoadBalancer.VM_IDS);
            List<PublicIpAddress> existingPips = ensurePIPs(pips);

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // Frontend (default)
                    .withExistingPublicIpAddress(existingPips.get(0))
                    // Backend (default)
                    .withExistingVirtualMachines(existingVMs)
                    // Probe (default)
                    .withTcpProbe(22)
                    // LB rule (default)
                    .withLoadBalancingRule(80, TransportProtocol.TCP)
                    .create();

            Assert.assertTrue(lb.backends().containsKey("default"));
            Assert.assertTrue(lb.frontends().containsKey("default"));
            Assert.assertTrue(lb.tcpProbes().containsKey("default"));
            Assert.assertTrue(lb.loadBalancingRules().containsKey("default"));

            LoadBalancingRule lbrule = lb.loadBalancingRules().get("default");
            Assert.assertTrue(lbrule.frontend().name().equalsIgnoreCase("default"));
            Assert.assertTrue(lbrule.backend().name().equalsIgnoreCase("default"));
            Assert.assertTrue(lbrule.probe().name().equalsIgnoreCase("default"));

            return lb;
        }

        @Override
        public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
            List<PublicIpAddress> existingPips = ensurePIPs(pips);
            PublicIpAddress pip = existingPips.get(1);
            resource =  resource.update()
                    .updateInternetFrontend("default")
                        .withExistingPublicIpAddress(pip)
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
            Assert.assertTrue(resource.tcpProbes().get("default").port() == 22);
            Assert.assertTrue(resource.httpProbes().get("httpprobe").numberOfProbes() == 3);
            Assert.assertTrue(resource.backends().containsKey("backend2"));
            Assert.assertTrue(!resource.backends().containsKey("default"));

            LoadBalancingRule lbRule = resource.loadBalancingRules().get("default");
            Assert.assertTrue(lbRule != null);
            Assert.assertTrue(lbRule.backend() == null);
            Assert.assertTrue(lbRule.backendPort() == 8080);
            Assert.assertTrue(lbRule.frontend().name().equalsIgnoreCase("default"));

            Frontend frontend = resource.frontends().get("default");
            Assert.assertTrue(frontend.isPublic());
            Assert.assertTrue(((PublicFrontend) frontend).publicIpAddressId().equalsIgnoreCase(pip.id()));
            Assert.assertTrue(lbRule.probe().name().equalsIgnoreCase("default"));

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assert.assertTrue(lbRule != null);
            Assert.assertTrue(lbRule.frontendPort() == 22);

            return resource;
        }
    }

    /**
     * Internal minimalistic LB test.
     */
    public static class InternalMinimal extends TestTemplate<LoadBalancer, LoadBalancers> {
        private final VirtualMachines vms;
        private final Networks networks;
        private Network network;

        public InternalMinimal(
                VirtualMachines vms,
                Networks networks) {
            this.vms = vms;
            this.networks = networks;
        }

        @Override
        public void print(LoadBalancer resource) {
            TestLoadBalancer.printLB(resource);
        }

        @Override
        public LoadBalancer createResource(LoadBalancers resources) throws Exception {
            VirtualMachine[] existingVMs = ensureVMs(this.networks, this.vms, TestLoadBalancer.VM_IDS);
            this.network = this.networks.define("net" + TestLoadBalancer.TEST_ID)
                    .withRegion(TestLoadBalancer.REGION)
                    .withNewResourceGroup(TestLoadBalancer.GROUP_NAME)
                    .withAddressSpace("10.0.0.0/28")
                    .withSubnet("subnet1", "10.0.0.0/29")
                    .withSubnet("subnet2", "10.0.0.8/29")
                    .create();

            // Create a load balancer
            LoadBalancer lb = resources.define(TestLoadBalancer.LB_NAME)
                    .withRegion(TestLoadBalancer.REGION)
                    .withExistingResourceGroup(TestLoadBalancer.GROUP_NAME)
                    // Frontend (default)
                    .withExistingSubnet(network, "subnet1")
                    // Backend (default)
                    .withExistingVirtualMachines(existingVMs)
                    .defineBackend("foo")
                    .attach()
                    // Probe (default)
                    .withTcpProbe(22)
                    // LB rule (default)
                    .withLoadBalancingRule(80, TransportProtocol.TCP)
                    .create();

            //TODO Assert.assertTrue(lb.backends().containsKey("default"));
            Assert.assertTrue(lb.frontends().containsKey("default"));
            Assert.assertTrue(lb.tcpProbes().containsKey("default"));
            Assert.assertTrue(lb.loadBalancingRules().containsKey("default"));

            LoadBalancingRule lbrule = lb.loadBalancingRules().get("default");
            Assert.assertTrue(lbrule.frontend().name().equalsIgnoreCase("default"));
            //TODO Assert.assertTrue(lbrule.backend().name().equalsIgnoreCase("default"));
            Assert.assertTrue(lbrule.probe().name().equalsIgnoreCase("default"));

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
            Assert.assertTrue(resource.tcpProbes().get("default").port() == 22);
            Assert.assertTrue(resource.httpProbes().get("httpprobe").numberOfProbes() == 3);
            Assert.assertTrue(resource.backends().containsKey("backend2"));
            Assert.assertTrue(!resource.backends().containsKey("default"));

            LoadBalancingRule lbRule = resource.loadBalancingRules().get("default");
            Assert.assertTrue(lbRule != null);
            Assert.assertTrue(lbRule.backend() == null);
            Assert.assertTrue(lbRule.backendPort() == 8080);
            Assert.assertTrue(lbRule.frontend().name().equalsIgnoreCase("default"));

            Frontend frontend = resource.frontends().get("default");
            Assert.assertTrue(!frontend.isPublic());
            Assert.assertTrue(lbRule.probe().name().equalsIgnoreCase("default"));

            lbRule = resource.loadBalancingRules().get("lbrule2");
            Assert.assertTrue(lbRule != null);
            Assert.assertTrue(lbRule.frontendPort() == 22);

            return resource;
        }
    }

    // Create VNet for the LB
    private static List<PublicIpAddress> ensurePIPs(PublicIpAddresses pips) throws Exception {
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
    private static VirtualMachine[] ensureVMs(Networks networks, VirtualMachines vms, String...vmIds) throws Exception {
        ArrayList<VirtualMachine> createdVMs = new ArrayList<>();
        Network network = null;
        Region region = Region.US_WEST;
        String userName = "testuser" + TEST_ID;
        String availabilitySetName = "as" + TEST_ID;

        for (String vmId : vmIds) {
            String groupName = ResourceUtils.groupFromResourceId(vmId);
            String vmName = ResourceUtils.nameFromResourceId(vmId);
            VirtualMachine vm = null;

            if (groupName == null) {
                // Creating a new VM
                vm = null;
                groupName = "rg" + TEST_ID;
                vmName = "vm" + TEST_ID;

                if (network == null) {
                    // Create a VNet for the VM
                    network = networks.define("net" + TEST_ID)
                        .withRegion(region)
                        .withNewResourceGroup(groupName)
                        .withAddressSpace("10.0.0.0/28")
                        .create();
                }

                vm = vms.define(vmName)
                        .withRegion(Region.US_WEST)
                        .withNewResourceGroup(groupName)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("subnet1")
                        .withPrimaryPrivateIpAddressDynamic()
                        .withoutPrimaryPublicIpAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                        .withRootUserName(userName)
                        .withPassword("Abcdef.123456")
                        .withNewAvailabilitySet(availabilitySetName)
                        .withSize(VirtualMachineSizeTypes.STANDARD_A1)
                        .create();
            } else {
                // Getting an existing VM
                try {
                    vm = vms.getById(vmId);
                } catch (Exception e) {
                    vm = null;
                }
            }

            if (vm != null) {
                createdVMs.add(vm);
            }
        }

        return createdVMs.toArray(new VirtualMachine[createdVMs.size()]);
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
        for (TcpProbe probe : resource.tcpProbes().values()) {
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
        for (HttpProbe probe : resource.httpProbes().values()) {
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

            Frontend frontend = rule.frontend();
            info.append("\n\t\t\tFrontend: ");
            if (frontend != null) {
                info.append(frontend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tFrontend port: ").append(rule.frontendPort());

            Backend backend = rule.backend();
            info.append("\n\t\t\tBackend: ");
            if (backend != null) {
                info.append(backend.name());
            } else {
                info.append("(None)");
            }

            info.append("\n\t\t\tBackend port: ").append(rule.backendPort());

            Probe probe = rule.probe();
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
        for (Frontend frontend : resource.frontends().values()) {
            info.append("\n\t\tFrontend name: ").append(frontend.name())
                .append("\n\t\t\tInternet facing: ").append(frontend.isPublic());
            if (frontend.isPublic()) {
                info.append("\n\t\t\tPublic IP Address ID: ").append(((PublicFrontend) frontend).publicIpAddressId());
            } else {
                info.append("\n\t\t\tVirtual network ID: ").append(((PrivateFrontend) frontend).networkId())
                    .append("\n\t\t\tSubnet name: ").append(((PrivateFrontend) frontend).subnetName())
                    .append("\n\t\t\tPrivate IP address: ").append(((PrivateFrontend) frontend).privateIpAddress())
                    .append("\n\t\t\tPrivate IP allocation method: ").append(((PrivateFrontend) frontend).privateIpAllocationMethod());
            }

            // Inbound NAT pool references
            info.append("\n\t\t\tReferenced inbound NAT pools: ")
                .append(frontend.inboundNatPools().size());
            for (InboundNatPool pool : frontend.inboundNatPools().values()) {
                info.append("\n\t\t\t\tName: ").append(pool.name());
            }

            // Inbound NAT rule references
            info.append("\n\t\t\tReferenced inbound NAT rules: ")
                .append(frontend.inboundNatRules().size());
            for (InboundNatRule rule : frontend.inboundNatRules().values()) {
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
        for (InboundNatRule natRule : resource.inboundNatRules().values()) {
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
        for (InboundNatPool natPool: resource.inboundNatPools().values()) {
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
        for (Backend backend : resource.backends().values()) {
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
                .append(backend.loadBalancingRules().keySet().toArray(new String[0]));
        }

        System.out.println(info.toString());
    }
}
