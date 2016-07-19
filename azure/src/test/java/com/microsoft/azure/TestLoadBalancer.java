/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure;

import java.util.List;

import org.junit.Assert;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancers;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

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

    @Override
    public LoadBalancer createResource(LoadBalancers resources) throws Exception {
        final String newName = "lb" + this.testId;
        Region region = Region.US_WEST;
        String networkName = "net" + this.testId;
        String groupName = "rg" + this.testId;
        String pipName1 = "pip" + this.testId;
        String pipName2 = pipName1 + "b";
        String vmName1 = "vm" + this.testId;
        String vmName2 = "vm" + this.testId + "b";
        String vmUsername = "user" + this.testId;
        String availabilitySetName = "as" + this.testId;

        // Create a VNet for the VMs
        Network vnet = networks.define(networkName)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withAddressSpace("10.0.0.0/28")
                .create();

        // Create VMs in a new availability set
        VirtualMachine vm1 = vms.define(vmName1)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withExistingPrimaryNetwork(vnet)
                .withSubnet("subnet1")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUserName(vmUsername)
                .withPassword("Abcdef.123456")
                .withNewAvailabilitySet(availabilitySetName)
                .withSize(VirtualMachineSizeTypes.STANDARD_A1)
                .create();

        VirtualMachine vm2 = vms.define(vmName2)
                .withRegion(region)
                .withExistingResourceGroup(groupName)
                .withExistingPrimaryNetwork(vnet)
                .withSubnet("subnet1")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUserName(vmUsername)
                .withPassword("Abcdef.123456")
                .withNewAvailabilitySet(availabilitySetName)
                .withSize(VirtualMachineSizeTypes.STANDARD_A1)
                .create();

        // Create a pip
        PublicIpAddress pip1 = this.pips.define(pipName1)
                .withRegion(region)
                .withNewResourceGroup(groupName)
                .withLeafDomainLabel(pipName1)
                .create();

        // Create a load balancer
        return resources.define(newName)
                .withRegion(region)
                .withExistingResourceGroup(groupName)
                .withExistingVirtualMachines(vm1, vm2)
                .withExistingPublicIpAddresses(pip1)
                .withNewPublicIpAddress(pipName2)
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

        System.out.println(info.toString());
    }
}
