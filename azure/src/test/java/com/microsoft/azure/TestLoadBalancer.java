/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.HttpProbe;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.LoadDistribution;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.network.TcpProbe;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

/**
 * Test of virtual network management.
 */
public class TestLoadBalancer extends TestTemplate<LoadBalancer, LoadBalancers> {

    private final PublicIpAddresses pips;
    private final VirtualMachines vms;
    private final Networks networks;

    public TestLoadBalancer(
            PublicIpAddresses pips,
            VirtualMachines vms,
            Networks networks) {
        this.pips = pips;
        this.vms = vms;
        this.networks = networks;
    }

    private VirtualMachine[] ensureVMs(String...vmIds) throws Exception {
        ArrayList<VirtualMachine> createdVMs = new ArrayList<>();
        Network network = null;
        Region region = Region.US_WEST;
        String userName = "user" + this.testId;
        String availabilitySetName = "as" + this.testId;

        for (String vmId : vmIds) {
            String groupName = ResourceUtils.groupFromResourceId(vmId);
            String vmName = ResourceUtils.nameFromResourceId(vmId);
            VirtualMachine vm = null;

            if (groupName == null) {
                // Creating a new VM
                vm = null;
                groupName = "rg" + this.testId;
                vmName = "vm" + this.testId;

                if (network == null) {
                    // Create a VNet for the VM
                    network = networks.define("net" + this.testId)
                        .withRegion(region)
                        .withNewResourceGroup(groupName)
                        .withAddressSpace("10.0.0.0/28")
                        .create();
                }

                vm = this.vms.define(vmName)
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
                    vm = this.vms.getById(vmId);
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


    @Override
    public LoadBalancer createResource(LoadBalancers resources) throws Exception {
        final String newName = "lb" + this.testId;
        Region region = Region.US_WEST;
        String groupName = "rg" + this.testId;
        String pipName1 = "pip" + this.testId;
        String vmID1 = "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest1";
        String vmID2 = "/subscriptions/9657ab5d-4a4a-4fd2-ae7a-4cd9fbd030ef/resourceGroups/marcinslbtest/providers/Microsoft.Compute/virtualMachines/marcinslbtest2";

        VirtualMachine[] existingVMs = ensureVMs(vmID1, vmID2);

        // Create a pip for the LB
        PublicIpAddress pip1 = this.pips.define(pipName1)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withLeafDomainLabel(pipName1)
                .create();

        // Create a load balancer
        LoadBalancer lb = resources.define(newName)
                .withRegion(region)
                .withExistingResourceGroup(groupName)
                .withExistingPublicIpAddresses(pip1)
                .withExistingVirtualMachines(existingVMs)
                .withBackend("backend2")
                .withTcpProbe(80, "tcp1")
                .withLoadBalancingRule(80, TransportProtocol.TCP, "rule1")
                .defineLoadBalancingRule("rule2")
                    .withProtocol(TransportProtocol.TCP)    // Required
                    .withFrontendPort(81)
                    .withBackendPort(82)                    // Optionals
                    .withIdleTimeoutInMinutes(10)
                    .withLoadDistribution(LoadDistribution.SOURCE_IP)
                    .attach()
                .defineTcpProbe("tcp2")
                    .withPort(25)               // Required
                    .withIntervalInSeconds(15)  // Optionals
                    .withNumberOfProbes(5)
                    .attach()
                .defineHttpProbe("http1")
                    .withRequestPath("/")       // Required
                    .withIntervalInSeconds(13)  // Optionals
                    .withNumberOfProbes(4)
                    .attach()
                .create();

        Assert.assertTrue(lb.backends().size() == 2);
        Assert.assertTrue(lb.backends().containsKey("backend2"));
        return lb;
    }

    @Override
    public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
        resource =  resource.update()
                .withoutProbe("tcp2")
                .defineHttpProbe("http2")
                    .withRequestPath("/foo")
                    .withNumberOfProbes(3)
                    .withPort(443)
                    .attach()
                .defineTcpProbe("tcp3")
                    .withPort(33)
                    .withIntervalInSeconds(33)
                    .attach()
                .updateTcpProbe("tcp1")
                    .withPort(81)
                    .parent()
                .updateHttpProbe("http1")
                    .withIntervalInSeconds(14)
                    .withNumberOfProbes(5)
                    .parent()
                .withoutLoadBalancingRule("rule2")
                .updateLoadBalancingRule("rule1")
                    .withBackendPort(8080)
                    .withFrontendPort(8080)
                    .withFloatingIp(true)
                    .withIdleTimeoutInMinutes(11)
                    .withLoadDistribution(LoadDistribution.SOURCE_IPPROTOCOL)
                    .parent()
                .defineLoadBalancingRule("rule3")
                    .withProtocol(TransportProtocol.UDP)
                    .withFrontendPort(22)
                    .attach()
                .withBackend("backend3")
                .withoutBackend("backend2")
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));
        Assert.assertTrue(resource.httpProbes().containsKey("http2"));
        Assert.assertTrue(resource.tcpProbes().containsKey("tcp3"));
        Assert.assertTrue(!resource.tcpProbes().containsKey("tcp2"));
        Assert.assertTrue(resource.backends().containsKey("backend3"));
        Assert.assertTrue(!resource.backends().containsKey("backend2"));
        Assert.assertTrue(resource.loadBalancingRules().containsKey("rule1"));
        Assert.assertTrue(resource.loadBalancingRules().containsKey("rule3"));
        Assert.assertTrue(!resource.loadBalancingRules().containsKey("rule2"));

        return resource;
    }

    @Override
    public void print(LoadBalancer resource) {
        StringBuilder info = new StringBuilder();
        info.append("Load balancer: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags())
                .append("\n\tBackends: ").append(resource.backends().keySet().toString());

        // Show public IP addresses
        info.append("\n\tPublic IP address IDs:");
        List<String> pipIds = resource.publicIpAddressIds();
        if (pipIds == null || pipIds.size() == 0) {
            info.append(" (None)");
        } else {
            for (String pipId : resource.publicIpAddressIds()) {
                info.append("\n\t\tPIP id: ").append(pipId);
            }
        }

        // Show TCP probes
        info.append("\n\tTCP probes:");
        for (TcpProbe probe : resource.tcpProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                .append("\n\t\t\tPort: ").append(probe.port())
                .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes());
        }

        // Show HTTP probes
        info.append("\n\tHTTP probes:");
        for (HttpProbe probe : resource.httpProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                .append("\n\t\t\tPort: ").append(probe.port())
                .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes())
                .append("\n\t\t\tHTTP request path: ").append(probe.requestPath());
        }

        // Show load balancing rules
        info.append("\n\tLoad balancing rules:");
        for (LoadBalancingRule rule : resource.loadBalancingRules().values()) {
            info.append("\n\t\tLB rule name: ").append(rule.name())
                .append("\n\t\t\tFrontend port: ").append(rule.frontendPort())
                .append("\n\t\t\tBackend port: ").append(rule.backendPort())
                .append("\n\t\t\tProtocol: ").append(rule.protocol())
                .append("\n\t\t\tFloating IP enabled? ").append(rule.floatingIp())
                .append("\n\t\t\tIdle timeout in minutes: ").append(rule.idleTimeoutInMinutes())
                .append("\n\t\t\tLoad distribution method: ").append(rule.loadDistribution().toString());

        }

        System.out.println(info.toString());
    }
}
