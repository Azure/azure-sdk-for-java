/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.HttpProbe;
import com.microsoft.azure.management.network.InternetFrontend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.LoadBalancingRule;
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
public class TestLoadBalancerInternetMinimum extends TestTemplate<LoadBalancer, LoadBalancers> {

    private final PublicIpAddresses pips;
    private final VirtualMachines vms;
    private final Networks networks;

    public TestLoadBalancerInternetMinimum(
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
                .withExistingPublicIpAddresses(pip1)                // Frontend
                .withExistingVirtualMachines(existingVMs)           // Backend
                .withTcpProbe(22)                                   // Probe
                .withLoadBalancingRule(80, TransportProtocol.TCP)   // LB rule
                .create();

        Assert.assertTrue(lb.backends().containsKey("default"));
        Assert.assertTrue(lb.frontends().containsKey("default"));
        Assert.assertTrue(lb.tcpProbes().containsKey("default"));
        Assert.assertTrue(lb.loadBalancingRules().containsKey("default"));

        LoadBalancingRule lbrule = lb.loadBalancingRules().get("default");
        Assert.assertTrue(lbrule.frontend().name().equalsIgnoreCase("default"));

        return lb;
    }

    @Override
    public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
        String pipName = "pip" + this.testId + "b";
        PublicIpAddress pip = this.pips.define(pipName)
                .withRegion(resource.region())
                .withExistingResourceGroup(resource.resourceGroupName())
                .withLeafDomainLabel(pipName)
                .create();

        resource =  resource.update()
                //TODO .withoutFrontend("default")
                //TODO .withExistingPublicIpAddress(pip)
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
                    .withFrontendPort(22)
                    .attach()
                .withBackend("backend2")
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));
        Assert.assertTrue(resource.tcpProbes().get("default").port() == 22);
        Assert.assertTrue(resource.httpProbes().get("httpprobe").numberOfProbes() == 3);
        Assert.assertTrue(resource.loadBalancingRules().get("default").backendPort() == 8080);
        Assert.assertTrue(resource.loadBalancingRules().get("lbrule2").frontendPort() == 22);
        Assert.assertTrue(resource.backends().containsKey("backend2"));

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
        Collection<TcpProbe> tcpProbes = resource.tcpProbes().values();
        if (tcpProbes.size() == 0) {
            info.append(" (None)");
        } else {
            for (TcpProbe probe : tcpProbes) {
                info.append("\n\t\tProbe name: ").append(probe.name())
                    .append("\n\t\t\tPort: ").append(probe.port())
                    .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                    .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes());
            }
        }

        // Show HTTP probes
        info.append("\n\tHTTP probes:");
        Collection<HttpProbe> httpProbes = resource.httpProbes().values();
        if (httpProbes.size() == 0) {
            info.append(" (None)");
        } else {
            for (HttpProbe probe : httpProbes) {
                info.append("\n\t\tProbe name: ").append(probe.name())
                    .append("\n\t\t\tPort: ").append(probe.port())
                    .append("\n\t\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                    .append("\n\t\t\tRetries before unhealthy: ").append(probe.numberOfProbes())
                    .append("\n\t\t\tHTTP request path: ").append(probe.requestPath());
            }
        }

        // Show load balancing rules
        info.append("\n\tLoad balancing rules:");
        Collection<LoadBalancingRule> lbRules = resource.loadBalancingRules().values();
        if (lbRules.size() == 0) {
            info.append(" (None)");
        } else {
            for (LoadBalancingRule rule : lbRules) {
                info.append("\n\t\tLB rule name: ").append(rule.name())
                    .append("\n\t\t\tFrontend port: ").append(rule.frontendPort())
                    .append("\n\t\t\tBackend port: ").append(rule.backendPort())
                    .append("\n\t\t\tProtocol: ").append(rule.protocol())
                    .append("\n\t\t\tFloating IP enabled? ").append(rule.floatingIp())
                    .append("\n\t\t\tIdle timeout in minutes: ").append(rule.idleTimeoutInMinutes())
                    .append("\n\t\t\tLoad distribution method: ").append(rule.loadDistribution().toString())
                    .append("\n\t\t\tFrontend: ").append(rule.frontend().name());
            }
        }

        // Show frontends
        info.append("\n\tFrontends:");
        Collection<Frontend> frontends = resource.frontends().values();
        if (frontends.size() == 0) {
            info.append(" (None)");
        } else {
            for (Frontend frontend : resource.frontends().values()) {
                info.append("\n\t\tFrontend name: ").append(frontend.name())
                    .append("\n\t\t\tInternet facing: ").append(frontend.isInternetFacing());
                if (frontend.isInternetFacing()) {
                    info.append("\n\t\t\tPublic IP Address ID: ").append(((InternetFrontend) frontend).publicIpAddressId());
                }
            }
        }

        System.out.println(info.toString());
    }
}
