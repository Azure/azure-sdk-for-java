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
        return resources.define(newName)
                .withRegion(region)
                .withExistingResourceGroup(groupName)
                .withExistingPublicIpAddresses(pip1)
                .withExistingVirtualMachines(existingVMs)
                .withTcpProbe(80)
                .withLoadBalancedPort(80, TransportProtocol.TCP)
                .defineTcpProbe("probeTCP")
                    .withPort(25)               // Required
                    .withIntervalInSeconds(15)  // Optionals
                    .withNumberOfProbes(5)
                    .attach()
                .defineHttpProbe("probeHTTP")
                    .withRequestPath("/")       // Required
                    .withPort(80)               // Optionals
                    .withIntervalInSeconds(13)
                    .withNumberOfProbes(4)
                    .attach()
                .create();
    }

    @Override
    public LoadBalancer updateResource(LoadBalancer resource) throws Exception {
        resource =  resource.update()
                .withTag("tag1", "value1")
                .withTag("tag2", "value2")
                .apply();
        Assert.assertTrue(resource.tags().containsKey("tag1"));

        return resource;
    }

    @Override
    public void print(LoadBalancer resource) {
        StringBuilder info = new StringBuilder();
        info.append("Load balancer: ").append(resource.id())
                .append("Name: ").append(resource.name())
                .append("\n\tResource group: ").append(resource.resourceGroupName())
                .append("\n\tRegion: ").append(resource.region())
                .append("\n\tTags: ").append(resource.tags());

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
                .append("\n\t\tPort: ").append(probe.port())
                .append("\n\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                .append("\n\t\tRetries before unhealthy: ").append(probe.numberOfProbes());
        }

        // Show HTTP probes
        info.append("\n\tHTTP probes:");
        for (HttpProbe probe : resource.httpProbes().values()) {
            info.append("\n\t\tProbe name: ").append(probe.name())
                .append("\n\t\tPort: ").append(probe.port())
                .append("\n\t\tInterval in seconds: ").append(probe.intervalInSeconds())
                .append("\n\t\tRetries before unhealthy: ").append(probe.numberOfProbes())
                .append("\n\t\tHTTP request path: ").append(probe.requestPath());
        }

        System.out.println(info.toString());
    }
}
